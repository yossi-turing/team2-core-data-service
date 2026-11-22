package com.therapy.coredataservice.dto.auth;

import jakarta.validation.constraints.NotBlank;

/**
 * Body of PUT /api/v1/therapists/me. Email and password are not changed here.
 */
public record TherapistUpdateRequest(
        @NotBlank(message = "fullName is required") String fullName,
        String specialty
) {
}
