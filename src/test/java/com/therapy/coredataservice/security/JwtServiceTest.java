package com.therapy.coredataservice.security;

import com.therapy.coredataservice.entity.Therapist;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import java.util.Base64;
import java.util.Date;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private static final String SECRET =
            "dGVhbTItc2VjcmV0LWtleS1mb3Itand0LXNpZ25pbmctbXVzdC1iZS1hdC1sZWFzdC0yNTYtYml0cw==";
    private static final String OTHER_SECRET =
            Base64.getEncoder().encodeToString("another-secret-key-that-is-at-least-256-bits-long!!".getBytes());

    private final JwtService jwtService = new JwtService(SECRET, 3_600_000L);

    @Test
    void generatedTokenIsValidAndCarriesTherapistId() {
        Therapist therapist = therapist();

        String token = jwtService.generateToken(therapist);

        assertThat(jwtService.validateAndExtractTherapistId(token)).contains(therapist.getId());
        assertThat(jwtService.getExpirationSeconds()).isEqualTo(3600);
    }

    @Test
    void tokenSignedWithDifferentSecretIsRejected() {
        String foreignToken = new JwtService(OTHER_SECRET, 3_600_000L).generateToken(therapist());

        assertThat(jwtService.validateAndExtractTherapistId(foreignToken)).isEmpty();
    }

    @Test
    void expiredTokenIsRejected() {
        String expired = new JwtService(SECRET, -1_000L).generateToken(therapist());

        assertThat(jwtService.validateAndExtractTherapistId(expired)).isEmpty();
    }

    @Test
    void tokenWithoutTherapistRoleIsRejected() {
        String noRole = Jwts.builder()
                .subject(UUID.randomUUID().toString())
                .claim("role", "ROLE_CHILD_SESSION")
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET)))
                .compact();

        assertThat(jwtService.validateAndExtractTherapistId(noRole)).isEmpty();
    }

    @Test
    void garbageAndBlankTokensAreRejected() {
        assertThat(jwtService.validateAndExtractTherapistId("not-a-jwt")).isEmpty();
        assertThat(jwtService.validateAndExtractTherapistId("")).isEmpty();
        assertThat(jwtService.validateAndExtractTherapistId(null)).isEmpty();
    }

    private static Therapist therapist() {
        Therapist therapist = new Therapist();
        therapist.setId(UUID.randomUUID());
        therapist.setEmail("therapist@clinic.co.il");
        therapist.setFullName("ד\"ר רונית לוי");
        return therapist;
    }
}
