package com.therapy.coredataservice.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Minimal stub matching AGENT_SYNC_CONTRACT.md Section 4.2 (owned by Issue #3).
 * Replace with Issue #3's full encrypted entity when merged to main.
 */
@Entity
@Table(name = "patients")
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "therapist_id", nullable = false)
    private UUID therapistId;

    @Column(name = "display_alias", nullable = false)
    private String displayAlias;

    @Column(name = "personal_details", columnDefinition = "TEXT")
    private String personalDetails;

    @Column(name = "age")
    private Integer age;

    @Column(name = "emotional_background", columnDefinition = "TEXT")
    private String emotionalBackground;

    @Column(name = "interests_and_safe_topics", columnDefinition = "TEXT")
    private String interestsAndSafeTopics;

    @Column(name = "long_term_goals", columnDefinition = "TEXT")
    private String longTermGoals;

    @Column(name = "therapist_notes", columnDefinition = "TEXT")
    private String therapistNotes;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "patient_preferred_characters", joinColumns = @JoinColumn(name = "patient_id"))
    @Column(name = "character_id")
    private List<String> preferredCharacterIds = new ArrayList<>();

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        if (this.createdAt == null) {
            this.createdAt = now;
        }
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getTherapistId() {
        return therapistId;
    }

    public void setTherapistId(UUID therapistId) {
        this.therapistId = therapistId;
    }

    public String getDisplayAlias() {
        return displayAlias;
    }

    public void setDisplayAlias(String displayAlias) {
        this.displayAlias = displayAlias;
    }

    public String getPersonalDetails() {
        return personalDetails;
    }

    public void setPersonalDetails(String personalDetails) {
        this.personalDetails = personalDetails;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getEmotionalBackground() {
        return emotionalBackground;
    }

    public void setEmotionalBackground(String emotionalBackground) {
        this.emotionalBackground = emotionalBackground;
    }

    public String getInterestsAndSafeTopics() {
        return interestsAndSafeTopics;
    }

    public void setInterestsAndSafeTopics(String interestsAndSafeTopics) {
        this.interestsAndSafeTopics = interestsAndSafeTopics;
    }

    public String getLongTermGoals() {
        return longTermGoals;
    }

    public void setLongTermGoals(String longTermGoals) {
        this.longTermGoals = longTermGoals;
    }

    public String getTherapistNotes() {
        return therapistNotes;
    }

    public void setTherapistNotes(String therapistNotes) {
        this.therapistNotes = therapistNotes;
    }

    public List<String> getPreferredCharacterIds() {
        return preferredCharacterIds;
    }

    public void setPreferredCharacterIds(List<String> preferredCharacterIds) {
        this.preferredCharacterIds = preferredCharacterIds == null
                ? new ArrayList<>()
                : new ArrayList<>(preferredCharacterIds);
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
