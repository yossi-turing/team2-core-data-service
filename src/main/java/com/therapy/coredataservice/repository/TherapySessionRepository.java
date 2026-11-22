package com.therapy.coredataservice.repository;

import com.therapy.coredataservice.entity.TherapySession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Minimal stub matching AGENT_SYNC_CONTRACT.md Section 4.4 (owned by Issue #5).
 */
@Repository
public interface TherapySessionRepository extends JpaRepository<TherapySession, UUID> {
    List<TherapySession> findAllByPatientIdAndTherapistIdOrderByStartedAtDesc(UUID patientId, UUID therapistId);

    Optional<TherapySession> findByIdAndTherapistId(UUID id, UUID therapistId);
}
