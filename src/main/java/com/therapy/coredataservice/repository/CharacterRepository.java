package com.therapy.coredataservice.repository;

import com.therapy.coredataservice.entity.CharacterEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Matching AGENT_SYNC_CONTRACT.md Section 4.3 (owned by Issue #4).
 */
@Repository
public interface CharacterRepository extends JpaRepository<CharacterEntity, String> {
}
