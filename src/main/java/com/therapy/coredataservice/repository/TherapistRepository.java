package com.therapy.coredataservice.repository;

import com.therapy.coredataservice.entity.Therapist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Matches AGENT_SYNC_CONTRACT.md Section 4.1 (owned by Issue #3).
 */
public interface TherapistRepository extends JpaRepository<Therapist, UUID> {
    Optional<Therapist> findByEmail(String email);

    boolean existsByEmail(String email);
}
