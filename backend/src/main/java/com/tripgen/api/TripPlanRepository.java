package com.tripgen.api;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TripPlanRepository extends JpaRepository<TripPlan, String> {

    Optional<TripPlan> findFirstByNormalizedDestinationAndDaysAndBudgetTypeOrderByCreatedAtDesc(
            String normalizedDestination,
            int days,
            String budgetType
    );
}
