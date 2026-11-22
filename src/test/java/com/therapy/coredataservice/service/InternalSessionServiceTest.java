package com.therapy.coredataservice.service;

import com.therapy.coredataservice.dto.internal.SessionAiContextResponseDto;
import com.therapy.coredataservice.dto.internal.SessionCompleteRequestDto;
import com.therapy.coredataservice.dto.internal.SessionCompleteRequestDto.TranscriptEntryItemDto;
import com.therapy.coredataservice.dto.internal.SessionCompleteResponseDto;
import com.therapy.coredataservice.entity.AnimationTrigger;
import com.therapy.coredataservice.entity.CharacterEntity;
import com.therapy.coredataservice.entity.Patient;
import com.therapy.coredataservice.entity.SessionStatus;
import com.therapy.coredataservice.entity.SpeakerType;
import com.therapy.coredataservice.entity.TherapySession;
import com.therapy.coredataservice.entity.TranscriptEntry;
import com.therapy.coredataservice.exception.ResourceNotFoundException;
import com.therapy.coredataservice.repository.CharacterRepository;
import com.therapy.coredataservice.repository.PatientRepository;
import com.therapy.coredataservice.repository.TherapySessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InternalSessionServiceTest {

    @Mock
    private TherapySessionRepository therapySessionRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private CharacterRepository characterRepository;

    @InjectMocks
    private InternalSessionService internalSessionService;

    private UUID sessionId;
    private UUID patientId;
    private UUID therapistId;
    private TherapySession session;
    private Patient patient;
    private CharacterEntity character;

    @BeforeEach
    void setUp() {
        sessionId = UUID.randomUUID();
        patientId = UUID.randomUUID();
        therapistId = UUID.randomUUID();

        session = new TherapySession();
        session.setId(sessionId);
        session.setPatientId(patientId);
        session.setTherapistId(therapistId);
        session.setCharacterId("char_astro");
        session.setStatus(SessionStatus.CREATED);
        session.setSessionGoals("לעודד את נועם לשתף חוויה אחת מההפסקה בבית הספר");
        session.setFocusTopics("משחקים משותפים בהפסקה, איך מבקשים להצטרף למשחק");
        session.setSensitiveBoundaries("להימנע משאלות על אבא או על מעבר הדירה האחרון");

        patient = new Patient();
        patient.setId(patientId);
        patient.setTherapistId(therapistId);
        patient.setDisplayAlias("נועם");
        patient.setPersonalDetails("נועם כהן, ת.ז 123456789, רחוב הרצל 10 תל אביב, הורים: דני ושרה");
        patient.setAge(8);
        patient.setEmotionalBackground("חרדה חברתית, נטייה להימנעות ממשחקים קבוצתיים בהפסקה");
        patient.setInterestsAndSafeTopics("חלל ואסטרונאוטים, לגו, כלבים");
        patient.setLongTermGoals("חיזוק תחושת המסוגלות החברתית ושיתוף רגשות תסכול");
        patient.setTherapistNotes("הערה פרטית סודית של המטפל - אסור שתגיע ל-AI");

        character = new CharacterEntity(
                "char_astro",
                "אסטרו החוקר מהחלל",
                "דמות סקרנית, חמה ומעודדת",
                "אתה אסטרו, חבר סקרן, חם ואמפתי.",
                "voice_astro_he_01",
                "Astro3DModel",
                List.of(
                        new AnimationTrigger("WAVE_HELLO", "נפנוף לשלום", "השתמש בתחילת שיחה או בפרידה מהילד"),
                        new AnimationTrigger("IDLE_LISTEN", "הקשבה", "השתמש כאשר הדמות מקשיבה או שואלת שאלה רגועה")
                )
        );
    }

    @Test
    void getSessionAiContextShouldActivateSessionAndStripPii() {
        when(therapySessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(characterRepository.findById("char_astro")).thenReturn(Optional.of(character));
        when(therapySessionRepository.save(any(TherapySession.class))).thenAnswer(inv -> inv.getArgument(0));

        SessionAiContextResponseDto response = internalSessionService.getSessionAiContext(sessionId);

        assertThat(response.sessionId()).isEqualTo(sessionId);
        assertThat(response.status()).isEqualTo(SessionStatus.ACTIVE);
        assertThat(session.getStatus()).isEqualTo(SessionStatus.ACTIVE);
        assertThat(session.getStartedAt()).isNotNull();

        assertThat(response.character().id()).isEqualTo("char_astro");
        assertThat(response.character().name()).isEqualTo("אסטרו החוקר מהחלל");
        assertThat(response.character().voiceConfigId()).isEqualTo("voice_astro_he_01");
        assertThat(response.character().unityModelAssetId()).isEqualTo("Astro3DModel");
        assertThat(response.character().personalityProfilePrompt()).isEqualTo("אתה אסטרו, חבר סקרן, חם ואמפתי.");
        assertThat(response.character().availableAnimations()).hasSize(2);
        assertThat(response.character().availableAnimations().get(0).animationTriggerId()).isEqualTo("WAVE_HELLO");
        assertThat(response.character().availableAnimations().get(0).aiUsageDescription())
                .isEqualTo("השתמש בתחילת שיחה או בפרידה מהילד");

        assertThat(response.patientContext().displayAlias()).isEqualTo("נועם");
        assertThat(response.patientContext().age()).isEqualTo(8);
        assertThat(response.patientContext().emotionalBackground()).isEqualTo(patient.getEmotionalBackground());
        assertThat(response.patientContext().interestsAndSafeTopics()).isEqualTo(patient.getInterestsAndSafeTopics());
        assertThat(response.patientContext().longTermGoals()).isEqualTo(patient.getLongTermGoals());

        assertThat(response.sessionDirectives().sessionGoals()).isEqualTo(session.getSessionGoals());
        assertThat(response.sessionDirectives().focusTopics()).isEqualTo(session.getFocusTopics());
        assertThat(response.sessionDirectives().sensitiveBoundaries()).isEqualTo(session.getSensitiveBoundaries());

        verify(therapySessionRepository).save(session);
    }

    @Test
    void getSessionAiContextShouldBeIdempotentWhenAlreadyActive() {
        Instant existingStart = Instant.parse("2026-11-10T16:00:00Z");
        session.setStatus(SessionStatus.ACTIVE);
        session.setStartedAt(existingStart);

        when(therapySessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(characterRepository.findById("char_astro")).thenReturn(Optional.of(character));

        SessionAiContextResponseDto response = internalSessionService.getSessionAiContext(sessionId);

        assertThat(response.status()).isEqualTo(SessionStatus.ACTIVE);
        assertThat(session.getStartedAt()).isEqualTo(existingStart);
        verify(therapySessionRepository, never()).save(any());
    }

    @Test
    void getSessionAiContextShouldThrowWhenSessionAlreadyCompleted() {
        session.setStatus(SessionStatus.COMPLETED);
        when(therapySessionRepository.findById(sessionId)).thenReturn(Optional.of(session));

        assertThatThrownBy(() -> internalSessionService.getSessionAiContext(sessionId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already completed");
    }

    @Test
    void getSessionAiContextShouldThrowNotFoundWhenSessionMissing() {
        when(therapySessionRepository.findById(sessionId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> internalSessionService.getSessionAiContext(sessionId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getSessionAiContextShouldThrowNotFoundWhenPatientMissing() {
        when(therapySessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(patientRepository.findById(patientId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> internalSessionService.getSessionAiContext(sessionId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getSessionAiContextShouldThrowNotFoundWhenCharacterMissing() {
        when(therapySessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(characterRepository.findById("char_astro")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> internalSessionService.getSessionAiContext(sessionId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void completeSessionShouldPersistSortedTranscriptAndMarkCompleted() {
        session.setStatus(SessionStatus.ACTIVE);
        when(therapySessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(therapySessionRepository.save(any(TherapySession.class))).thenAnswer(inv -> inv.getArgument(0));

        Instant startedAt = Instant.parse("2026-11-10T16:00:00Z");
        Instant endedAt = Instant.parse("2026-11-10T16:20:30Z");

        SessionCompleteRequestDto request = new SessionCompleteRequestDto(
                startedAt,
                endedAt,
                1230L,
                "נועם שיתף בהתלהבות על חללית הלגו.",
                List.of(
                        new TranscriptEntryItemDto(
                                3,
                                Instant.parse("2026-11-10T16:00:25Z"),
                                SpeakerType.THERAPIST_STEERING,
                                "תחמיא לו על החללית ותשאל על ההפסקה",
                                null,
                                null
                        ),
                        new TranscriptEntryItemDto(
                                1,
                                Instant.parse("2026-11-10T16:00:05Z"),
                                SpeakerType.CHARACTER,
                                "היי נועם! איזה כיף שנפגשנו היום!",
                                "שמח, נלהב",
                                "WAVE_HELLO"
                        ),
                        new TranscriptEntryItemDto(
                                2,
                                Instant.parse("2026-11-10T16:00:19Z"),
                                SpeakerType.CHILD,
                                "בניתי חללית חדשה מלגו.",
                                null,
                                null
                        )
                )
        );

        SessionCompleteResponseDto response = internalSessionService.completeSession(sessionId, request);

        assertThat(response.sessionId()).isEqualTo(sessionId);
        assertThat(response.status()).isEqualTo(SessionStatus.COMPLETED);
        assertThat(response.savedTranscriptCount()).isEqualTo(3);

        assertThat(session.getStatus()).isEqualTo(SessionStatus.COMPLETED);
        assertThat(session.getStartedAt()).isEqualTo(startedAt);
        assertThat(session.getEndedAt()).isEqualTo(endedAt);
        assertThat(session.getDurationSeconds()).isEqualTo(1230L);
        assertThat(session.getAiSummary()).isEqualTo("נועם שיתף בהתלהבות על חללית הלגו.");
        assertThat(session.getTranscript())
                .extracting(TranscriptEntry::getSequenceNumber)
                .containsExactly(1, 2, 3);
        assertThat(session.getTranscript())
                .extracting(TranscriptEntry::getSpeaker)
                .containsExactly(SpeakerType.CHARACTER, SpeakerType.CHILD, SpeakerType.THERAPIST_STEERING);

        verify(therapySessionRepository).save(session);
    }

    @Test
    void completeSessionShouldRejectEndedAtBeforeStartedAt() {
        session.setStatus(SessionStatus.ACTIVE);
        when(therapySessionRepository.findById(sessionId)).thenReturn(Optional.of(session));

        SessionCompleteRequestDto request = new SessionCompleteRequestDto(
                Instant.parse("2026-11-10T16:20:00Z"),
                Instant.parse("2026-11-10T16:00:00Z"),
                0L,
                null,
                List.of()
        );

        assertThatThrownBy(() -> internalSessionService.completeSession(sessionId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("endedAt");
    }

    @Test
    void completeSessionShouldRejectAlreadyCompletedSession() {
        session.setStatus(SessionStatus.COMPLETED);
        when(therapySessionRepository.findById(sessionId)).thenReturn(Optional.of(session));

        SessionCompleteRequestDto request = new SessionCompleteRequestDto(
                Instant.parse("2026-11-10T16:00:00Z"),
                Instant.parse("2026-11-10T16:20:00Z"),
                1200L,
                null,
                List.of()
        );

        assertThatThrownBy(() -> internalSessionService.completeSession(sessionId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already completed");
    }

    @Test
    void completeSessionShouldThrowNotFoundWhenSessionMissing() {
        when(therapySessionRepository.findById(sessionId)).thenReturn(Optional.empty());

        SessionCompleteRequestDto request = new SessionCompleteRequestDto(
                Instant.parse("2026-11-10T16:00:00Z"),
                Instant.parse("2026-11-10T16:20:00Z"),
                1200L,
                null,
                List.of()
        );

        assertThatThrownBy(() -> internalSessionService.completeSession(sessionId, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
