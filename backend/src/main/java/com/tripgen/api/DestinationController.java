package com.tripgen.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") 
public class DestinationController {

    @Value("${gemini.api.key:#{null}}")
    private String apiKey;

    private final String geminiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent";

    @GetMapping("/plan")
    public Map<String, Object> getTripPlan(
            @RequestParam String destination,
            @RequestParam(defaultValue = "3") int days) {

        String prompt = "Sən mükəmməl bir səyahət bələdçisi və ağıllı çamadan hazırlama ekspertisən. " +
                        "Mənə " + destination + " şəhəri üçün " + days + " günlük ətraflı səyahət planı hazırlamalısan. " +
                        "Cavabın mütləq iki əsas hissədən ibarət olsun və təmiz Azərbaycan dilində yazılmalıdır:\n\n" +
                        "1. **SƏYAHƏT MARŞRUTU**: Günbəgün gəziləcək yerlər, tövsiyələr və fəaliyyətlər.\n" +
                        "2. **AĞILLI ÇAMADAN SİYAHISI (Packing List)**: Bu şəhərə, qeyd olunan gün sayına və ümumi mövsümə uyğun olaraq istifadəçinin çamadana mütləq qoymalı olduğu geyimlər, cihazlar və vacib əşyaların siyahısı (hər birinin qarşısına [ ] qoyaraq todo list formatında yaz).";

        Map<String, Object> response = new HashMap<>();
        
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = geminiUrl + "?key=" + apiKey;

            Map<String, Object> textMap = new HashMap<>();
            textMap.put("text", prompt);

            Map<String, Object> partsMap = new HashMap<>();
            partsMap.put("parts", new Object[]{textMap});

            Map<String, Object> contentsMap = new HashMap<>();
            contentsMap.put("contents", new Object[]{partsMap});

            Map<String, Object> geminiResponse = restTemplate.postForObject(url, contentsMap, Map.class);
            response.put("status", "success");
            response.put("data", geminiResponse);
            
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Gemini bağlantısında xəta: " + e.getMessage());
        }

        return response;
    }
}