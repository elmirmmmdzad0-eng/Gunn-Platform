package com.tripgen.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.text.Normalizer;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/destinations")
@CrossOrigin(origins = "*")
public class DestinationController {

    private static final String GEMINI_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";

    private static final String NOT_FOUND = "Məlumat tapılmadı";
    private static final String QUOTA_MESSAGE =
            "Gündəlik AI istifadə limiti dolub. Bir az sonra yenidən yoxlayın.";

    // Production üçün 24 saat. Test üçün 1 saat istəyirsinizsə: 60L * 60 * 1000
    private static final long CACHE_TTL_MS = 24L * 60 * 60 * 1000;
    private static final long CACHE_CLEANUP_INTERVAL_MS = 60L * 60 * 1000;

    private final ConcurrentHashMap<String, CacheEntry> tripCache = new ConcurrentHashMap<>();
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${gemini.api.key}")
    private String apiKey;

    @GetMapping("/analyze")
    public Map<String, String> analyzeTrip(
            @RequestParam String city,
            @RequestParam String status,
            @RequestParam(defaultValue = "AZN") String currency,
            @RequestParam(defaultValue = "") String selectedTypes) {
        return callGemini(city, status, currency, selectedTypes);
    }

    @GetMapping
    public Map<String, String> getTripPlan(
            @RequestParam String destination,
            @RequestParam(defaultValue = "3") int days,
            @RequestParam(defaultValue = "AZN") String currency,
            @RequestParam(defaultValue = "") String selectedTypes) {
        return callGemini(destination, "Tələbə", currency, selectedTypes);
    }

