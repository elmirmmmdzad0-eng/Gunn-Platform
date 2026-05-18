package com.tripgen.api;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class TripCleanupService {

    private static final int CACHE_RETENTION_DAYS = 90;

    private final TripPlanRepository tripPlanRepository;

    public TripCleanupService(TripPlanRepository tripPlanRepository) {
        this.tripPlanRepository = tripPlanRepository;
    }

    @Scheduled(cron = "0 0 0 * * ?", zone = "Asia/Baku")
    public void cleanOldTripPlanCache() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(CACHE_RETENTION_DAYS);
        long oldCacheCount = tripPlanRepository.countByCreatedAtBefore(cutoffDate);

        System.out.println("[DATABASE_CLEANUP] Basladi. Cutoff=" + cutoffDate
                + ", silinecekCacheSayi=" + oldCacheCount);

        tripPlanRepository.deleteByCreatedAtBefore(cutoffDate);

        System.out.println("[DATABASE_CLEANUP] 3 aydan köhnə keşlər təmizləndi. Silinen say: "
                + oldCacheCount);
    }
}
