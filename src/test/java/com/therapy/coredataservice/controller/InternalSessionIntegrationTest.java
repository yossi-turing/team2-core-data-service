package com.therapy.coredataservice.controller;

import com.therapy.coredataservice.entity.AnimationTrigger;
import com.therapy.coredataservice.entity.CharacterEntity;
import com.therapy.coredataservice.entity.Patient;
import com.therapy.coredataservice.entity.SessionStatus;
import com.therapy.coredataservice.entity.SpeakerType;
import com.therapy.coredataservice.entity.TherapySession;
import com.therapy.coredataservice.repository.CharacterRepository;
import com.therapy.coredataservice.repository.PatientRepository;
import com.therapy.coredataservice.repository.TherapySessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:internal_session_integration_test;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
class InternalSessionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private CharacterRepository characterRepository;

    @Autowired
    private TherapySessionRepository therapySessionRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    private UUID sessionId;

    @BeforeEach
    void setUp() {
        therapySessionRepository.deleteAll();
        patientRepository.deleteAll();
        characterRepository.deleteAll();

        CharacterEntity astro = new CharacterEntity(
                "char_astro",
                "אסטרו החוקר מהחלל",
                "דמות סקרנית, חמה ומעודדת",
                "אתה אסטרו, חבר סקרן, חם ואמפתי.",
                "voice_astro_he_01",
                "Astro3DModel",
                List.of(
                        new AnimationTrigger("WAVE_HELLO", "נפנוף לשלום", "השתמש בתחילת שיחה או בפרידה מהילד"),
                        new AnimationTrigger("CLAP_HAPPY", "מחיאת כף", "השתמש לשמחה, התלהבות וחיזוק חיובי לילד")
                )
        );
        characterRepository.save(astro);

        UUID therapistId = UUID.randomUUID();
        Patient patient = new Patient();
        patient.setTherapistId(therapistId);
        patient.setDisplayAlias("נועם");
        patient.setPersonalDetails("PII_SECRET_FULL_NAME_NOAM_COHEN_ID_987654321");
        patient.setAge(8);
        patient.setEmotionalBackground("חרדה חברתית בהפסקות");
        patient.setInterestsAndSafeTopics("חלל, לגו, כלבים");
        patient.setLongTermGoals("חיזוק מסוגלות חברתית");
        patient.setTherapistNotes("PRIVATE_THERAPIST_NOTE_DO_NOT_LEAK");
        patient = patientRepository.save(patient);

        TherapySession session = new TherapySession();
        session.setPatientId(patient.getId());
        session.setTherapistId(therapistId);
        session.setCharacterId(astro.getId());
        session.setStatus(SessionStatus.CREATED);
        session.setSessionGoals("לשתף חוויה אחת מההפסקה");
        session.setFocusTopics("משחקים משותפים בהפסקה");
        session.setSensitiveBoundaries("להימנע משאלות על מעבר הדירה");
        session = therapySessionRepository.save(session);

        this.sessionId = session.getId();
    }

    @Test
    void fullInternalSessionLifecycleFromContextFetchToTranscriptArchive() throws Exception {
        // 1. Contract 5.1: Fetch AI context -> transitions CREATED -> ACTIVE and strips all PII
        MvcResult contextResult = mockMvc.perform(get("/internal/api/v1/sessions/{sessionId}/context", sessionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").value(sessionId.toString()))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.character.id").value("char_astro"))
                .andExpect(jsonPath("$.character.availableAnimations.length()").value(2))
                .andExpect(jsonPath("$.patientContext.displayAlias").value("נועם"))
                .andExpect(jsonPath("$.patientContext.age").value(8))
                .andExpect(jsonPath("$.sessionDirectives.sessionGoals").value("לשתף חוויה אחת מההפסקה"))
                .andReturn();

        String responseJson = contextResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(responseJson).doesNotContain("PII_SECRET_FULL_NAME_NOAM_COHEN_ID_987654321");
        assertThat(responseJson).doesNotContain("PRIVATE_THERAPIST_NOTE_DO_NOT_LEAK");
        assertThat(responseJson).doesNotContain("personalDetails");
        assertThat(responseJson).doesNotContain("therapistNotes");

        TherapySession activeSession = therapySessionRepository.findById(sessionId).orElseThrow();
        assertThat(activeSession.getStatus()).isEqualTo(SessionStatus.ACTIVE);
        assertThat(activeSession.getStartedAt()).isNotNull();

        // 2. Contract 5.2: Archive full transcript -> transitions ACTIVE -> COMPLETED
        String completePayload = """
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
                      "content": "בניתי חללית חדשה מלגו.",
                      "ttsDirectives": null,
                      "animationTriggerId": null
                    },
                    {
                      "sequenceNumber": 3,
                      "timestamp": "2026-11-10T16:00:25Z",
                      "speaker": "THERAPIST_STEERING",
                      "content": "תחמיא לו על החללית ותשאל על הכיתה",
                      "ttsDirectives": null,
                      "animationTriggerId": null
                    }
                  ]
                }
                """;

        mockMvc.perform(post("/internal/api/v1/sessions/{sessionId}/complete", sessionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(completePayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").value(sessionId.toString()))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.savedTranscriptCount").value(3));

        // 3. Verify persisted state and full word-for-word transcript in DB
        transactionTemplate.executeWithoutResult(status -> {
            TherapySession completed = therapySessionRepository.findById(sessionId).orElseThrow();
            assertThat(completed.getStatus()).isEqualTo(SessionStatus.COMPLETED);
            assertThat(completed.getDurationSeconds()).isEqualTo(1230L);
            assertThat(completed.getTranscript()).hasSize(3);
            assertThat(completed.getTranscript().get(0).getSpeaker()).isEqualTo(SpeakerType.CHARACTER);
            assertThat(completed.getTranscript().get(0).getAnimationTriggerId()).isEqualTo("WAVE_HELLO");
            assertThat(completed.getTranscript().get(1).getSpeaker()).isEqualTo(SpeakerType.CHILD);
            assertThat(completed.getTranscript().get(2).getSpeaker()).isEqualTo(SpeakerType.THERAPIST_STEERING);
        });
    }
}
