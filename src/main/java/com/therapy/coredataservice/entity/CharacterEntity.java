package com.therapy.coredataservice.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Matching AGENT_SYNC_CONTRACT.md Section 4.3 (owned by Issue #4).
 */
@Entity
@Table(name = "characters")
public class CharacterEntity {

    @Id
    @Column(name = "id", nullable = false, length = 100)
    private String id;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "personality_profile_prompt", nullable = false, columnDefinition = "TEXT")
    private String personalityProfilePrompt;

    @Column(name = "voice_config_id", nullable = false, length = 100)
    private String voiceConfigId;

    @Column(name = "unity_model_asset_id", nullable = false, length = 100)
    private String unityModelAssetId;

    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "character_available_animations",
            joinColumns = @JoinColumn(name = "character_id"),
            inverseJoinColumns = @JoinColumn(name = "animation_trigger_id")
    )
    private List<AnimationTrigger> availableAnimations = new ArrayList<>();

    public CharacterEntity() {
    }

    public CharacterEntity(
            String id,
            String name,
            String description,
            String personalityProfilePrompt,
            String voiceConfigId,
            String unityModelAssetId,
            List<AnimationTrigger> availableAnimations
    ) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.personalityProfilePrompt = personalityProfilePrompt;
        this.voiceConfigId = voiceConfigId;
        this.unityModelAssetId = unityModelAssetId;
        this.availableAnimations = availableAnimations == null
                ? new ArrayList<>()
                : new ArrayList<>(availableAnimations);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPersonalityProfilePrompt() {
        return personalityProfilePrompt;
    }

    public void setPersonalityProfilePrompt(String personalityProfilePrompt) {
        this.personalityProfilePrompt = personalityProfilePrompt;
    }

    public String getVoiceConfigId() {
        return voiceConfigId;
    }

    public void setVoiceConfigId(String voiceConfigId) {
        this.voiceConfigId = voiceConfigId;
    }

    public String getUnityModelAssetId() {
        return unityModelAssetId;
    }

    public void setUnityModelAssetId(String unityModelAssetId) {
        this.unityModelAssetId = unityModelAssetId;
    }

    public List<AnimationTrigger> getAvailableAnimations() {
        return availableAnimations;
    }

    public void setAvailableAnimations(List<AnimationTrigger> availableAnimations) {
        this.availableAnimations = availableAnimations == null
                ? new ArrayList<>()
                : new ArrayList<>(availableAnimations);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CharacterEntity that)) {
            return false;
        }
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
