package com.therapy.coredataservice.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Minimal stub matching AGENT_SYNC_CONTRACT.md Section 4.4 (owned by Issue #5).
 * Replace with Issue #5's full encrypted entity when merged to main.
 */
@Entity
@Table(name = "therapy_sessions")
public class TherapySession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "patient_id", nullable = false)
    private UUID patientId;

    @Column(name = "therapist_id", nullable = false)
    private UUID therapistId;

    @Column(name = "character_id", nullable = false, length = 100)
    private String characterId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private SessionStatus status = SessionStatus.CREATED;

    @Column(name = "session_goals", columnDefinition = "TEXT")
    private String sessionGoals;

    @Column(name = "focus_topics", columnDefinition = "TEXT")
    private String focusTopics;

    @Column(name = "sensitive_boundaries", columnDefinition = "TEXT")
    private String sensitiveBoundaries;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "ended_at")
    private Instant endedAt;

    @Column(name = "duration_seconds")
    private Long durationSeconds;

    @Column(name = "therapist_session_notes", columnDefinition = "TEXT")
    private String therapistSessionNotes;

    @Column(name = "ai_summary", columnDefinition = "TEXT")
    private String aiSummary;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    @OrderBy("sequenceNumber ASC")
    private List<TranscriptEntry> transcript = new ArrayList<>();

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getPatientId() {
        return patientId;
    }

    public void setPatientId(UUID patientId) {
        this.patientId = patientId;
    }

    public UUID getTherapistId() {
        return therapistId;
    }

    public void setTherapistId(UUID therapistId) {
        this.therapistId = therapistId;
    }

    public String getCharacterId() {
        return characterId;
    }

    public void setCharacterId(String characterId) {
        this.characterId = characterId;
    }

    public SessionStatus getStatus() {
        return status;
    }

    public void setStatus(SessionStatus status) {
        this.status = status;
    }

    public String getSessionGoals() {
        return sessionGoals;
    }

    public void setSessionGoals(String sessionGoals) {
        this.sessionGoals = sessionGoals;
    }

    public String getFocusTopics() {
        return focusTopics;
    }

    public void setFocusTopics(String focusTopics) {
        this.focusTopics = focusTopics;
    }

    public String getSensitiveBoundaries() {
        return sensitiveBoundaries;
    }

    public void setSensitiveBoundaries(String sensitiveBoundaries) {
        this.sensitiveBoundaries = sensitiveBoundaries;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getEndedAt() {
        return endedAt;
    }

    public void setEndedAt(Instant endedAt) {
        this.endedAt = endedAt;
    }

    public Long getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(Long durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public String getTherapistSessionNotes() {
        return therapistSessionNotes;
    }

    public void setTherapistSessionNotes(String therapistSessionNotes) {
        this.therapistSessionNotes = therapistSessionNotes;
    }

    public String getAiSummary() {
        return aiSummary;
    }

    public void setAiSummary(String aiSummary) {
        this.aiSummary = aiSummary;
    }

    public List<TranscriptEntry> getTranscript() {
        return transcript;
    }

    public void setTranscript(List<TranscriptEntry> transcript) {
        this.transcript.clear();
        if (transcript != null) {
            this.transcript.addAll(transcript);
        }
    }
}
