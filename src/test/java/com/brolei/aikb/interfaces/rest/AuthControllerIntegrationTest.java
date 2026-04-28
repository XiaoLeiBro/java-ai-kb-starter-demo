package com.brolei.aikb.interfaces.rest;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.brolei.aikb.interfaces.dto.auth.LoginRequest;
import com.brolei.aikb.interfaces.dto.auth.RegisterRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
class AuthControllerIntegrationTest {

  @Container
  static PostgreSQLContainer<?> postgres =
      new PostgreSQLContainer<>("postgres:16-alpine")
          .withDatabaseName("ai_kb_test")
          .withUsername("test")
          .withPassword("test");

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
    registry.add("spring.flyway.enabled", () -> "true");
    registry.add("spring.flyway.baseline-on-migrate", () -> "true");
    registry.add("ai-kb.security.jwt.secret", () -> "test-jwt-secret-with-at-least-32-characters");
    registry.add("springdoc.api-docs.enabled", () -> "false");
    registry.add("springdoc.swagger-ui.enabled", () -> "false");
  }

  @Autowired private WebApplicationContext context;

  private final ObjectMapper objectMapper = new ObjectMapper();

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
  }

  @Test
  void registerShouldReturn200() throws Exception {
    RegisterRequest req = new RegisterRequest("alice", "password123", "alice@example.com");

    mockMvc
        .perform(
            post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.id").isNotEmpty())
        .andExpect(jsonPath("$.data.username").value("alice"));
  }

  @Test
  void loginShouldReturn200WithToken() throws Exception {
    RegisterRequest regReq = new RegisterRequest("bob", "password123", null);
    mockMvc.perform(
        post("/api/v1/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(regReq)));

    LoginRequest loginReq = new LoginRequest("bob", "password123");
    mockMvc
        .perform(
            post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginReq)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.token").isNotEmpty())
        .andExpect(jsonPath("$.data.user.username").value("bob"));
  }

  @Test
  void meShouldReturn200WithJwt() throws Exception {
    RegisterRequest regReq = new RegisterRequest("charlie", "password123", null);
    mockMvc.perform(
        post("/api/v1/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(regReq)));

    LoginRequest loginReq = new LoginRequest("charlie", "password123");
    String loginResponse =
        mockMvc
            .perform(
                post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginReq)))
            .andReturn()
            .getResponse()
            .getContentAsString();

    String token = objectMapper.readTree(loginResponse).get("data").get("token").asText();

    mockMvc
        .perform(get("/api/v1/auth/me").header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.username").value("charlie"));
  }

  @Test
  void meShouldReturnUnauthorizedWithoutJwt() throws Exception {
    mockMvc.perform(get("/api/v1/auth/me")).andExpect(status().isUnauthorized());
  }

  @Test
  void logoutShouldReturn204WithJwt() throws Exception {
    RegisterRequest regReq = new RegisterRequest("dave", "password123", null);
    mockMvc.perform(
        post("/api/v1/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(regReq)));

    LoginRequest loginReq = new LoginRequest("dave", "password123");
    String loginResponse =
        mockMvc
            .perform(
                post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginReq)))
            .andReturn()
            .getResponse()
            .getContentAsString();

    String token = objectMapper.readTree(loginResponse).get("data").get("token").asText();

    mockMvc
        .perform(post("/api/v1/auth/logout").header("Authorization", "Bearer " + token))
        .andExpect(status().isNoContent());
  }

  @Test
  void logoutShouldReturnUnauthorizedWithoutJwt() throws Exception {
    mockMvc.perform(post("/api/v1/auth/logout")).andExpect(status().isUnauthorized());
  }

  @Test
  void healthShouldReturn200WithoutAuth() throws Exception {
    mockMvc.perform(get("/api/v1/health")).andExpect(status().isOk());
  }

  @Test
  void duplicateUsernameShouldReturn409() throws Exception {
    RegisterRequest req = new RegisterRequest("eve", "password123", null);

    mockMvc.perform(
        post("/api/v1/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(req)));

    mockMvc
        .perform(
            post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().is(409));
  }

  @Test
  void registerShouldReturn400WhenUsernameContainsInvalidCharacters() throws Exception {
    RegisterRequest req = new RegisterRequest("alice@", "password123", null);

    mockMvc
        .perform(
            post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("VALIDATION_ERROR: Validation failed"));
  }
}
