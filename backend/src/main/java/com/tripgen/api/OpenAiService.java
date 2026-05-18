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

        // Real OpenAI HTTP çağırışı hazır olanda burada ediləcək.
        // 429, insufficient_quota və billing xətalarında AiProviderException(..., true) atın.
        throw new AiProviderException(
                getProviderName(),
                "OpenAI generatoru hələ real API-yə qoşulmayıb. Gemini fallback aktivləşdirilir.",
                true
        );
    }
}
