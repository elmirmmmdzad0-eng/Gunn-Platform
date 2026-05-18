package com.tripgen.api;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

public interface TripPlanRepository extends JpaRepository<TripPlan, String> {

    Optional<TripPlan> findFirstByNormalizedDestinationAndDaysAndBudgetTypeAndLanguageCodeOrderByCreatedAtDesc(
            String normalizedDestination,
            int days,
            String budgetType,
            String languageCode
    );

    long countByCreatedAtBefore(LocalDateTime dateTime);

    @Modifying
    @Transactional
    @Query("delete from TripPlan t where t.createdAt < :dateTime")
    void deleteByCreatedAtBefore(@Param("dateTime") LocalDateTime dateTime);
}
