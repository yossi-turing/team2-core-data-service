package com.therapy.coredataservice.repository;

import com.therapy.coredataservice.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Minimal stub matching AGENT_SYNC_CONTRACT.md Section 4.2 (owned by Issue #3).
 */
@Repository
public interface PatientRepository extends JpaRepository<Patient, UUID> {
    List<Patient> findAllByTherapistIdOrderByCreatedAtDesc(UUID therapistId);

    Optional<Patient> findByIdAndTherapistId(UUID id, UUID therapistId);
}
