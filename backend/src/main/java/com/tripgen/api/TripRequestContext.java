package com.tripgen.api;

public class TripRequestContext {

    private final String destination;
    private final String normalizedDestination;
    private final int days;
    private final String budgetType;

    public TripRequestContext(String destination, String normalizedDestination, int days, String budgetType) {
        this.destination = destination;
        this.normalizedDestination = normalizedDestination;
        this.days = days;
        this.budgetType = budgetType;
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
}
