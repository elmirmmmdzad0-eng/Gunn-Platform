package com.tripgen.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class TripService {

    private final TripPlanRepository tripPlanRepository;
    private final OpenAiService openAiService;
    private final GeminiService geminiService;
    private final WebSearchFallbackService webSearchFallbackService;
    private final PexelsService pexelsService;
    private final ObjectMapper objectMapper;

    public TripService(
            TripPlanRepository tripPlanRepository,
            OpenAiService openAiService,
            GeminiService geminiService,
            WebSearchFallbackService webSearchFallbackService,
            PexelsService pexelsService,
            ObjectMapper objectMapper
    ) {
        this.tripPlanRepository = tripPlanRepository;
        this.openAiService = openAiService;
        this.geminiService = geminiService;
        this.webSearchFallbackService = webSearchFallbackService;
        this.pexelsService = pexelsService;
        this.objectMapper = objectMapper;
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
        List<String> imageKeywords = extractImageKeywords(providerResult.itineraryRaw(), context);
        List<String> imageUrls = fetchTargetedImages(context, imageKeywords);
        String cleanItinerary = removeImageKeywordLine(providerResult.itineraryRaw());

        TripPlan tripPlan = new TripPlan();
        tripPlan.setId(UUID.randomUUID().toString());
        tripPlan.setDestination(context.getDestination());
        tripPlan.setNormalizedDestination(context.getNormalizedDestination());
        tripPlan.setDays(context.getDays());
        tripPlan.setBudgetType(context.getBudgetType());
        tripPlan.setLanguageCode(context.getLanguageCode());
        tripPlan.setItineraryRaw(cleanItinerary);
        tripPlan.setImageUrlsJson(toImageUrlsJson(imageUrls));
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
        String itineraryRaw = ensureHiddenGemsBlock(
                tripPlan.getItineraryRaw(),
                tripPlan.getDestination()
        );

        return new TripResponse(
                tripPlan.getId(),
                tripPlan.getDestination(),
                tripPlan.getDays(),
                itineraryRaw,
                fromImageUrlsJson(tripPlan.getImageUrlsJson())
        );
    }

    private String ensureHiddenGemsBlock(String itineraryRaw, String destination) {
        String cleanItinerary = itineraryRaw == null ? "" : itineraryRaw.trim();
        if (cleanItinerary.toUpperCase(Locale.ROOT).contains("HIDDEN_GEMS:")) {
            return cleanItinerary;
        }

        String hiddenGemsBlock = buildDefaultHiddenGemsBlock(destination);
        if (cleanItinerary.isBlank()) {
            return hiddenGemsBlock;
        }

        return cleanItinerary + "\n\n" + hiddenGemsBlock;
    }

    private String buildDefaultHiddenGemsBlock(String destination) {
        String safeDestination = destination == null || destination.isBlank()
                ? "the destination"
                : destination.trim();

        return """
                HIDDEN_GEMS:
                1. %s backstreet cafe - A quiet local stop away from the main tourist route. Local tip: ask for the house dessert and sit where locals gather.
                2. %s hidden viewpoint - A calm angle for golden-hour photos without the crowded main square. Local tip: arrive before sunset and bring comfortable shoes.
                3. %s artisan lane - Small workshops and independent makers with more local character than souvenir streets. Local tip: ask makers which nearby street they personally recommend.
                """.formatted(safeDestination, safeDestination, safeDestination).trim();
    }

    private List<String> fetchTargetedImages(TripRequestContext context, List<String> imageKeywords) {
        Set<String> imageUrls = new LinkedHashSet<>();
        List<String> queries = new ArrayList<>();

        for (String keyword : imageKeywords) {
            if (keyword != null && !keyword.isBlank()) {
                queries.add(context.getDestination() + " " + keyword.trim());
            }
        }

        queries.add(context.getDestination() + " travel landmarks");
        queries.add(context.getDestination() + " cafe street");

        for (String query : queries) {
            List<String> fetchedUrls = new ArrayList<>(pexelsService.fetchImages(query));
            Collections.shuffle(fetchedUrls);
            for (String url : fetchedUrls) {
                imageUrls.add(url);
                if (imageUrls.size() >= 6) {
                    return new ArrayList<>(imageUrls);
                }
            }
        }

        return new ArrayList<>(imageUrls);
    }

    private List<String> extractImageKeywords(String itineraryRaw, TripRequestContext context) {
        List<String> keywords = new ArrayList<>();
        if (itineraryRaw != null) {
            for (String line : itineraryRaw.split("\\R")) {
                String trimmed = line.trim();
                if (trimmed.toUpperCase(Locale.ROOT).startsWith("IMAGE_KEYWORDS:")) {
                    String keywordLine = trimmed.substring("IMAGE_KEYWORDS:".length())
                            .replace("[", "")
                            .replace("]", "")
                            .replace("\"", "");
                    for (String keyword : keywordLine.split(",")) {
                        String cleanKeyword = keyword.trim();
                        if (!cleanKeyword.isBlank()) {
                            keywords.add(cleanKeyword);
                        }
                    }
                }
            }
        }

        if (keywords.isEmpty()) {
            keywords.add(context.getDestination() + " skyline");
            keywords.add(context.getDestination() + " old town");
            keywords.add(context.getDestination() + " local cafe");
            keywords.add(context.getDestination() + " museum");
        }

        return keywords.stream().distinct().limit(4).toList();
    }

    private String removeImageKeywordLine(String itineraryRaw) {
        if (itineraryRaw == null || itineraryRaw.isBlank()) {
            return "";
        }

        List<String> lines = new ArrayList<>();
        for (String line : itineraryRaw.split("\\R")) {
            if (!line.trim().toUpperCase(Locale.ROOT).startsWith("IMAGE_KEYWORDS:")) {
                lines.add(line);
            }
        }

        return String.join("\n", lines).trim();
    }

    private String toImageUrlsJson(List<String> imageUrls) {
        try {
            return objectMapper.writeValueAsString(imageUrls == null ? List.of() : imageUrls);
        } catch (Exception e) {
            System.out.println("[PEXELS_CACHE_JSON_ERROR] Could not serialize image URLs: " + e.getMessage());
            return "[]";
        }
    }

    private List<String> fromImageUrlsJson(String imageUrlsJson) {
        if (imageUrlsJson == null || imageUrlsJson.isBlank()) {
            return List.of();
        }

        try {
            return objectMapper.readValue(imageUrlsJson, new TypeReference<>() {
            });
        } catch (Exception e) {
            System.out.println("[PEXELS_CACHE_JSON_ERROR] Could not parse cached image URLs: " + e.getMessage());
            return List.of();
        }
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
