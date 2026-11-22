package com.therapy.coredataservice.security;

import com.therapy.coredataservice.exception.ForbiddenAccessException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SecurityUtilsTest {

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void returnsTherapistIdOfAuthenticatedTherapist() {
        UUID therapistId = UUID.randomUUID();
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                therapistId, null, List.of(new SimpleGrantedAuthority(JwtService.ROLE_THERAPIST))));

        assertThat(SecurityUtils.getCurrentTherapistId()).isEqualTo(therapistId);
    }

    @Test
    void throwsWhenNobodyIsAuthenticated() {
        assertThatThrownBy(SecurityUtils::getCurrentTherapistId).isInstanceOf(ForbiddenAccessException.class);
    }

    @Test
    void throwsForInternalServiceCaller() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "internal-service", null, List.of(new SimpleGrantedAuthority(InternalApiKeyFilter.ROLE_INTERNAL_SERVICE))));

        assertThatThrownBy(SecurityUtils::getCurrentTherapistId).isInstanceOf(ForbiddenAccessException.class);
    }
}
