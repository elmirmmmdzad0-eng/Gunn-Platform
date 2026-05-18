package com.tripgen.api;

import org.springframework.stereotype.Service;

@Service
public class WebSearchFallbackService {

    public String generateFromStaticSearch(TripRequestContext context) {
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
}
