# רשימת ה-Issues וחלוקת המשימות – צוות 2 (Core CRUD & Data Service)

מסמך זה מרכז את כלל ה-Issues לפיתוח במאגר של **צוות 2**.
כל Issue תוכנן כך שהוא נוגע בקבצים מוגדרים מראש כדי למנוע התנגשויות קוד (Merge Conflicts) בין חברי הצוות.

### תפקיד ראש הצוות – יוסי גלר (`@yossi-turing`)
* **ארכיטקטורה ותשתית הרפו:** הגדרת מבנה השכבות, `AGENTS.md`, `docker-compose.yml`, ותקני הפיתוח של הצוות.
* **תיאום וסגירת חוזי API בין-צוותיים:** ניהול הממשקים מול ראשי צוותים 1, 3, 4 ו-5 ודיווח התקדמות לשי.
* **ניהול משימות, Code Review ואינטגרציה:** הקצאת ה-Issues למפתחים, מעבר ואישור Pull Requests (וידוא עמידה בחוזי ה-JSON, בהצפנת ה-AES-256 ובסינון ה-PII מול ה-AI) ואינטגרציה של כל חלקי השרת.

---

## חלוקת ה-Issues בין מפתחי הצוות

| מס' Issue | כותרת ה-Issue | שם ענף (Branch) | מפתח אחראי (GitHub) |
| :--- | :--- | :--- | :--- |
| **Issue #1** | תשתית הצפנת שדות ב-DB (AES-256-GCM) ו-Audit Log | `feat/issue-1-crypto-and-audit` | **איתמר אהרונוב** (`@eitamr717`) ✅ |
| **Issue #2** | אבטחה, התחברות מטפל (JWT) ואימות מפתח פנימי (Spring Security) | `feat/issue-2-security-jwt` | **איתמר אהרונוב** (`@eitamr717`) ✅ |
| **Issue #3** | ישויות Hibernate ו-Repositories עבור מטפלים ומטופלים (השלמת הצפנת `@Convert` וטסטים) | `feat/issue-3-patient-entities` | **איתמר אהרונוב** (`@eitamr717`) |
| **Issue #4** | ישויות Hibernate, Repositories ו-Data Seeder עבור קטלוג דמויות ואנימציות | `feat/issue-4-character-catalog-entities` | **אוהד סבג** (`@ohadjobs2`) ✅ |
| **Issue #5** | ישויות Hibernate ו-Repositories עבור סשנים ופרוטוקול שיחה (השלמת הצפנת `@Convert` וטסטים) | `feat/issue-5-session-transcript-entities` | **איתמר אהרונוב** (`@eitamr717`) |
| **Issue #6** | מימוש REST API לניהול כרטיסי מטופלים (חוזה 1 מול צוות 1) | `feat/issue-6-patient-crud-api` | **אליה גבאי** (`@EliaGabay`) |
| **Issue #7** | מימוש REST API לקטלוג הדמויות והאנימציות (חוזה 1 מול צוות 1) | `feat/issue-7-character-catalog-api` | **אליה גבאי** (`@EliaGabay`) |
| **Issue #8** | מימוש REST API לפתיחת סשן והיסטוריית מפגשים למטפל (חוזה 1 מול צוות 1) | `feat/issue-8-therapist-sessions-api` | **אליה גבאי** (`@EliaGabay`) |
| **Issue #9** | מימוש Internal REST API מול צוות 3 וצוות 5 – שליפת Context ושמירת פרוטוקול (חוזה 5) | `feat/issue-9-internal-ai-orchestrator-api` | **אוהד סבג** (`@ohadjobs2`) ✅ |
| **Issue #14** | הכנת השרת לפרודקשן (`Spring Boot + PostgreSQL Standalone`: `Dockerfile`, `${PORT:8080}`, `docker-compose.yml`, `.env.example`) | `feat/issue-10-production-deployment` | **אוהד סבג** (`@ohadjobs2`) |

