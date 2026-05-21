package com.tripgen.api;

import org.springframework.stereotype.Service;

@Service
public class WebSearchFallbackService {

    public String generateFromStaticSearch(TripRequestContext context) {
        StringBuilder itinerary = new StringBuilder();
        appendHeader(itinerary, context);

        for (int i = 0; i < context.getDays(); i++) {
            appendDay(itinerary, context, i);
        }

        itinerary.append("\n")
                .append(ConcretePlaceCatalog.touristInfrastructureBlock(context))
                .append("\n\n")
                .append(ConcretePlaceCatalog.hiddenGemsBlock(context))
                .append("\n\n")
                .append(ConcretePlaceCatalog.mapPointsLine(context))
                .append("\n")
                .append(imageKeywordsLine(context));

        return itinerary.toString();
    }

    private void appendHeader(StringBuilder itinerary, TripRequestContext context) {
        if (context.getLanguageCode().equals("ru")) {
            itinerary.append("Plan GUNN\n");
            itinerary.append("Napravlenie: ").append(context.getDestination()).append("\n");
            itinerary.append("Tip byudzheta: ").append(context.getBudgetType()).append("\n");
        } else if (context.getLanguageCode().equals("en")) {
            itinerary.append("GUNN Travel Plan\n");
            itinerary.append("Destination: ").append(context.getDestination()).append("\n");
            itinerary.append("Budget type: ").append(context.getBudgetType()).append("\n");
        } else {
            itinerary.append("GUNN Seyahat Plani\n");
            itinerary.append("Istiqamet: ").append(context.getDestination()).append("\n");
            itinerary.append("Budce tipi: ").append(context.getBudgetType()).append("\n");
        }

        if (context.hasSelectedTypes()) {
            itinerary.append("Selected tourism styles: ").append(context.getSelectedTypes()).append("\n");
        }

        itinerary.append(context.getLanguageInstruction()).append("\n\n");
    }

    private void appendDay(StringBuilder itinerary, TripRequestContext context, int zeroBasedDay) {
        if (context.getLanguageCode().equals("ru")) {
            itinerary.append("Den ");
        } else if (context.getLanguageCode().equals("en")) {
            itinerary.append("Day ");
        } else {
            itinerary.append(zeroBasedDay + 1).append("-ci gun: ");
            itinerary.append(ConcretePlaceCatalog.dayLine(context, zeroBasedDay));
            appendStyleNote(itinerary, context);
            itinerary.append("\n");
            return;
        }

        itinerary.append(zeroBasedDay + 1)
                .append(": ")
                .append(ConcretePlaceCatalog.dayLine(context, zeroBasedDay));
        appendStyleNote(itinerary, context);
        itinerary.append("\n");
    }

    private void appendStyleNote(StringBuilder itinerary, TripRequestContext context) {
        if (context.hasSelectedTypes()) {
            itinerary.append(" Style focus: ").append(context.getSelectedTypes()).append(".");
        }
    }

    private String imageKeywordsLine(TripRequestContext context) {
        String destination = context.getDestination();
        return "IMAGE_KEYWORDS: " + destination + " landmark, "
                + destination + " hotel, "
                + destination + " museum, "
                + destination + " shopping";
    }
}