    private Map<String, String> callGemini(String city, String status, String currency, String selectedTypes) {
        String cleanCity = cleanInput(city);
        String cleanStatus = cleanInput(status);
        String cleanCurrency = cleanCurrency(currency);
        String cleanSelectedTypes = cleanSelectedTypes(selectedTypes);
        String cacheKey = buildCacheKey(cleanCity, cleanStatus, cleanCurrency, cleanSelectedTypes);

        Map<String, String> cachedResponse = getCachedResponse(cacheKey);
        if (cachedResponse != null) {
            return cachedResponse;
        }

        Map<String, String> responseMap = new HashMap<>();
        responseMap.put("city", cleanCity);
        responseMap.put("currency", cleanCurrency);
        responseMap.put("selectedTypes", cleanSelectedTypes);

        if (apiKey == null || apiKey.trim().isEmpty()) {
            return errorResponse(cleanCity, "API açarı tapılmadı. Server konfiqurasiyasını yoxlayın.");
        }

        try {
            String prompt = buildPrompt(cleanCity, cleanStatus, cleanCurrency, cleanSelectedTypes);
            String rawResponse = restTemplate.postForObject(
                    URI.create(GEMINI_URL),
                    new HttpEntity<>(buildGeminiRequest(prompt), buildHeaders()),
                    String.class
            );

            String aiText = extractGeminiText(rawResponse);

            responseMap.put("hotel", parseSection(aiText, "HOTEL:", "VISA:"));
            responseMap.put("visa", parseSection(aiText, "VISA:", "TICKET:"));
            responseMap.put("ticket", parseSection(aiText, "TICKET:", "HACKS:"));
            responseMap.put("hacks", parseSection(aiText, "HACKS:", "PACKING:"));
            responseMap.put("packingList", parseSection(aiText, "PACKING:", "END_OF_TEXT"));

            tripCache.put(cacheKey, new CacheEntry(responseMap));
            return responseMap;
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                return errorResponse(cleanCity, QUOTA_MESSAGE);
            }

            return errorResponse(cleanCity, "AI servisi müvəqqəti olaraq cavab vermir. Bir az sonra yenidən cəhd edin.");
        } catch (Exception e) {
            return errorResponse(cleanCity, "Məlumat müvəqqəti olaraq yüklənmədi. Bir az sonra yenidən cəhd edin.");
        }
    }

    private Map<String, String> getCachedResponse(String cacheKey) {
        CacheEntry cacheEntry = tripCache.get(cacheKey);
        if (cacheEntry == null) {
            return null;
        }

        if (cacheEntry.isExpired(System.currentTimeMillis())) {
            tripCache.remove(cacheKey, cacheEntry);
            return null;
        }

        return cacheEntry.responseCopy();
    }

    @Scheduled(fixedRate = CACHE_CLEANUP_INTERVAL_MS)
    public void cleanExpiredCacheEntries() {
        long now = System.currentTimeMillis();
        tripCache.entrySet().removeIf(entry -> entry.getValue().isExpired(now));
    }

    private String buildPrompt(String city, String status, String currency, String selectedTypes) {
        return """
                Azərbaycanlı səyahətçi üçün qısa plan hazırla.
                Şəhər: %s. Status: %s.
                Bütün otel, viza, yemək və nəqliyyat qiymətlərini tam olaraq %s valyutası ilə hesabla və cavabda qiymətlərin yanına bu valyutanı yaz.
                %s
                Yalnız bu formatda cavab ver, əlavə mətn yazma:
                HOTEL: 2-3 uyğun otel/ərazi tövsiyəsi
                VISA: viza/e-viza/vizasız məlumatı
                TICKET: təxmini bilet qiyməti və məsləhət
                HACKS: 3 qısa səyahət məsləhəti
                PACKING: 5 vacib əşya
                """.formatted(
                city,
                status,
                currency,
                buildSelectedTypesInstruction(city, selectedTypes)
                        + "\n" + ConcretePlaceCatalog.promptRules(city, currency)
        );
    }

    private String buildSelectedTypesInstruction(String city, String selectedTypes) {
        if (selectedTypes == null || selectedTypes.isBlank()) {
            return "Səyahət stili seçilməyib: balanslı, ümumi faydalı plan hazırla.";
        }

        return "CRITICAL INSTRUCTION: The user has strictly customized this trip for the following travel styles: "
                + selectedTypes
                + ". You MUST transform HOTEL, HACKS, food, area and experience recommendations based on these styles. If \"Romantik turizm\" is selected for "
                + city
                + ", prioritize romantic viewpoints, elegant dining, scenic walks and couples' activities through exact real names such as Giardino degli Aranci, Bar San Calisto, or exact local equivalents. If \"Konsert turizmi\" is selected, include real concert halls, live music venues and jazz clubs such as Royal Albert Hall or Alexanderplatz Jazz Club where relevant. If \"Qastronomik turizm\" is selected, include named markets, tasting routes, restaurants and signature dishes. Do not return generic mass-tourism advice.";
    }

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-goog-api-key", apiKey.trim());
        return headers;
    }

    private Map<String, Object> buildGeminiRequest(String prompt) {
        Map<String, Object> textMap = new HashMap<>();
        textMap.put("text", prompt);

        Map<String, Object> partsMap = new HashMap<>();
        partsMap.put("parts", new Object[]{textMap});

        Map<String, Object> contentsMap = new HashMap<>();
        contentsMap.put("contents", new Object[]{partsMap});
        return contentsMap;
    }

    private String extractGeminiText(String rawResponse) throws Exception {
        JsonNode root = objectMapper.readTree(rawResponse);
        JsonNode textNode = root.path("candidates")
                .path(0)
                .path("content")
                .path("parts")
                .path(0)
                .path("text");

        if (textNode.isMissingNode() || textNode.asText().isBlank()) {
            throw new IllegalStateException("Gemini boş cavab qaytardı.");
        }

        return textNode.asText();
    }

    private Map<String, String> errorResponse(String city, String message) {
        Map<String, String> responseMap = new HashMap<>();
        responseMap.put("city", city);
        responseMap.put("hotel", message);
        responseMap.put("visa", message);
        responseMap.put("ticket", NOT_FOUND);
        responseMap.put("hacks", NOT_FOUND);
        responseMap.put("packingList", NOT_FOUND);
        return responseMap;
    }

    private String parseSection(String fullText, String startTag, String endTag) {
        try {
            int start = fullText.indexOf(startTag);
            if (start == -1) {
                return NOT_FOUND;
            }

            start += startTag.length();
            int end = endTag.equals("END_OF_TEXT") ? fullText.length() : fullText.indexOf(endTag);
            if (end == -1 || end < start) {
                end = fullText.length();
            }

            String result = fullText.substring(start, end).trim();
            return result.isBlank() ? NOT_FOUND : result;
        } catch (Exception e) {
            return NOT_FOUND;
        }
    }

    private String buildCacheKey(String city, String status, String currency, String selectedTypes) {
        return normalizeForCache(city)
                + "::" + normalizeForCache(status)
                + "::" + normalizeForCache(currency)
                + "::" + normalizeForCache(selectedTypes);
    }

    private String normalizeForCache(String value) {
        String normalized = Normalizer.normalize(cleanInput(value), Normalizer.Form.NFKC);
        return normalized.toLowerCase(Locale.ROOT);
    }

    private String cleanInput(String value) {
        if (value == null) {
            return "";
        }

        return value.trim().replaceAll("\\s+", " ");
    }

    private String cleanSelectedTypes(String value) {
        String cleaned = cleanInput(value)
                .replaceAll("[\\r\\n]+", " ")
                .replaceAll("\\s+", " ");
        return cleaned.length() > 600 ? cleaned.substring(0, 600).trim() : cleaned;
    }

    private String cleanCurrency(String value) {
        String normalized = cleanInput(value).toUpperCase(Locale.ROOT);
        if (normalized.equals("USD") || normalized.equals("EUR")) {
            return normalized;
        }

        return "AZN";
    }

    private static class CacheEntry {
        private final Map<String, String> response;
        private final long createdAt;

        private CacheEntry(Map<String, String> response) {
            this.response = new HashMap<>(response);
            this.createdAt = System.currentTimeMillis();
        }

        private boolean isExpired(long now) {
            return now - createdAt > CACHE_TTL_MS;
        }

        private Map<String, String> responseCopy() {
            return new HashMap<>(response);
        }
    }
}
