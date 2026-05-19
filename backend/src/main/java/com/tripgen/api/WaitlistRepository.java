package com.tripgen.api;

import org.springframework.data.jpa.repository.JpaRepository;

public interface WaitlistRepository extends JpaRepository<WaitlistEntry, Long> {

    boolean existsByEmailIgnoreCase(String email);
}
