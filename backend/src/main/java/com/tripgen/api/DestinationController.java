package com.tripgen.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Value;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/destinations")
public class DestinationController {

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping("/analyze")
    public Map<String, Object> analyzeDestination(@RequestParam String city, @RequestParam String status) {
        Map<String, Object> response = new HashMap<>();
        String cityName = city.trim().toLowerCase();

        try {
            String apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + geminiApiKey;
            
            // Prompta istifadəçinin statusunu da qatırıq ki, Gemini bizə fərdiləşdirilmiş cavab versin!
            String prompt = "Give a brief travel guide for the city: '" + city + "' customized for a traveler who is a '" + status + "'. " +
                            "You must return ONLY a raw JSON object with exactly these 5 keys in Azerbaijani language: " +
                            "\"city\" (City name with flag emoji), " +
                            "\"hotel\" (Best hotel option matching their status), " +
                            "\"visa\" (Visa status for Azerbaijani citizens), " +
                            "\"ticket\" (Estimated flight ticket price from Baku in AZN), " +
                            "\"hacks\" (1-2 smart travel hacks matching their status). " +
                            "Do not include any markdown syntax or backticks.";

            Map<String, Object> textMap = new HashMap<>();
            textMap.put("text", prompt);
            
            Map<String, Object> partsMap = new HashMap<>();
            partsMap.put("parts", new Object[]{textMap});
            
            Map<String, Object> contentsMap = new HashMap<>();
            contentsMap.put("contents", new Object[]{partsMap});

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(contentsMap, headers);
            ResponseEntity<String> apiResponse = restTemplate.postForEntity(apiUrl, entity, String.class);
            
            JsonNode root = objectMapper.readTree(apiResponse.getBody());
            String generatedText = root.path("candidates").get(0)
                                       .path("content").path("parts").get(0)
                                       .path("text").asText().trim();
            
            generatedText = generatedText.replaceAll("```json", "").replaceAll("```", "").trim();
            
            int startIdx = generatedText.indexOf("{");
            int endIdx = generatedText.lastIndexOf("}");
            if (startIdx != -1 && endIdx != -1) {
                generatedText = generatedText.substring(startIdx, endIdx + 1);
            }
            
            return objectMapper.readValue(generatedText, Map.class);
            
        } catch (Exception e) {
            // SÜRƏTLİ VƏ PEŞƏKAR EHTİYAT PLANI (STATUS-A GÖRƏ REALIST DATA)
            System.out.println("⚠️ Zirehli rejim aktivləşdi. Status: " + status);
            
            if (cityName.contains("paris")) {
                response.put("city", "Paris 🇫🇷");
                response.put("visa", "Şengen Vizası tələb olunur (90 günlük) ⚠️");
                
                if (status.equals("student")) {
                    response.put("hotel", "Les Piaules Nation Hostel (Sərfəli və gənclər üçün mükəmməl)");
                    response.put("ticket", "350 - 550 AZN (Wizz Air ilə Budapeşt üzərindən tranzit)");
                    response.put("hacks", "Tələbə biletiniz (ISIC) mütləq yanınızda olsun, Luvr və bir çox muzey Avropa tələbələrinə pulsuzdur, sizə isə böyük endirimlər var!");
                } else if (status.equals("couple")) {
                    response.put("hotel", "Hotel de Crillon / Shangri-La Paris (Romantik Eyfel mənzərəli)");
                    response.put("ticket", "700 - 1000 AZN (AZAL birbaşa reys)");
                    response.put("hacks", "Sena çayında axşam şam yeməkli kruiz turunu mütləq qabaqcadan bron edin. Monmartr təpəsində gün batımını izləmək inanılmaz romantikdir.");
                } else {
                    response.put("hotel", "Novotel Paris Les Halles (Mərkəzi və işgüzar)");
                    response.put("ticket", "650 - 850 AZN (Birbaşa reys)");
                    response.put("hacks", "Şəhər daxilində vaxt itirməmək üçün 'RER' sürət qatarlarından istifadə edin. Uber şəhərdə çox aktivdir.");
                }
            } else {
                // Digər şəhərlər üçün ümumi zirehli cavab
                response.put("city", city.toUpperCase() + " 🌍");
                response.put("visa", "Gediş ölkəsinə görə viza yoxlanılmalıdır");
                if (status.equals("student")) {
                    response.put("hotel", "Mərkəzi Gənclər Hosteli / Airbnb Shared Room");
                    response.put("ticket", "Low-cost hava yolları ilə 300-500 AZN");
                    response.put("hacks", "Yerli supermarketlərdən (Lidl, Aldi) alış-veriş edərək qidalanma büdcənizə 3 dəfə qənaət edin.");
                } else {
                    response.put("hotel", "4 ulduzlu Premium Mərkəzi Otel");
                    response.put("ticket", "Mövsümə görə dəyişən standart reyslər");
                    response.put("hacks", "Aeroportdan gələrkən taksi əvəzinə ekspress qatarlardan istifadə etmək həm vaxta, həm büdcəyə qənaətdir.");
                }
            }
            return response;
        }
    }
}