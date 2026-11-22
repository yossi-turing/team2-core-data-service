package com.therapy.coredataservice.repository;

import com.therapy.coredataservice.entity.AuditLog;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
class AuditLogRepositoryTest {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Test
    void savesTimestampAutomaticallyAndFindsByTherapistNewestFirst() {
        UUID therapistId = UUID.randomUUID();
        AuditLog older = new AuditLog(therapistId, "READ_PATIENT", "PATIENT", "p1");
        older.setTimestamp(Instant.parse("2026-11-01T10:00:00Z"));
        auditLogRepository.save(older);
        auditLogRepository.save(new AuditLog(therapistId, "UPDATE_PATIENT", "PATIENT", "p1"));
        auditLogRepository.save(new AuditLog(UUID.randomUUID(), "READ_PATIENT", "PATIENT", "p2"));

        List<AuditLog> logs = auditLogRepository.findAllByTherapistIdOrderByTimestampDesc(therapistId);

        assertThat(logs).hasSize(2);
        assertThat(logs).allSatisfy(log -> assertThat(log.getTimestamp()).isNotNull());
        assertThat(logs.get(0).getAction()).isEqualTo("UPDATE_PATIENT");
        assertThat(logs.get(1).getAction()).isEqualTo("READ_PATIENT");
    }
}