---

## פירוט מלא של כל Issue (דרישות וקבצים למימוש)

---

### 🔒 Issue #1: תשתית הצפנת שדות ב-DB (AES-256-GCM) ו-Audit Log
* **מפתח אחראי:** איתמר אהרונוב (`@eitamr717`)
* **ענף:** `feat/issue-1-crypto-and-audit`
* **תיאור המשימה:**
  מימוש מנגנון ההצפנה השקוף ברמת Hibernate עבור שדות רפואיים ורגישים במנוחה (At Rest), ומימוש מנגנון תיעוד הגישה (Audit Log) לפי סעיף 4 באפיון.
* **קבצים ליצירה/עריכה:**
  * `crypto/CryptoService.java` – הצפנה ופיענוח באמצעות `AES/GCM/NoPadding` (עם IV אקראי של 12 בייטים ששמור יחד עם ה-Ciphertext ב-Base64) על בסיס מפתח מ-`application.yml` (`app.security.encryption-key`).
  * `crypto/CryptoAttributeConverter.java` – מממש `AttributeConverter<String, String>` של JPA.
  * `entity/AuditLog.java` + `repository/AuditLogRepository.java` + `service/AuditLogService.java`.
* **קריטריונים להצלחה (Acceptance Criteria):**
  * טסט יחידה (`CryptoAttributeConverterTest`) שמוודא שטקסט בעברית מוצפן לטקסט לא קריא ב-DB ומפוענח חזרה במדויק.

---

### 🛡️ Issue #2: אבטחה, התחברות מטפל (JWT) ואימות מפתח פנימי (Spring Security)
* **מפתח אחראי:** איתמר אהרונוב (`@eitamr717`)
* **ענף:** `feat/issue-2-security-jwt`
* **תיאור המשימה:**
  הקמת מערך האבטחה וההרשאות (RBAC) המפריד הרמטית בין ממשק המטפל (`/api/v1/**`), הממשק הפנימי לסרוויסים (`/internal/api/v1/**`), וממשק הילד (שאינו מורשה לגשת כלל לשרת זה).
* **קבצים ליצירה/עריכה:**
  * `security/JwtService.java` – יצירה ואימות של JWT הכולל `therapistId` ו-`ROLE_THERAPIST`.
  * `security/JwtAuthenticationFilter.java` – פילטר שקורא את `Authorization: Bearer <token>` עבור נתיבי `/api/v1/**`.
  * `security/InternalApiKeyFilter.java` – פילטר שבודק את ה-Header `X-Internal-Service-Key` עבור נתיבי `/internal/api/v1/**` (מול צוותים 3 ו-5).
  * `security/SecurityConfig.java` – הגדרת `SecurityFilterChain`, `BCryptPasswordEncoder`, ופתיחת נתיבי Swagger ו-`/api/v1/auth/**`.
  * `controller/AuthController.java` + `dto/auth/*` – מימוש `POST /api/v1/auth/login`, `POST /api/v1/auth/register` ו-`GET / PUT /api/v1/therapists/me`.

---

### 🧒 Issue #3: ישויות Hibernate ו-Repositories עבור מטפלים ומטופלים
* **מפתח אחראי:** משה אנידג'ר
* **ענף:** `feat/issue-3-patient-entities`
* **תיאור המשימה:**
  מימוש ישויות ה-JPA וה-Repositories עבור מטפלים (`Therapist`) ומטופלים (`Patient`) בהתאם לסעיפים 3.1 ו-3.2 במסמך האפיון.
* **קבצים ליצירה/עריכה:**
  * `entity/Therapist.java` + `repository/TherapistRepository.java` (`findByEmail`).
  * `entity/Patient.java` – כולל כל השדות באפיון (`id`, `therapistId`, `displayAlias`, `personalDetails`, `age`, `emotionalBackground`, `interestsAndSafeTopics`, `longTermGoals`, `therapistNotes`, `preferredCharacterIds`) עם `@Convert(converter = CryptoAttributeConverter.class)` על השדות הרגישים.
  * `repository/PatientRepository.java` – מתודות `findAllByTherapistIdOrderByCreatedAtDesc(UUID therapistId)` ו-`findByIdAndTherapistId(UUID id, UUID therapistId)`.

