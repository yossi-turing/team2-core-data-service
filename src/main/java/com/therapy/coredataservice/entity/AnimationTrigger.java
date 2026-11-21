package com.therapy.coredataservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.Objects;

/**
 * Matching AGENT_SYNC_CONTRACT.md Section 4.3 (owned by Issue #4).
 */
@Entity
@Table(name = "character_animations")
public class AnimationTrigger {

    @Id
    @Column(name = "animation_trigger_id", nullable = false, length = 100)
    private String animationTriggerId;

    @Column(name = "display_name", nullable = false, length = 255)
    private String displayName;

    @Column(name = "ai_usage_description", nullable = false, columnDefinition = "TEXT")
    private String aiUsageDescription;

    public AnimationTrigger() {
    }

    public AnimationTrigger(String animationTriggerId, String displayName, String aiUsageDescription) {
        this.animationTriggerId = animationTriggerId;
        this.displayName = displayName;
        this.aiUsageDescription = aiUsageDescription;
    }

    public String getAnimationTriggerId() {
        return animationTriggerId;
    }

    public void setAnimationTriggerId(String animationTriggerId) {
        this.animationTriggerId = animationTriggerId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getAiUsageDescription() {
        return aiUsageDescription;
    }

    public void setAiUsageDescription(String aiUsageDescription) {
        this.aiUsageDescription = aiUsageDescription;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AnimationTrigger that)) {
            return false;
        }
        return animationTriggerId != null && Objects.equals(animationTriggerId, that.animationTriggerId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(animationTriggerId);
    }
}
