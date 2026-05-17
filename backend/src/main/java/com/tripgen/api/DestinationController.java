package com.tripgen.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/destinations")
@CrossOrigin(origins = "*")
public class DestinationController {

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
        Map<String, String> responseMap = new HashMap<>();
        responseMap.put("city", city);

        if (apiKey == null || apiKey.trim().isEmpty()) {
            responseMap.put("hotel", "XƏTA: API Açar tapılmadı!");
            return responseMap;
        }

        String prompt = "Sən professional turizm ekspertisən. Səyahətçi Azərbaycan vətəndaşıdır, statusu '" + status + "'-dur. " +
                        "Bu şəxs " + city + " şəhərinə getmək istəyir. " +
                        "Mənə aşağıdakı formatda, əlavə heç bir mətn yazmadan, yalnız bu 5 başlığı daxil edən cavab qaytar:\n\n" +
                        "HOTEL: [Otel tövsiyələri]\n" +
                        "VISA: [Viza şərtləri]\n" +
                        "TICKET: [Bilet qiymətləri]\n" +
                        "HACKS: [Səyahət fəndləri]\n" +
                        "PACKING: [Çamadan əşya siyahısı]";

        try {
            RestTemplate restTemplate = new RestTemplate();
            // DƏYİŞİKLİK BURADADIR: v1beta bura dəyişdirildi -> v1
            String urlString = "https://generativelanguage.googleapis.com/v1/models/gemini-1.5-flash:generateContent?key=" + apiKey.trim();
            URI uri = URI.create(urlString);

            Map<String, Object> textMap = new HashMap<>();
            textMap.put("text", prompt);

            Map<String, Object> partsMap = new HashMap<>();
            partsMap.put("parts", new Object[]{textMap});

            Map<String, Object> contentsMap = new HashMap<>();
            contentsMap.put("contents", new Object[]{partsMap});

            String rawResponse = restTemplate.postForObject(uri, contentsMap, String.class);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(rawResponse);
            String aiText = root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();

            responseMap.put("hotel", parseSection(aiText, "HOTEL:", "VISA:"));
            responseMap.put("visa", parseSection(aiText, "VISA:", "TICKET:"));
            responseMap.put("ticket", parseSection(aiText, "TICKET:", "HACKS:"));
            responseMap.put("hacks", parseSection(aiText, "HACKS:", "PACKING:"));
            responseMap.put("packingList", parseSection(aiText, "PACKING:", "END_OF_TEXT"));

        } catch (Exception e) {
            responseMap.put("hotel", "Sistemə qoşulmada xəta baş verdi.");
            responseMap.put("visa", "Xəta: " + e.getMessage());
            responseMap.put("ticket", "Məlumat tapılmadı");
            responseMap.put("hacks", "Məlumat tapılmadı");
            responseMap.put("packingList", "Məlumat tapılmadı");
        }

        return responseMap;
    }

    private String parseSection(String fullText, String startTag, String endTag) {
        try {
            int start = fullText.indexOf(startTag);
            if (start == -1) return "Məlumat tapılmadı";
            start += startTag.length();
            
            int end = endTag.equals("END_OF_TEXT") ? fullText.length() : fullText.indexOf(endTag);
            if (end == -1 || end < start) end = fullText.length();
            
            return fullText.substring(start, end).trim();
        } catch (Exception e) {
            return "Məlumat tapılmadı";
        }
    }
}