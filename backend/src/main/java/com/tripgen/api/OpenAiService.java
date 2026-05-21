package com.tripgen.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class OpenAiService implements TripPlanProvider {

    @Value("${openai.api.key:}")
    private String apiKey;

    @Override
    public String getProviderName() {
        return "OpenAI";
    }

    @Override
    public String generate(TripRequestContext context) {
        if (apiKey == null || apiKey.trim().isBlank()) {
            throw new AiProviderException(
                    getProviderName(),
                    "OPENAI_API_KEY tapılmadı. Gemini fallback aktivləşdirilir.",
                    true
            );
        }

        String prompt = buildPrompt(context);
        System.out.println("[OPENAI_PROMPT_READY] lang=" + context.getLanguageCode()
                + ", length=" + prompt.length());

        // Real OpenAI HTTP çağırışı hazır olanda burada ediləcək.
        // 429, insufficient_quota və billing xətalarında AiProviderException(..., true) atın.
        throw new AiProviderException(
                getProviderName(),
                "OpenAI generatoru hələ real API-yə qoşulmayıb. Gemini fallback aktivləşdirilir.",
                true
        );
    }

    private String buildPrompt(TripRequestContext context) {
        return """
                Build a clean day-by-day travel itinerary for GUNN.
                Destination: %s
                Days: %d
                Budget type: %s
                Tourism style input: %s

                Absolute quality rules:
                - Never use generic phrases such as "quiet neighborhood cafe", "central hotel", "local restaurant", "hidden viewpoint", "artisan lane", "main shopping street", or "nearby ATM".
                - Every hotel, cafe, restaurant, museum, viewpoint, bank, exchange office, ATM network, mall, supermarket and shopping street must be a real official name in or directly relevant to the destination.
                - If tourism style input contains selected types, make those styles the main route logic and express them through real named places.
                - Include 3 real hotels matched to the budget type.
                - Include tourist infrastructure clusters: Real hotels, Currency exchange, Fee-friendly ATMs, Shopping and daily essentials.
                - Add a MAP_POINTS line with 5-7 comma-separated real places that Google Maps and Google Earth can search together.

                Return one line per day in this format, using real names:
                Day 1: hotel/check-in at Hotel Name, breakfast at Cafe Name, Museum Name, dinner at Restaurant Name, transfer note.
                Before MAP_POINTS and IMAGE_KEYWORDS, add a separate block titled exactly HIDDEN_GEMS:
                HIDDEN_GEMS:
                1. Place name - why this lesser-known local spot is special. Local tip: what to taste or do there.
                2. Place name - why this lesser-known local spot is special. Local tip: what to taste or do there.
                3. Place name - why this lesser-known local spot is special. Local tip: what to taste or do there.
                IMPORTANT: Write every place name, description and local tip inside HIDDEN_GEMS in the user's selected language (%s). If the language is AZ, use only Azerbaijani; if RU, use only Russian; if EN, use only English.
                Add one exact MAP_POINTS line:
                MAP_POINTS: Hotel Ritz Paris, Café de Flore, BNP Paribas Saint-Germain, Westfield Forum des Halles, Musée Jacquemart-André
                At the end, add exactly one line with 3-4 English Pexels search keywords:
                IMAGE_KEYWORDS: Paris cafe, Eiffel Tower night, Louvre museum, Seine river

                %s
                """.formatted(
                context.getDestination(),
                context.getDays(),
                context.getBudgetType(),
                context.getSelectedTypesInstruction(),
                context.getLanguageCode().toUpperCase(),
                context.getLanguageInstruction()
        );
    }
}
