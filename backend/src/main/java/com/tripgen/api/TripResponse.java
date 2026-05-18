package com.tripgen.api;

public class TripResponse {

    private String id;
    private String destination;
    private int days;
    private String itineraryRaw;

    public TripResponse() {
    }

    public TripResponse(String id, String destination, int days, String itineraryRaw) {
        this.id = id;
        this.destination = destination;
        this.days = days;
        this.itineraryRaw = itineraryRaw;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public int getDays() {
        return days;
    }

    public void setDays(int days) {
        this.days = days;
    }

    public String getItineraryRaw() {
        return itineraryRaw;
    }

    public void setItineraryRaw(String itineraryRaw) {
        this.itineraryRaw = itineraryRaw;
    }
}
