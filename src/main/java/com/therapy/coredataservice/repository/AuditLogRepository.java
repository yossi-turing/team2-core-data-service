package com.therapy.coredataservice.repository;

import com.therapy.coredataservice.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

    List<AuditLog> findAllByTherapistIdOrderByTimestampDesc(UUID therapistId);

    List<AuditLog> findAllByResourceTypeAndResourceIdOrderByTimestampDesc(String resourceType, String resourceId);
}
