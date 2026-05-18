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
                Build a clean day-by-day travel itinerary.
                Destination: %s
                Days: %d
                Budget type: %s

                Return one line per day in this format:
                Day 1: hotel/check-in, breakfast, historic place, dinner, transfer note.

                %s
                """.formatted(
                context.getDestination(),
                context.getDays(),
                context.getBudgetType(),
                context.getLanguageInstruction()
        );
    }
}
