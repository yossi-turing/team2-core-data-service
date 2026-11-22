package com.therapy.coredataservice.service;

import com.therapy.coredataservice.entity.AuditLog;
import com.therapy.coredataservice.repository.AuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogServiceImpl(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    @Transactional
    public void logAction(UUID therapistId, String action, String resourceType, String resourceId) {
        if (action == null || action.isBlank()) {
            throw new IllegalArgumentException("Audit action must not be blank");
        }
        if (resourceType == null || resourceType.isBlank()) {
            throw new IllegalArgumentException("Audit resourceType must not be blank");
        }
        auditLogRepository.save(new AuditLog(therapistId, action, resourceType, resourceId));
    }
}