---

### 🎭 Issue #4: ישויות Hibernate, Repositories ו-Data Seeder עבור קטלוג דמויות ואנימציות
* **מפתח אחראי:** אוהד סבג (`@ohadjobs2`)
* **ענף:** `feat/issue-4-character-catalog-entities`
* **תיאור המשימה:**
  מימוש ישויות קטלוג הדמויות (`CharacterEntity`) ובנק האנימציות (`AnimationTrigger`) לפי סעיף 3.3 באפיון, וכתיבת `DataSeeder` שטוען דמויות ואנימציות התחלתיות בעליית השרת.
* **קבצים ליצירה/עריכה:**
  * `entity/CharacterEntity.java` – שדות: `id` (למשל `"char_astro"`), `name`, `description`, `personalityProfilePrompt`, `voiceConfigId`, `unityModelAssetId`, וקשר `@OneToMany` לרשימת האנימציות.
  * `entity/AnimationTrigger.java` – שדות: `animationTriggerId` (`WAVE_HELLO`, `IDLE_LISTEN`, `THINKING`, `CLAP_HAPPY`, `EMPATHIC_NOD`, `HUG_GESTURE`), `displayName`, `aiUsageDescription`.
  * `repository/CharacterRepository.java` + `repository/AnimationTriggerRepository.java`.
  * `config/CharacterDataSeeder.java` – `CommandLineRunner` שטוען דמויות ובנק אנימציות ל-DB אם הטבלה ריקה.

---

### 💬 Issue #5: ישויות Hibernate ו-Repositories עבור סשנים ופרוטוקול שיחה (Transcript)
* **מפתח אחראי:** משה אנידג'ר
* **ענף:** `feat/issue-5-session-transcript-entities`
* **תיאור המשימה:**
  מימוש ישות המפגש הטיפולי (`TherapySession`) וישות שורות התמליל (`TranscriptEntry`) לפי סעיף 3.4 באפיון.
* **קבצים ליצירה/עריכה:**
  * `entity/SessionStatus.java` (`CREATED`, `ACTIVE`, `COMPLETED`) ו-`entity/SpeakerType.java` (`CHILD`, `CHARACTER`, `THERAPIST_STEERING`).
  * `entity/TherapySession.java` – כולל הגדרות פתיחת הסשן המוצפנות (`sessionGoals`, `focusTopics`, `sensitiveBoundaries`), זמני התחלה/סיום/משך, `therapistSessionNotes`, ורשימת ה-`TranscriptEntry`.
  * `entity/TranscriptEntry.java` – כולל `sequenceNumber`, `timestamp`, `speaker`, `content` (מוצפן), `ttsDirectives`, `animationTriggerId`.
  * `repository/TherapySessionRepository.java` (`findAllByPatientIdAndTherapistIdOrderByStartedAtDesc`, `findByIdAndTherapistId`).

---

### 📋 Issue #6: מימוש REST API לניהול כרטיסי מטופלים (חוזה 1 מול צוות 1)
* **מפתח אחראי:** אליה גבאי (`@EliaGabay`)
* **ענף:** `feat/issue-6-patient-crud-api`
* **תיאור המשימה:**
  מימוש ה-DTOs, ה-Service וה-Controller עבור ניהול כרטיסי מטופלים מול ממשק המטפל של צוות 1 (סעיף 5 – חוזה 1.2 באפיון).
