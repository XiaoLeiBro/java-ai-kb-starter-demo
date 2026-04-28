package com.brolei.aikb.interfaces.rest;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.brolei.aikb.domain.llm.EmbeddingProvider;
import com.brolei.aikb.domain.llm.LlmProvider;
import com.brolei.aikb.interfaces.dto.auth.LoginRequest;
import com.brolei.aikb.interfaces.dto.auth.RegisterRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
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

  private static final AtomicInteger USER_SEQUENCE = new AtomicInteger();

  @Container
  static PostgreSQLContainer<?> postgres =
      new PostgreSQLContainer<>("pgvector/pgvector:pg16")
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

  @Test
  void knowledgeBaseShouldReturnUnauthorizedWithoutJwt() throws Exception {
    mockMvc.perform(get("/api/v1/knowledge-bases")).andExpect(status().isUnauthorized());
  }

  @Test
  void chatShouldReturnUnauthorizedWithoutJwt() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"knowledgeBaseId":"kb-1","question":"年假？","topK":1}
                    """))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void createKnowledgeBaseShouldReturn200WithJwt() throws Exception {
    String token = registerAndLogin("kb_user");

    mockMvc
        .perform(
            post("/api/v1/knowledge-bases")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"name":"公司制度","description":"测试知识库"}
                    """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.id").isNotEmpty())
        .andExpect(jsonPath("$.data.name").value("公司制度"));
  }

  @Test
  void uploadDocumentAndChatShouldReturnAnswerAndReferences() throws Exception {
    String token = registerAndLogin("rag_user");
    String kbId = createKnowledgeBase(token);
    MockMultipartFile file =
        new MockMultipartFile(
            "file", "policy.md", "text/markdown", "年假规则：司龄 1-3 年为 5 天。".getBytes());

    mockMvc
        .perform(
            multipart("/api/v1/knowledge-bases/{id}/documents", kbId)
                .file(file)
                .header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.status").value("READY"))
        .andExpect(jsonPath("$.data.chunkCount").value(1));

    mockMvc
        .perform(
            post("/api/v1/chat")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"knowledgeBaseId":"%s","question":"年假规则是什么？","topK":3}
                    """
                        .formatted(kbId)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.answer").value("fake answer"))
        .andExpect(jsonPath("$.data.references[0].fileName").value("policy.md"));
  }

  @Test
  void chatShouldSearchOnlyInsideRequestedKnowledgeBase() throws Exception {
    String token = registerAndLogin("isolation_user");
    String firstKbId = createKnowledgeBase(token);
    String secondKbId = createKnowledgeBase(token);
    MockMultipartFile firstFile =
        new MockMultipartFile("file", "first.md", "text/markdown", "第一知识库内容".getBytes());
    MockMultipartFile secondFile =
        new MockMultipartFile("file", "second.md", "text/markdown", "第二知识库内容".getBytes());

    mockMvc
        .perform(
            multipart("/api/v1/knowledge-bases/{id}/documents", firstKbId)
                .file(firstFile)
                .header("Authorization", "Bearer " + token))
        .andExpect(status().isOk());
    mockMvc
        .perform(
            multipart("/api/v1/knowledge-bases/{id}/documents", secondKbId)
                .file(secondFile)
                .header("Authorization", "Bearer " + token))
        .andExpect(status().isOk());

    mockMvc
        .perform(
            post("/api/v1/chat")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"knowledgeBaseId":"%s","question":"内容是什么？","topK":5}
                    """
                        .formatted(secondKbId)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.references.length()").value(1))
        .andExpect(jsonPath("$.data.references[0].fileName").value("second.md"));
  }

  @Test
  void uploadUnsupportedFileTypeShouldReturn400() throws Exception {
    String token = registerAndLogin("bad_file_user");
    String kbId = createKnowledgeBase(token);
    MockMultipartFile file =
        new MockMultipartFile("file", "policy.pdf", "application/pdf", "bad".getBytes());

    mockMvc
        .perform(
            multipart("/api/v1/knowledge-bases/{id}/documents", kbId)
                .file(file)
                .header("Authorization", "Bearer " + token))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("UNSUPPORTED_FILE_TYPE: 不支持的文件类型: pdf"));
  }

  @Test
  void otherUserShouldGet404ForKnowledgeBaseDocumentsAndChat() throws Exception {
    String ownerToken = registerAndLogin("owner_user");
    String otherToken = registerAndLogin("other_user");
    String kbId = createKnowledgeBase(ownerToken);

    mockMvc
        .perform(
            get("/api/v1/knowledge-bases/{id}/documents", kbId)
                .header("Authorization", "Bearer " + otherToken))
        .andExpect(status().isNotFound());

    mockMvc
        .perform(
            post("/api/v1/chat")
                .header("Authorization", "Bearer " + otherToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"knowledgeBaseId":"%s","question":"年假？","topK":1}
                    """
                        .formatted(kbId)))
        .andExpect(status().isNotFound());
  }

  @Test
  void chatShouldReturn400WhenQuestionMissing() throws Exception {
    String token = registerAndLogin("chat_validation_user");

    mockMvc
        .perform(
            post("/api/v1/chat")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"knowledgeBaseId":"kb-1","topK":1}
                    """))
        .andExpect(status().isBadRequest());
  }

  private String registerAndLogin(String prefix) throws Exception {
    String username = "usr" + USER_SEQUENCE.incrementAndGet();
    RegisterRequest registerRequest = new RegisterRequest(username, "password123", null);
    mockMvc
        .perform(
            post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
        .andExpect(status().isOk());

    LoginRequest loginRequest = new LoginRequest(username, "password123");
    String response =
        mockMvc
            .perform(
                post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    return objectMapper.readTree(response).get("data").get("token").asText();
  }

  private String createKnowledgeBase(String token) throws Exception {
    String response =
        mockMvc
            .perform(
                post("/api/v1/knowledge-bases")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {"name":"测试知识库","description":"integration test"}
                        """))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    return objectMapper.readTree(response).get("data").get("id").asText();
  }

  @TestConfiguration
  static class FakeAiConfig {

    @Bean
    @Primary
    EmbeddingProvider fakeEmbeddingProvider() {
      return texts -> texts.stream().map(text -> vector()).toList();
    }

    @Bean
    @Primary
    LlmProvider fakeLlmProvider() {
      return (systemPrompt, userMessage) -> "fake answer";
    }

    private static float[] vector() {
      float[] vector = new float[1024];
      vector[0] = 1.0f;
      return vector;
    }
  }
}
