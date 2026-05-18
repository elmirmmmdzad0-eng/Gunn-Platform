package com.tripgen.api;

public class TripRequestContext {

    private final String destination;
    private final String normalizedDestination;
    private final int days;
    private final String budgetType;
    private final String languageCode;

    public TripRequestContext(String destination, String normalizedDestination, int days, String budgetType, String languageCode) {
        this.destination = destination;
        this.normalizedDestination = normalizedDestination;
        this.days = days;
        this.budgetType = budgetType;
        this.languageCode = languageCode;
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

    public String getLanguageInstruction() {
        return switch (languageCode) {
            case "en" -> "Please prepare the travel plan completely in English.";
            case "ru" -> "Пожалуйста, подготовьте план путешествия полностью на русском языке.";
            default -> "Zəhmət olmasa, səyahət planını tamamilə az dilində hazırla.";
        };
    }
}
