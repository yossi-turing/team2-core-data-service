package com.therapy.coredataservice.service;

import com.therapy.coredataservice.dto.internal.SessionAiContextResponseDto;
import com.therapy.coredataservice.dto.internal.SessionAiContextResponseDto.AnimationContextDto;
import com.therapy.coredataservice.dto.internal.SessionAiContextResponseDto.CharacterContextDto;
import com.therapy.coredataservice.dto.internal.SessionAiContextResponseDto.PatientContextDto;
import com.therapy.coredataservice.dto.internal.SessionAiContextResponseDto.SessionDirectivesDto;
import com.therapy.coredataservice.dto.internal.SessionCompleteRequestDto;
import com.therapy.coredataservice.dto.internal.SessionCompleteResponseDto;
import com.therapy.coredataservice.entity.CharacterEntity;
import com.therapy.coredataservice.entity.Patient;
import com.therapy.coredataservice.entity.SessionStatus;
import com.therapy.coredataservice.entity.TherapySession;
import com.therapy.coredataservice.entity.TranscriptEntry;
import com.therapy.coredataservice.exception.ResourceNotFoundException;
import com.therapy.coredataservice.repository.CharacterRepository;
import com.therapy.coredataservice.repository.PatientRepository;
import com.therapy.coredataservice.repository.TherapySessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Business logic for Contract 5 (Internal API for Team 3 AI Engine & Team 5 Real-Time Orchestrator).
 * Enforces strict PII stripping when building AI session context and archives completed session transcripts.
 */
@Service
public class InternalSessionService {

    private final TherapySessionRepository therapySessionRepository;
    private final PatientRepository patientRepository;
    private final CharacterRepository characterRepository;

    public InternalSessionService(
            TherapySessionRepository therapySessionRepository,
            PatientRepository patientRepository,
            CharacterRepository characterRepository
    ) {
        this.therapySessionRepository = therapySessionRepository;
        this.patientRepository = patientRepository;
        this.characterRepository = characterRepository;
    }

    @Transactional
    public SessionAiContextResponseDto getSessionAiContext(UUID sessionId) {
        TherapySession session = therapySessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Therapy session not found: " + sessionId));

        if (session.getStatus() == SessionStatus.COMPLETED) {
            throw new IllegalArgumentException("Cannot fetch active AI context for an already completed session: " + sessionId);
        }

        Patient patient = patientRepository.findById(session.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found: " + session.getPatientId()));

        CharacterEntity character = characterRepository.findById(session.getCharacterId())
                .orElseThrow(() -> new ResourceNotFoundException("Character not found: " + session.getCharacterId()));

        TherapySession activeSession = session;
        if (session.getStatus() == SessionStatus.CREATED) {
            session.setStatus(SessionStatus.ACTIVE);
            if (session.getStartedAt() == null) {
                session.setStartedAt(Instant.now());
            }
            activeSession = therapySessionRepository.save(session);
        }

        return mapToAiContextResponse(activeSession, patient, character);
    }

    @Transactional
    public SessionCompleteResponseDto completeSession(UUID sessionId, SessionCompleteRequestDto request) {
        TherapySession session = therapySessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Therapy session not found: " + sessionId));

        if (session.getStatus() == SessionStatus.COMPLETED) {
            throw new IllegalArgumentException("Therapy session is already completed: " + sessionId);
        }

        if (request.endedAt().isBefore(request.startedAt())) {
            throw new IllegalArgumentException("endedAt cannot be before startedAt");
        }

        List<TranscriptEntry> transcriptEntries = request.transcript().stream()
                .sorted(Comparator.comparing(SessionCompleteRequestDto.TranscriptEntryItemDto::sequenceNumber))
                .map(this::toTranscriptEntry)
                .toList();

        session.setStartedAt(request.startedAt());
        session.setEndedAt(request.endedAt());
        session.setDurationSeconds(request.durationSeconds());
        session.setAiSummary(request.aiSummary());
        session.setStatus(SessionStatus.COMPLETED);
        session.setTranscript(transcriptEntries);

        TherapySession savedSession = therapySessionRepository.save(session);

        return new SessionCompleteResponseDto(
                savedSession.getId(),
                savedSession.getStatus(),
                transcriptEntries.size()
        );
    }

    private SessionAiContextResponseDto mapToAiContextResponse(
            TherapySession session,
            Patient patient,
            CharacterEntity character
    ) {
        List<AnimationContextDto> animations = character.getAvailableAnimations() == null
                ? List.of()
                : character.getAvailableAnimations().stream()
                .map(anim -> new AnimationContextDto(
                        anim.getAnimationTriggerId(),
                        anim.getAiUsageDescription()
                ))
                .toList();

        CharacterContextDto characterContext = new CharacterContextDto(
                character.getId(),
                character.getName(),
                character.getVoiceConfigId(),
                character.getUnityModelAssetId(),
                character.getPersonalityProfilePrompt(),
                animations
        );

        // STRICT PII STRIPPING (Contract 5 / AGENTS.md Rule #3):
        // Never include personalDetails or therapistNotes in AI context.
        PatientContextDto patientContext = new PatientContextDto(
                patient.getDisplayAlias(),
                patient.getAge(),
                patient.getEmotionalBackground(),
                patient.getInterestsAndSafeTopics(),
                patient.getLongTermGoals()
        );

        SessionDirectivesDto sessionDirectives = new SessionDirectivesDto(
                session.getSessionGoals(),
                session.getFocusTopics(),
                session.getSensitiveBoundaries()
        );

        return new SessionAiContextResponseDto(
                session.getId(),
                session.getStatus(),
                characterContext,
                patientContext,
                sessionDirectives
        );
    }

    private TranscriptEntry toTranscriptEntry(SessionCompleteRequestDto.TranscriptEntryItemDto item) {
        TranscriptEntry entry = new TranscriptEntry();
        entry.setSequenceNumber(item.sequenceNumber());
        entry.setTimestamp(item.timestamp());
        entry.setSpeaker(item.speaker());
        entry.setContent(item.content());
        entry.setTtsDirectives(item.ttsDirectives());
        entry.setAnimationTriggerId(item.animationTriggerId());
        return entry;
    }
}
