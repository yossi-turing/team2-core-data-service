package com.therapy.coredataservice.service;

import java.util.UUID;

/**
 * תיעוד גישה לתיקי מטופלים ופרוטוקולי שיחה.
 * דוגמה: {@code auditLogService.logAction(therapistId, "READ_PATIENT", "PATIENT", patientId.toString());}
 */
public interface AuditLogService {
    void logAction(UUID therapistId, String action, String resourceType, String resourceId);
}
