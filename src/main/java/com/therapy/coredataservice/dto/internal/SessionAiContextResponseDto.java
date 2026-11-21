package com.therapy.coredataservice.dto.internal;

import com.therapy.coredataservice.entity.SessionStatus;

import java.util.List;
import java.util.UUID;

/**
 * Contract 5.1 response DTO for GET /internal/api/v1/sessions/{sessionId}/context.
 * Strictly strips PII from patient context (never includes personalDetails or therapistNotes).
 */
public record SessionAiContextResponseDto(
        UUID sessionId,
        SessionStatus status,
        CharacterContextDto character,
        PatientContextDto patientContext,
        SessionDirectivesDto sessionDirectives
) {

    public record CharacterContextDto(
            String id,
            String name,
            String voiceConfigId,
            String unityModelAssetId,
            String personalityProfilePrompt,
            List<AnimationContextDto> availableAnimations
    ) {
    }

    public record AnimationContextDto(
            String animationTriggerId,
            String aiUsageDescription
    ) {
    }

    public record PatientContextDto(
            String displayAlias,
            Integer age,
            String emotionalBackground,
            String interestsAndSafeTopics,
            String longTermGoals
    ) {
    }

    public record SessionDirectivesDto(
            String sessionGoals,
            String focusTopics,
            String sensitiveBoundaries
    ) {
    }
}
