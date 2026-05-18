package com.tripgen.api;

public interface TripPlanProvider {

    String getProviderName();

    String generate(TripRequestContext context);
}
