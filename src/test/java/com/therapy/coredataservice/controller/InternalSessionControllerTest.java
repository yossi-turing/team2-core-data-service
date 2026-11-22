package com.therapy.coredataservice.controller;

import com.therapy.coredataservice.dto.internal.SessionAiContextResponseDto;
import com.therapy.coredataservice.dto.internal.SessionAiContextResponseDto.AnimationContextDto;
import com.therapy.coredataservice.dto.internal.SessionAiContextResponseDto.CharacterContextDto;
import com.therapy.coredataservice.dto.internal.SessionAiContextResponseDto.PatientContextDto;
import com.therapy.coredataservice.dto.internal.SessionAiContextResponseDto.SessionDirectivesDto;
import com.therapy.coredataservice.dto.internal.SessionCompleteRequestDto;
import com.therapy.coredataservice.dto.internal.SessionCompleteResponseDto;
import com.therapy.coredataservice.entity.SessionStatus;
import com.therapy.coredataservice.exception.GlobalExceptionHandler;
import com.therapy.coredataservice.exception.ResourceNotFoundException;
import com.therapy.coredataservice.service.InternalSessionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = InternalSessionController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class InternalSessionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InternalSessionService internalSessionService;

    @Test
    void getSessionContextShouldReturnContract5JsonWithoutPii() throws Exception {
        UUID sessionId = UUID.fromString("77a1b2c3-4567-89ab-cdef-0123456789ab");

        SessionAiContextResponseDto dto = new SessionAiContextResponseDto(
                sessionId,
                SessionStatus.ACTIVE,
                new CharacterContextDto(
                        "char_astro",
                        "אסטרו החוקר מהחלל",
                        "voice_astro_he_01",
                        "Astro3DModel",
                        "אתה אסטרו, חבר סקרן, חם ואמפתי.",
                        List.of(
                                new AnimationContextDto("WAVE_HELLO", "השתמש בתחילת שיחה או בפרידה מהילד"),
                                new AnimationContextDto("IDLE_LISTEN", "השתמש כאשר הדמות מקשיבה או שואלת שאלה רגועה")
                        )
                ),
                new PatientContextDto(
                        "נועם",
                        8,
                        "חרדה חברתית, נטייה להימנעות ממשחקים קבוצתיים בהפסקה",
                        "חלל ואסטרונאוטים, לגו, כלבים",
                        "חיזוק תחושת המסוגלות החברתית ושיתוף רגשות תסכול"
                ),
                new SessionDirectivesDto(
                        "לעודד את נועם לשתף חוויה אחת מההפסקה בבית הספר",
                        "משחקים משותפים בהפסקה, איך מבקשים להצטרף למשחק",
                        "להימנע משאלות על אבא או על מעבר הדירה האחרון"
                )
        );

        when(internalSessionService.getSessionAiContext(sessionId)).thenReturn(dto);

        MvcResult result = mockMvc.perform(get("/internal/api/v1/sessions/{sessionId}/context", sessionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").value(sessionId.toString()))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.character.id").value("char_astro"))
                .andExpect(jsonPath("$.character.name").value("אסטרו החוקר מהחלל"))
                .andExpect(jsonPath("$.character.voiceConfigId").value("voice_astro_he_01"))
                .andExpect(jsonPath("$.character.unityModelAssetId").value("Astro3DModel"))
                .andExpect(jsonPath("$.character.personalityProfilePrompt").value("אתה אסטרו, חבר סקרן, חם ואמפתי."))
                .andExpect(jsonPath("$.character.availableAnimations[0].animationTriggerId").value("WAVE_HELLO"))
                .andExpect(jsonPath("$.character.availableAnimations[0].aiUsageDescription")
                        .value("השתמש בתחילת שיחה או בפרידה מהילד"))
                .andExpect(jsonPath("$.patientContext.displayAlias").value("נועם"))
                .andExpect(jsonPath("$.patientContext.age").value(8))
                .andExpect(jsonPath("$.patientContext.emotionalBackground").isNotEmpty())
                .andExpect(jsonPath("$.patientContext.interestsAndSafeTopics").isNotEmpty())
                .andExpect(jsonPath("$.patientContext.longTermGoals").isNotEmpty())
                .andExpect(jsonPath("$.patientContext.personalDetails").doesNotExist())
                .andExpect(jsonPath("$.patientContext.therapistNotes").doesNotExist())
                .andExpect(jsonPath("$.sessionDirectives.sessionGoals").isNotEmpty())
                .andExpect(jsonPath("$.sessionDirectives.focusTopics").isNotEmpty())
                .andExpect(jsonPath("$.sessionDirectives.sensitiveBoundaries").isNotEmpty())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        assertThat(body).doesNotContain("personalDetails");
        assertThat(body).doesNotContain("therapistNotes");
    }

    @Test
    void getSessionContextShouldReturn404WhenSessionNotFound() throws Exception {
        UUID sessionId = UUID.randomUUID();
        when(internalSessionService.getSessionAiContext(sessionId))
                .thenThrow(new ResourceNotFoundException("Therapy session not found: " + sessionId));

        mockMvc.perform(get("/internal/api/v1/sessions/{sessionId}/context", sessionId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.path").value("/internal/api/v1/sessions/" + sessionId + "/context"));
    }

    @Test
    void completeSessionShouldReturn200WithContract5Response() throws Exception {
        UUID sessionId = UUID.fromString("77a1b2c3-4567-89ab-cdef-0123456789ab");

        when(internalSessionService.completeSession(eq(sessionId), any(SessionCompleteRequestDto.class)))
                .thenReturn(new SessionCompleteResponseDto(sessionId, SessionStatus.COMPLETED, 3));

        String payload = """
                {
                  "startedAt": "2026-11-10T16:00:00Z",
                  "endedAt": "2026-11-10T16:20:30Z",
                  "durationSeconds": 1230,
                  "aiSummary": null,
                  "transcript": [
                    {
                      "sequenceNumber": 1,
                      "timestamp": "2026-11-10T16:00:05Z",
                      "speaker": "CHARACTER",
                      "content": "היי נועם! איזה כיף שנפגשנו היום!",
                      "ttsDirectives": "שמח, נלהב",
                      "animationTriggerId": "WAVE_HELLO"
                    },
                    {
                      "sequenceNumber": 2,
                      "timestamp": "2026-11-10T16:00:19Z",
                      "speaker": "CHILD",
                      "content": "לא ראיתי כוכבים, אבל בניתי חללית חדשה מלגו.",
                      "ttsDirectives": null,
                      "animationTriggerId": null
                    },
                    {
                      "sequenceNumber": 3,
                      "timestamp": "2026-11-10T16:00:25Z",
                      "speaker": "THERAPIST_STEERING",
                      "content": "תחמיא לו על החללית ואז תשאל אם הוא סיפר עליה למישהו מהכיתה בהפסקה",
                      "ttsDirectives": null,
                      "animationTriggerId": null
                    }
                  ]
                }
                """;

        mockMvc.perform(post("/internal/api/v1/sessions/{sessionId}/complete", sessionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").value(sessionId.toString()))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.savedTranscriptCount").value(3));
    }

    @Test
    void completeSessionShouldReturn400WhenValidationFails() throws Exception {
        UUID sessionId = UUID.randomUUID();
        String invalidPayload = """
                {
                  "startedAt": "2026-11-10T16:00:00Z",
                  "endedAt": "2026-11-10T16:20:30Z",
                  "durationSeconds": -5,
                  "transcript": [
                    {
                      "sequenceNumber": 0,
                      "timestamp": "2026-11-10T16:00:05Z",
                      "speaker": "CHILD",
                      "content": "   "
                    }
                  ]
                }
                """;

        mockMvc.perform(post("/internal/api/v1/sessions/{sessionId}/complete", sessionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }
}
