package com.therapy.coredataservice.security;

import com.therapy.coredataservice.entity.Therapist;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

/**
 * יצירה ואימות של JWT למטפלים. ה-subject הוא ה-therapistId, ובטוקן נשמר התפקיד ROLE_THERAPIST.
 */
@Service
public class JwtService {

    public static final String ROLE_THERAPIST = "ROLE_THERAPIST";
    static final String CLAIM_ROLE = "role";
    static final String CLAIM_EMAIL = "email";

    private final SecretKey signingKey;
    private final long expirationMs;

    public JwtService(@Value("${app.security.jwt-secret}") String base64Secret,
                      @Value("${app.security.jwt-expiration-ms}") long expirationMs) {
        this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(base64Secret));
        this.expirationMs = expirationMs;
    }

    public String generateToken(Therapist therapist) {
        Date now = new Date();
        return Jwts.builder()
                .subject(therapist.getId().toString())
                .claim(CLAIM_EMAIL, therapist.getEmail())
                .claim(CLAIM_ROLE, ROLE_THERAPIST)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMs))
                .signWith(signingKey)
                .compact();
    }

    /**
     * מחזיר את ה-therapistId אם הטוקן תקין, חתום, לא פג תוקף ומכיל ROLE_THERAPIST. אחרת Optional ריק.
     */
    public Optional<UUID> validateAndExtractTherapistId(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            if (!ROLE_THERAPIST.equals(claims.get(CLAIM_ROLE, String.class))) {
                return Optional.empty();
            }
            return Optional.of(UUID.fromString(claims.getSubject()));
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    /** תוקף הטוקן בשניות, עבור השדה expiresIn בתשובת ה-login. */
    public long getExpirationSeconds() {
        return expirationMs / 1000;
    }
}