* **קבצים ליצירה/עריכה:**
  * `dto/patient/PatientCreateUpdateDto.java`, `dto/patient/PatientResponseDto.java`, `dto/patient/PatientSummaryDto.java`.
  * `service/PatientService.java` – אכיפת בעלות מטפל (`therapistId` מה-JWT) ורישום פעולות ב-`AuditLogService`.
  * `controller/PatientController.java`:
    * `GET /api/v1/patients`
    * `POST /api/v1/patients`
    * `GET /api/v1/patients/{patientId}`
    * `PUT /api/v1/patients/{patientId}`

---

### 🎨 Issue #7: מימוש REST API לקטלוג הדמויות והאנימציות (חוזה 1 מול צוות 1)
* **מפתח אחראי:** אליה גבאי (`@EliaGabay`)
* **ענף:** `feat/issue-7-character-catalog-api`
* **תיאור המשימה:**
  מימוש נקודות הקצה לשליפת קטלוג הדמויות ובנק האנימציות שלהן עבור מסך בחירת הדמות בפתיחת סשן (חוזה 1.3 באפיון).
* **קבצים ליצירה/עריכה:**
  * `dto/character/CharacterDto.java`, `dto/character/AnimationTriggerDto.java`.
  * `service/CharacterService.java`.
  * `controller/CharacterController.java` (`GET /api/v1/characters` ו-`GET /api/v1/characters/{characterId}`).

---

### 🗂️ Issue #8: מימוש REST API לפתיחת סשן והיסטוריית מפגשים למטפל (חוזה 1 מול צוות 1)
* **מפתח אחראי:** אליה גבאי (`@EliaGabay`)
* **ענף:** `feat/issue-8-therapist-sessions-api`
* **תיאור המשימה:**
  מימוש נקודות הקצה למטפל עבור פתיחת סשן חדש, צפייה בהיסטוריית המפגשים הכרונולוגית של מטופל, צפייה בפרוטוקול שיחה מלא, ועדכון הערות מטפל (חוזה 1.4 באפיון).
* **קבצים ליצירה/עריכה:**
  * `dto/session/SessionCreateRequestDto.java`, `dto/session/SessionResponseDto.java`, `dto/session/SessionSummaryDto.java`, `dto/session/SessionNotesPatchDto.java`, `dto/session/TranscriptEntryDto.java`.
  * `service/SessionService.java`.
  * `controller/SessionController.java`:
    * `POST /api/v1/sessions`
    * `GET /api/v1/patients/{patientId}/sessions`
    * `GET /api/v1/sessions/{sessionId}`
    * `PATCH /api/v1/sessions/{sessionId}/notes`

---

### 🤖 Issue #9: מימוש Internal REST API מול צוות 3 וצוות 5 – שליפת Context ושמירת פרוטוקול (חוזה 5)
* **מפתח אחראי:** אוהד סבג (`@ohadjobs2`)
* **ענף:** `feat/issue-9-internal-ai-orchestrator-api`
* **תיאור המשימה:**
  מימוש חוזה 5 (סעיפים 5.1 ו-5.2 באפיון) מול מנוע ה-AI (צוות 3) ומתזמר זמן האמת (צוות 5):
  1. `GET /internal/api/v1/sessions/{sessionId}/context` – מחזיר את פרופיל הדמות + בנק האנימציות, רקע המטופל **ללא פרטים מזהים (PII Stripping – השמטת `personalDetails` ו-`therapistNotes`!)**, והנחיות פתיחת הסשן, ומעביר את סטטוס הסשן ל-`ACTIVE`.
  2. `POST /internal/api/v1/sessions/{sessionId}/complete` – שומר את תמליל השיחה המלא מילה-במילה (`transcript`), זמני התחלה/סיום ומשך המפגש, ומעביר את הסשן ל-`COMPLETED`.
* **קבצים ליצירה/עריכה:**
  * `dto/internal/SessionAiContextResponseDto.java`, `dto/internal/SessionCompleteRequestDto.java`, `dto/internal/SessionCompleteResponseDto.java`.
  * `controller/InternalSessionController.java`.
