package com.tripgen.api;

public class TripRequestContext {

    private final String destination;
    private final String normalizedDestination;
    private final int days;
    private final String budgetType;
    private final String languageCode;
    private final String selectedTypes;

    public TripRequestContext(String destination, String normalizedDestination, int days, String budgetType, String languageCode, String selectedTypes) {
        this.destination = destination;
        this.normalizedDestination = normalizedDestination;
        this.days = days;
        this.budgetType = budgetType;
        this.languageCode = languageCode;
        this.selectedTypes = selectedTypes;
    }

    public String getDestination() {
        return destination;
    }

    public String getNormalizedDestination() {
        return normalizedDestination;
    }

    public int getDays() {
        return days;
    }

    public String getBudgetType() {
        return budgetType;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public String getSelectedTypes() {
        return selectedTypes;
    }

    public boolean hasSelectedTypes() {
        return selectedTypes != null && !selectedTypes.isBlank();
    }

    public String getSelectedTypesInstruction() {
        if (!hasSelectedTypes()) {
            return "No specific tourism types were selected. Build a balanced, broadly useful itinerary.";
        }

        return "Selected tourism types: " + selectedTypes
                + ". Use these specific tourism types as the main basis of the plan, and personalize each day in the synergy of these concepts.";
    }

    public String getLanguageInstruction() {
        return switch (languageCode) {
            case "en" -> "Please prepare the travel plan completely in English.";
            case "ru" -> "Пожалуйста, подготовьте план путешествия полностью на русском языке.";
            default -> "Zəhmət olmasa, səyahət planını tamamilə az dilində hazırla.";
        };
    }
}
