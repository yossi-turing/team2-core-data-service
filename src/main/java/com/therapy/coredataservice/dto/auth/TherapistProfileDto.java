package com.therapy.coredataservice.dto.auth;

import com.therapy.coredataservice.entity.Therapist;

import java.util.UUID;

public record TherapistProfileDto(
        UUID id,
        String fullName,
        String email,
        String specialty
) {
    public static TherapistProfileDto from(Therapist therapist) {
        return new TherapistProfileDto(
                therapist.getId(),
                therapist.getFullName(),
                therapist.getEmail(),
                therapist.getSpecialty()
        );
    }
}
