package com.therapy.coredataservice.service;

import com.therapy.coredataservice.entity.AuditLog;
import com.therapy.coredataservice.repository.AuditLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceImplTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditLogServiceImpl auditLogService;

    @Test
    void logActionSavesAuditRecordWithAllFields() {
        UUID therapistId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();

        auditLogService.logAction(therapistId, "READ_PATIENT", "PATIENT", patientId.toString());

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());
        AuditLog saved = captor.getValue();
        assertThat(saved.getTherapistId()).isEqualTo(therapistId);
        assertThat(saved.getAction()).isEqualTo("READ_PATIENT");
        assertThat(saved.getResourceType()).isEqualTo("PATIENT");
        assertThat(saved.getResourceId()).isEqualTo(patientId.toString());
    }

    @Test
    void blankActionIsRejected() {
        assertThatThrownBy(() -> auditLogService.logAction(UUID.randomUUID(), " ", "PATIENT", "1"))
                .isInstanceOf(IllegalArgumentException.class);
        verify(auditLogRepository, never()).save(any());
    }
}
