package com.tripgen.api;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.Locale;
import java.util.UUID;

@Service
public class TripService {

    private final TripPlanRepository tripPlanRepository;
    private final OpenAiService openAiService;
    private final GeminiService geminiService;
    private final WebSearchFallbackService webSearchFallbackService;

    public TripService(
            TripPlanRepository tripPlanRepository,
            OpenAiService openAiService,
            GeminiService geminiService,
            WebSearchFallbackService webSearchFallbackService
    ) {
        this.tripPlanRepository = tripPlanRepository;
        this.openAiService = openAiService;
        this.geminiService = geminiService;
        this.webSearchFallbackService = webSearchFallbackService;
    }

    @Transactional
    public TripResponse generateTrip(TripRequest request) {
        TripRequestContext context = normalizeRequest(request);

        return tripPlanRepository
                .findFirstByNormalizedDestinationAndDaysAndBudgetTypeAndLanguageCodeOrderByCreatedAtDesc(
                        context.getNormalizedDestination(),
                        context.getDays(),
                        context.getBudgetType(),
                        context.getLanguageCode()
                )
                .map(this::toResponse)
                .orElseGet(() -> generateAndCache(context));
    }

    private TripResponse generateAndCache(TripRequestContext context) {
        ProviderResult providerResult = generateWithFallbacks(context);

        TripPlan tripPlan = new TripPlan();
        tripPlan.setId(UUID.randomUUID().toString());
        tripPlan.setDestination(context.getDestination());
        tripPlan.setNormalizedDestination(context.getNormalizedDestination());
        tripPlan.setDays(context.getDays());
        tripPlan.setBudgetType(context.getBudgetType());
        tripPlan.setLanguageCode(context.getLanguageCode());
        tripPlan.setItineraryRaw(providerResult.itineraryRaw());
        tripPlan.setSource(providerResult.source());

        return toResponse(tripPlanRepository.save(tripPlan));
    }

    private ProviderResult generateWithFallbacks(TripRequestContext context) {
        try {
            return new ProviderResult(openAiService.getProviderName(), openAiService.generate(context));
        } catch (Exception openAiError) {
            logProviderFailure(openAiService.getProviderName(), openAiError);
        }

        try {
            return new ProviderResult(geminiService.getProviderName(), geminiService.generate(context));
        } catch (Exception geminiError) {
            logProviderFailure(geminiService.getProviderName(), geminiError);
        }

        String fallbackPlan = webSearchFallbackService.generateFromStaticSearch(context);
        return new ProviderResult("WebSearchFallback", fallbackPlan);
    }

    private void logProviderFailure(String providerName, Exception error) {
        boolean quotaIssue = error instanceof AiProviderException aiError && aiError.isQuotaOrBillingLimit();
        System.out.println("[TRIP_PROVIDER_FAILOVER] provider=" + providerName
                + ", quotaOrBilling=" + quotaIssue
                + ", reason=" + error.getMessage());
    }

    private TripResponse toResponse(TripPlan tripPlan) {
        return new TripResponse(
                tripPlan.getId(),
                tripPlan.getDestination(),
                tripPlan.getDays(),
                tripPlan.getItineraryRaw()
        );
    }

    private TripRequestContext normalizeRequest(TripRequest request) {
        String destination = cleanDestination(request == null ? null : request.getDestination());
        int days = normalizeDays(request == null ? 0 : request.getDays());
        String budgetType = cleanBudgetType(request == null ? null : request.getBudgetType());
        String languageCode = cleanLanguage(request == null ? null : request.getLang());
        String normalizedDestination = normalizeForLookup(destination);

        return new TripRequestContext(destination, normalizedDestination, days, budgetType, languageCode);
    }

    private String cleanDestination(String destination) {
        if (destination == null || destination.trim().isBlank()) {
            return "İstanbul";
        }

        return destination.trim().replaceAll("\\s+", " ");
    }

    private int normalizeDays(int days) {
        if (days < 1) {
            return 1;
        }

        return Math.min(days, 7);
    }

    private String cleanBudgetType(String budgetType) {
        if (budgetType == null || budgetType.trim().isBlank()) {
            return "Orta";
        }

        String cleaned = budgetType.trim().replaceAll("\\s+", " ");
        String normalized = normalizeForLookup(cleaned);
        if (normalized.equals("ekonomik")) {
            return "Ekonomik";
        }
        if (normalized.equals("luks") || normalized.equals("lux")) {
            return "Lüks";
        }

        return "Orta";
    }

    private String normalizeForLookup(String value) {
        String cleaned = value == null ? "" : value.trim().replaceAll("\\s+", " ");
        String decomposed = Normalizer.normalize(cleaned, Normalizer.Form.NFD);
        String withoutMarks = decomposed.replaceAll("\\p{M}", "");
        return withoutMarks.toLowerCase(Locale.ROOT);
    }

    private String cleanLanguage(String lang) {
        String normalized = lang == null ? "" : lang.trim().toLowerCase(Locale.ROOT);
        if (normalized.equals("en") || normalized.equals("ru")) {
            return normalized;
        }

        return "az";
    }

    private record ProviderResult(String source, String itineraryRaw) {
    }
}
