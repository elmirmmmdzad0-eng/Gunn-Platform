package com.tripgen.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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

    private final ConcurrentHashMap<String, Map<String, String>> tripCache = new ConcurrentHashMap<>();
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${gemini.api.key}")
    private String apiKey;

    @GetMapping("/analyze")
    public Map<String, String> analyzeTrip(
            @RequestParam String city,
            @RequestParam String status) {
        return callGemini(city, status);
    }

    @GetMapping
    public Map<String, String> getTripPlan(
            @RequestParam String destination,
            @RequestParam(defaultValue = "3") int days) {
        return callGemini(destination, "Tələbə");
    }

    private Map<String, String> callGemini(String city, String status) {
        String cleanCity = cleanInput(city);
        String cleanStatus = cleanInput(status);
        String cacheKey = buildCacheKey(cleanCity, cleanStatus);

        Map<String, String> cachedResponse = tripCache.get(cacheKey);
        if (cachedResponse != null) {
            return new HashMap<>(cachedResponse);
        }

        Map<String, String> responseMap = new HashMap<>();
        responseMap.put("city", cleanCity);

        if (apiKey == null || apiKey.trim().isEmpty()) {
            return errorResponse(cleanCity, "API açarı tapılmadı. Server konfiqurasiyasını yoxlayın.");
        }

        try {
            String prompt = buildPrompt(cleanCity, cleanStatus);
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

            tripCache.put(cacheKey, new HashMap<>(responseMap));
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

    private String buildPrompt(String city, String status) {
        return """
                Azərbaycanlı səyahətçi üçün qısa plan hazırla.
                Şəhər: %s. Status: %s.
                Yalnız bu formatda cavab ver, əlavə mətn yazma:
                HOTEL: 2-3 uyğun otel/ərazi tövsiyəsi
                VISA: viza/e-viza/vizasız məlumatı
                TICKET: təxmini bilet qiyməti və məsləhət
                HACKS: 3 qısa səyahət məsləhəti
                PACKING: 5 vacib əşya
                """.formatted(city, status);
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

    private String buildCacheKey(String city, String status) {
        return normalizeForCache(city) + "::" + normalizeForCache(status);
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
}
