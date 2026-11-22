package com.therapy.coredataservice.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

/**
 * בודק את ה-Header {@code X-Internal-Service-Key} בנתיבי {@code /internal/api/v1/**} (חוזה 5 מול צוותים 3 ו-5).
 * מפתח חסר או שגוי: מחזיר 401 מיד. מפתח נכון: מוגדר Authentication עם ROLE_INTERNAL_SERVICE.
 * <p>
 * לא מוגדר כ-@Component בכוונה, כדי ש-Spring Boot לא ירשום אותו פעמיים כ-Servlet Filter.
 */
public class InternalApiKeyFilter extends OncePerRequestFilter {

    public static final String HEADER_NAME = "X-Internal-Service-Key";
    public static final String ROLE_INTERNAL_SERVICE = "ROLE_INTERNAL_SERVICE";
    static final String INTERNAL_PRINCIPAL = "internal-service";

    private final byte[] expectedKey;
    private final SecurityErrorWriter errorWriter;

    public InternalApiKeyFilter(String expectedKey, SecurityErrorWriter errorWriter) {
        if (expectedKey == null || expectedKey.isBlank()) {
            throw new IllegalStateException("app.security.internal-service-key (INTERNAL_SERVICE_API_KEY) must be set");
        }
        this.expectedKey = expectedKey.getBytes(StandardCharsets.UTF_8);
        this.errorWriter = errorWriter;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith(request.getContextPath() + "/internal/api/v1/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String providedKey = request.getHeader(HEADER_NAME);
        if (providedKey == null || !MessageDigest.isEqual(expectedKey, providedKey.getBytes(StandardCharsets.UTF_8))) {
            errorWriter.write(request, response, HttpStatus.UNAUTHORIZED,
                    "Missing or invalid " + HEADER_NAME + " header");
            return;
        }

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                INTERNAL_PRINCIPAL,
                null,
                List.of(new SimpleGrantedAuthority(ROLE_INTERNAL_SERVICE)));
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        filterChain.doFilter(request, response);
    }
}
