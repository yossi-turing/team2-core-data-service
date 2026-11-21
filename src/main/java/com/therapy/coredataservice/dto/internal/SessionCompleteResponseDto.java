package com.therapy.coredataservice.dto.internal;

import com.therapy.coredataservice.entity.SessionStatus;

import java.util.UUID;

/**
 * Contract 5.2 response DTO for POST /internal/api/v1/sessions/{sessionId}/complete.
 */
public record SessionCompleteResponseDto(
        UUID sessionId,
        SessionStatus status,
        int savedTranscriptCount
) {
}
