package com.therapy.coredataservice.security;

import com.therapy.coredataservice.exception.ForbiddenAccessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

/**
 * גישה אחידה למטפל המחובר (AGENT_SYNC_CONTRACT.md סעיף 3.1).
 * ה-therapistId נלקח אך ורק מה-JWT המאומת, לעולם לא מה-Body של הבקשה.
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    /**
     * שולף את ה-UUID של המטפל המחובר מתוך ה-SecurityContextHolder.
     * זורק ForbiddenAccessException אם אין מטפל מאומת.
     */
    public static UUID getCurrentTherapistId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof UUID therapistId)
                || authentication.getAuthorities().stream()
                .noneMatch(a -> JwtService.ROLE_THERAPIST.equals(a.getAuthority()))) {
            throw new ForbiddenAccessException("No authenticated therapist in the current request");
        }
        return therapistId;
    }
}
