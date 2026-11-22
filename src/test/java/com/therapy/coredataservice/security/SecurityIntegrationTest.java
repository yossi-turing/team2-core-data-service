package com.therapy.coredataservice.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.therapy.coredataservice.repository.TherapistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:security_integration_test;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "app.security.internal-service-key=test-internal-key"
})
class SecurityIntegrationTest {

    private static final String EMAIL = "therapist@clinic.co.il";
    private static final String PASSWORD = "Password123!";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TherapistRepository therapistRepository;

    @BeforeEach
    void cleanDb() {
        therapistRepository.deleteAll();
    }

    @Test
    void registerLoginAndReadAndUpdateOwnProfile() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("{\"email\":\"Therapist@Clinic.co.il\",\"password\":\"%s\",\"fullName\":\"ד\\\"ר רונית לוי\",\"specialty\":\"חרדה חברתית\"}"
                                .formatted(PASSWORD))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.therapist.email").value(EMAIL));

        assertThat(therapistRepository.findByEmail(EMAIL)).get()
                .satisfies(t -> assertThat(t.getPasswordHash()).isNotEqualTo(PASSWORD).startsWith("$2"));

        String token = login(EMAIL, PASSWORD);

        mockMvc.perform(get("/api/v1/therapists/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(EMAIL))
                .andExpect(jsonPath("$.fullName").value("ד\"ר רונית לוי"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());

        mockMvc.perform(put("/api/v1/therapists/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("{\"fullName\":\"רונית לוי\",\"specialty\":\"קשיי ויסות\"}")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("רונית לוי"))
                .andExpect(jsonPath("$.specialty").value("קשיי ויסות"));
    }

    @Test
    void loginReturnsContractShape() throws Exception {
        register();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("{\"email\":\"%s\",\"password\":\"%s\"}".formatted(EMAIL, PASSWORD))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(3600))
                .andExpect(jsonPath("$.therapist.id").isNotEmpty())
                .andExpect(jsonPath("$.therapist.fullName").isNotEmpty())
                .andExpect(jsonPath("$.therapist.email").value(EMAIL));
    }

    @Test
    void wrongPasswordAndUnknownEmailReturn401WithSameMessage() throws Exception {
        register();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("{\"email\":\"%s\",\"password\":\"wrong-password\"}".formatted(EMAIL))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Invalid email or password"))
                .andExpect(jsonPath("$.path").value("/api/v1/auth/login"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("{\"email\":\"nobody@clinic.co.il\",\"password\":\"whatever1\"}")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    void duplicateRegistrationReturns409() throws Exception {
        register();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("{\"email\":\"%s\",\"password\":\"%s\",\"fullName\":\"שם\"}".formatted(EMAIL, PASSWORD))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void invalidLoginBodyReturns400() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("{\"email\":\"not-an-email\",\"password\":\"\"}")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void protectedTherapistEndpointRequiresValidJwt() throws Exception {
        mockMvc.perform(get("/api/v1/therapists/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.path").value("/api/v1/therapists/me"));

        mockMvc.perform(get("/api/v1/therapists/me").header(HttpHeaders.AUTHORIZATION, "Bearer not.a.valid.token"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/patients"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void internalApiKeyDoesNotGrantAccessToTherapistEndpoints() throws Exception {
        mockMvc.perform(get("/api/v1/therapists/me").header(InternalApiKeyFilter.HEADER_NAME, "test-internal-key"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void internalEndpointsRequireInternalServiceKey() throws Exception {
        String path = "/internal/api/v1/sessions/00000000-0000-0000-0000-000000000001/context";

        mockMvc.perform(get(path))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.path").value(path));

        mockMvc.perform(get(path).header(InternalApiKeyFilter.HEADER_NAME, "wrong-key"))
                .andExpect(status().isUnauthorized());

        // מפתח נכון עובר את שכבת האבטחה. ה-Controller עצמו שייך ל-Issue #9, ולכן כאן מתקבל 404 ולא 401.
        int statusWithKey = mockMvc.perform(get(path).header(InternalApiKeyFilter.HEADER_NAME, "test-internal-key"))
                .andReturn().getResponse().getStatus();
        assertThat(statusWithKey).isNotIn(401, 403);
    }

    @Test
    void therapistJwtDoesNotGrantAccessToInternalEndpoints() throws Exception {
        register();
        String token = login(EMAIL, PASSWORD);

        mockMvc.perform(get("/internal/api/v1/sessions/00000000-0000-0000-0000-000000000001/context")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void swaggerDocsArePublic() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk());
    }

    @Test
    void unknownPathsOutsideTheApiAreDenied() throws Exception {
        int status = mockMvc.perform(get("/actuator/env")).andReturn().getResponse().getStatus();
        assertThat(status).isIn(401, 403);
    }

    private void register() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("{\"email\":\"%s\",\"password\":\"%s\",\"fullName\":\"ד\\\"ר רונית לוי\"}"
                                .formatted(EMAIL, PASSWORD))))
                .andExpect(status().isCreated());
    }

    private String login(String email, String password) throws Exception {
        String body = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("{\"email\":\"%s\",\"password\":\"%s\"}".formatted(email, password))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        JsonNode node = objectMapper.readTree(body);
        return node.get("accessToken").asText();
    }

    private static byte[] json(String raw) {
        return raw.getBytes(StandardCharsets.UTF_8);
    }
}
