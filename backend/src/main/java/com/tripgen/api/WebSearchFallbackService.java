package com.tripgen.api;

import org.springframework.stereotype.Service;

@Service
public class WebSearchFallbackService {

    public String generateFromStaticSearch(TripRequestContext context) {
        if (context.getLanguageCode().equals("en")) {
            return generateEnglishFallback(context);
        }

        if (context.getLanguageCode().equals("ru")) {
            return generateRussianFallback(context);
        }

        StringBuilder itinerary = new StringBuilder();
        itinerary.append("TripGen Web Search Fallback").append("\n");
        itinerary.append("İstiqamət: ").append(context.getDestination()).append("\n");
        itinerary.append("Büdcə tipi: ").append(context.getBudgetType()).append("\n");
        itinerary.append("Mənbə: Statik axtarış simulyatoru").append("\n\n");

        for (int i = 1; i <= context.getDays(); i++) {
            itinerary.append(i)
                    .append("-ci gün: ")
                    .append(context.getDestination())
                    .append(" üçün mərkəzi görməli yerlər, yerli mətbəx dayanacağı, ictimai nəqliyyatla rahat marşrut və axşam üçün sakit gəzinti planı.")
                    .append("\n");
        }

        itinerary.append("\nQeyd: AI provayderləri müvəqqəti əlçatan olmadı. Sistem xəta verməmək üçün statik axtarış fallback planı qaytardı.");
        return itinerary.toString();
    }

    private String generateEnglishFallback(TripRequestContext context) {
        StringBuilder itinerary = new StringBuilder();
        itinerary.append("TripGen Web Search Fallback").append("\n");
        itinerary.append("Destination: ").append(context.getDestination()).append("\n");
        itinerary.append("Budget type: ").append(context.getBudgetType()).append("\n");
        itinerary.append(context.getLanguageInstruction()).append("\n\n");

        for (int i = 1; i <= context.getDays(); i++) {
            itinerary.append("Day ")
                    .append(i)
                    .append(": Central sights in ")
                    .append(context.getDestination())
                    .append(", local food stop, easy public transport route and a calm evening walk.")
                    .append("\n");
        }

        itinerary.append("\nNote: AI providers were temporarily unavailable, so TripGen returned a static search fallback plan.");
        return itinerary.toString();
    }

    private String generateRussianFallback(TripRequestContext context) {
        StringBuilder itinerary = new StringBuilder();
        itinerary.append("Резервный поиск TripGen").append("\n");
        itinerary.append("Направление: ").append(context.getDestination()).append("\n");
        itinerary.append("Тип бюджета: ").append(context.getBudgetType()).append("\n");
        itinerary.append(context.getLanguageInstruction()).append("\n\n");

        for (int i = 1; i <= context.getDays(); i++) {
            itinerary.append("День ")
                    .append(i)
                    .append(": Главные места в ")
                    .append(context.getDestination())
                    .append(", остановка для местной кухни, удобный маршрут на транспорте и спокойная вечерняя прогулка.")
                    .append("\n");
        }

        itinerary.append("\nПримечание: AI-провайдеры временно недоступны, поэтому TripGen вернул статический резервный план.");
        return itinerary.toString();
    }
}
