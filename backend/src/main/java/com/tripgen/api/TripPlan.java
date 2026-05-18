package com.tripgen.api;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "trip_plans",
        indexes = {
                @Index(
                        name = "idx_trip_plan_lookup",
                        columnList = "normalized_destination, days, budget_type"
                )
        }
)
public class TripPlan {

    @Id
    @Column(nullable = false, length = 36)
    private String id;

    @Column(nullable = false)
    private String destination;

    @Column(name = "normalized_destination", nullable = false)
    private String normalizedDestination;

    @Column(nullable = false)
    private int days;

    @Column(name = "budget_type", nullable = false)
    private String budgetType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String itineraryRaw;

    @Column(nullable = false)
    private String source;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public TripPlan() {
    }

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
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

    public String getNormalizedDestination() {
        return normalizedDestination;
    }

    public void setNormalizedDestination(String normalizedDestination) {
        this.normalizedDestination = normalizedDestination;
    }

    public int getDays() {
        return days;
    }

    public void setDays(int days) {
        this.days = days;
    }

    public String getBudgetType() {
        return budgetType;
    }

    public void setBudgetType(String budgetType) {
        this.budgetType = budgetType;
    }

    public String getItineraryRaw() {
        return itineraryRaw;
    }

    public void setItineraryRaw(String itineraryRaw) {
        this.itineraryRaw = itineraryRaw;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
