package com.therapy.coredataservice.service;

import com.therapy.coredataservice.dto.auth.AuthResponse;
import com.therapy.coredataservice.dto.auth.LoginRequest;
import com.therapy.coredataservice.dto.auth.RegisterRequest;
import com.therapy.coredataservice.dto.auth.TherapistProfileDto;
import com.therapy.coredataservice.dto.auth.TherapistUpdateRequest;
import com.therapy.coredataservice.entity.Therapist;
import com.therapy.coredataservice.exception.EmailAlreadyRegisteredException;
import com.therapy.coredataservice.exception.InvalidCredentialsException;
import com.therapy.coredataservice.exception.ResourceNotFoundException;
import com.therapy.coredataservice.repository.TherapistRepository;
import com.therapy.coredataservice.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.UUID;

@Service
public class AuthService {

    private static final String TOKEN_TYPE = "Bearer";
    private static final String INVALID_CREDENTIALS = "Invalid email or password";

    private final TherapistRepository therapistRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(TherapistRepository therapistRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.therapistRepository = therapistRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        Therapist therapist = therapistRepository.findByEmail(normalizeEmail(request.email()))
                .orElseThrow(() -> new InvalidCredentialsException(INVALID_CREDENTIALS));
        if (!passwordEncoder.matches(request.password(), therapist.getPasswordHash())) {
            throw new InvalidCredentialsException(INVALID_CREDENTIALS);
        }
        return buildAuthResponse(therapist);
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (therapistRepository.existsByEmail(email)) {
            throw new EmailAlreadyRegisteredException("A therapist with this email is already registered");
        }
        Therapist therapist = new Therapist();
        therapist.setEmail(email);
        therapist.setPasswordHash(passwordEncoder.encode(request.password()));
        therapist.setFullName(request.fullName().trim());
        therapist.setSpecialty(request.specialty());
        return buildAuthResponse(therapistRepository.save(therapist));
    }

    @Transactional(readOnly = true)
    public TherapistProfileDto getProfile(UUID authenticatedTherapistId) {
        return TherapistProfileDto.from(findTherapist(authenticatedTherapistId));
    }

    @Transactional
    public TherapistProfileDto updateProfile(UUID authenticatedTherapistId, TherapistUpdateRequest request) {
        Therapist therapist = findTherapist(authenticatedTherapistId);
        therapist.setFullName(request.fullName().trim());
        therapist.setSpecialty(request.specialty());
        return TherapistProfileDto.from(therapistRepository.save(therapist));
    }

    private Therapist findTherapist(UUID therapistId) {
        return therapistRepository.findById(therapistId)
                .orElseThrow(() -> new ResourceNotFoundException("Therapist not found"));
    }

    private AuthResponse buildAuthResponse(Therapist therapist) {
        return new AuthResponse(
                jwtService.generateToken(therapist),
                TOKEN_TYPE,
                jwtService.getExpirationSeconds(),
                TherapistProfileDto.from(therapist));
    }

    private static String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
