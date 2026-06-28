package com.fashion.store.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fashion.store.dto.AuthDTO.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AuthController integration test — runs against H2 in-memory DB
 * with the full Spring Security filter chain active.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    // Shared state across ordered tests
    static String jwtToken;
    static final String TEST_EMAIL    = "integration@test.com";
    static final String TEST_PASSWORD = "SecurePass1!";
    static final String TEST_NAME     = "Integration User";

    // ——— Register ———

    @Test
    @Order(1)
    @DisplayName("POST /api/auth/register → 201 with token")
    void register_success() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setName(TEST_NAME);
        req.setEmail(TEST_EMAIL);
        req.setPassword(TEST_PASSWORD);

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token", not(emptyOrNullString())))
                .andExpect(jsonPath("$.email").value(TEST_EMAIL))
                .andExpect(jsonPath("$.name").value(TEST_NAME))
                .andExpect(jsonPath("$.role").value("USER"))
                .andReturn();

        String body = result.getResponse().getContentAsString();
        jwtToken = objectMapper.readTree(body).get("token").asText();
        assertThat(jwtToken).isNotBlank();
    }

    @Test
    @Order(2)
    @DisplayName("POST /api/auth/register duplicate email → 400")
    void register_duplicateEmail() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setName(TEST_NAME);
        req.setEmail(TEST_EMAIL);
        req.setPassword(TEST_PASSWORD);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsStringIgnoringCase("already registered")));
    }

    @Test
    @Order(3)
    @DisplayName("POST /api/auth/register missing field → 400 validation error")
    void register_missingName() throws Exception {
        // name is blank
        String payload = """
                {"name": "", "email": "no-name@test.com", "password": "Pass1234!"}
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());
    }

    // ——— Login ———

    @Test
    @Order(4)
    @DisplayName("POST /api/auth/login valid creds → 200 with token")
    void login_success() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setEmail(TEST_EMAIL);
        req.setPassword(TEST_PASSWORD);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", not(emptyOrNullString())))
                .andExpect(jsonPath("$.email").value(TEST_EMAIL));
    }

    @Test
    @Order(5)
    @DisplayName("POST /api/auth/login wrong password → 401")
    void login_wrongPassword() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setEmail(TEST_EMAIL);
        req.setPassword("WrongPass999!");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    // ——— Me (protected endpoint) ———

    @Test
    @Order(6)
    @DisplayName("GET /api/auth/me with valid JWT → 200 with user profile")
    void getMe_authenticated() throws Exception {
        assertThat(jwtToken).as("JWT token must be set by register test").isNotBlank();

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(TEST_EMAIL))
                .andExpect(jsonPath("$.name").value(TEST_NAME))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    @Order(7)
    @DisplayName("GET /api/auth/me without JWT → 401")
    void getMe_unauthenticated() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(8)
    @DisplayName("GET /api/auth/me with tampered token → 401")
    void getMe_tamperedToken() throws Exception {
        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer invalid.token.here"))
                .andExpect(status().isUnauthorized());
    }

    // ——— Public endpoints (no auth needed) ———

    @Test
    @Order(9)
    @DisplayName("GET /api/products (public) → 200")
    void getProducts_public() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @Order(10)
    @DisplayName("GET /api/categories (public) → 200")
    void getCategories_public() throws Exception {
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
