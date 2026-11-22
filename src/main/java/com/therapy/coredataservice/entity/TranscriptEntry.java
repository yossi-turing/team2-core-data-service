package com.therapy.coredataservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * Minimal stub matching AGENT_SYNC_CONTRACT.md Section 4.4 (owned by Issue #5).
 * Replace with Issue #5's full encrypted entity when merged to main.
 */
@Entity
@Table(name = "session_transcript_entries")
public class TranscriptEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "sequence_number", nullable = false)
    private Integer sequenceNumber;

    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;

    @Enumerated(EnumType.STRING)
    @Column(name = "speaker", nullable = false, length = 32)
    private SpeakerType speaker;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "tts_directives", length = 255)
    private String ttsDirectives;

    @Column(name = "animation_trigger_id", length = 100)
    private String animationTriggerId;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Integer getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(Integer sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public SpeakerType getSpeaker() {
        return speaker;
    }

    public void setSpeaker(SpeakerType speaker) {
        this.speaker = speaker;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTtsDirectives() {
        return ttsDirectives;
    }

    public void setTtsDirectives(String ttsDirectives) {
        this.ttsDirectives = ttsDirectives;
    }

    public String getAnimationTriggerId() {
        return animationTriggerId;
    }

    public void setAnimationTriggerId(String animationTriggerId) {
        this.animationTriggerId = animationTriggerId;
    }
}
