package com.tripgen.api;

import java.util.ArrayList;
import java.util.List;

public class TripResponse {

    private String id;
    private String destination;
    private int days;
    private String itineraryRaw;
    private List<String> imageUrls = new ArrayList<>();

    public TripResponse() {
    }

    public TripResponse(String id, String destination, int days, String itineraryRaw) {
        this(id, destination, days, itineraryRaw, new ArrayList<>());
    }

    public TripResponse(String id, String destination, int days, String itineraryRaw, List<String> imageUrls) {
        this.id = id;
        this.destination = destination;
        this.days = days;
        this.itineraryRaw = itineraryRaw;
        this.imageUrls = imageUrls == null ? new ArrayList<>() : new ArrayList<>(imageUrls);
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

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls == null ? new ArrayList<>() : new ArrayList<>(imageUrls);
    }
}
