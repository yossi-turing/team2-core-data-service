package com.therapy.coredataservice.controller;

import com.therapy.coredataservice.dto.internal.SessionAiContextResponseDto;
import com.therapy.coredataservice.dto.internal.SessionCompleteRequestDto;
import com.therapy.coredataservice.dto.internal.SessionCompleteResponseDto;
import com.therapy.coredataservice.service.InternalSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/internal/api/v1/sessions")
@Tag(name = "Contract 5: Internal AI & Orchestrator API (Teams 3 & 5)")
@SecurityRequirement(name = "internalServiceKey")
public class InternalSessionController {

    private final InternalSessionService internalSessionService;

    public InternalSessionController(InternalSessionService internalSessionService) {
        this.internalSessionService = internalSessionService;
    }

    @GetMapping("/{sessionId}/context")
    @Operation(summary = "Fetch anonymized session context, character personality, and animation triggers at session start")
    public ResponseEntity<SessionAiContextResponseDto> getSessionContext(@PathVariable UUID sessionId) {
        return ResponseEntity.ok(internalSessionService.getSessionAiContext(sessionId));
    }

    @PostMapping("/{sessionId}/complete")
    @Operation(summary = "Archive completed session duration and full word-for-word transcript")
    public ResponseEntity<SessionCompleteResponseDto> completeSession(
            @PathVariable UUID sessionId,
            @Valid @RequestBody SessionCompleteRequestDto request
    ) {
        return ResponseEntity.ok(internalSessionService.completeSession(sessionId, request));
    }
}
