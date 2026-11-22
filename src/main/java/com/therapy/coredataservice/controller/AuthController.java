package com.therapy.coredataservice.controller;

import com.therapy.coredataservice.dto.auth.AuthResponse;
import com.therapy.coredataservice.dto.auth.LoginRequest;
import com.therapy.coredataservice.dto.auth.RegisterRequest;
import com.therapy.coredataservice.dto.auth.TherapistProfileDto;
import com.therapy.coredataservice.dto.auth.TherapistUpdateRequest;
import com.therapy.coredataservice.dto.common.ApiErrorResponse;
import com.therapy.coredataservice.exception.EmailAlreadyRegisteredException;
import com.therapy.coredataservice.exception.InvalidCredentialsException;
import com.therapy.coredataservice.security.SecurityUtils;
import com.therapy.coredataservice.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Contract 1: Auth & Therapist (Team 1)")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/auth/login")
    @SecurityRequirements
    @Operation(summary = "Authenticate therapist and return JWT")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/auth/register")
    @SecurityRequirements
    @Operation(summary = "Register a new therapist account and return JWT")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @GetMapping("/therapists/me")
    @Operation(summary = "Get current authenticated therapist profile")
    public TherapistProfileDto getMyProfile() {
        return authService.getProfile(SecurityUtils.getCurrentTherapistId());
    }

    @PutMapping("/therapists/me")
    @Operation(summary = "Update current authenticated therapist profile")
    public TherapistProfileDto updateMyProfile(@Valid @RequestBody TherapistUpdateRequest request) {
        return authService.updateProfile(SecurityUtils.getCurrentTherapistId(), request);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidCredentials(InvalidCredentialsException ex,
                                                                     HttpServletRequest request) {
        return error(HttpStatus.UNAUTHORIZED, ex.getMessage(), request);
    }

    @ExceptionHandler(EmailAlreadyRegisteredException.class)
    public ResponseEntity<ApiErrorResponse> handleEmailTaken(EmailAlreadyRegisteredException ex,
                                                             HttpServletRequest request) {
        return error(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    private static ResponseEntity<ApiErrorResponse> error(HttpStatus status, String message, HttpServletRequest request) {
        return ResponseEntity.status(status).body(new ApiErrorResponse(
                Instant.now(), status.value(), status.getReasonPhrase(), message, request.getRequestURI()));
    }
}
