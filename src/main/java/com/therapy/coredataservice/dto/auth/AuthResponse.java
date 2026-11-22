package com.therapy.coredataservice.dto.auth;

public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        TherapistProfileDto therapist
) {
}
