package com.therapy.coredataservice.dto.internal;

import com.therapy.coredataservice.entity.SpeakerType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.List;

/**
 * Contract 5.2 request DTO for POST /internal/api/v1/sessions/{sessionId}/complete.
 */
public record SessionCompleteRequestDto(
        @NotNull(message = "startedAt is required")
        Instant startedAt,

        @NotNull(message = "endedAt is required")
        Instant endedAt,

        @NotNull(message = "durationSeconds is required")
        @Min(value = 0, message = "durationSeconds must be non-negative")
        Long durationSeconds,

        String aiSummary,

        @NotNull(message = "transcript is required")
        @Valid
        List<TranscriptEntryItemDto> transcript
) {

    public record TranscriptEntryItemDto(
            @NotNull(message = "sequenceNumber is required")
            @Min(value = 1, message = "sequenceNumber must be at least 1")
            Integer sequenceNumber,

            @NotNull(message = "timestamp is required")
            Instant timestamp,

            @NotNull(message = "speaker is required")
            SpeakerType speaker,

            @NotBlank(message = "content is required")
            String content,

            String ttsDirectives,

            String animationTriggerId
    ) {
    }
}
