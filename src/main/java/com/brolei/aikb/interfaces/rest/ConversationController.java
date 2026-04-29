package com.brolei.aikb.interfaces.rest;

import com.brolei.aikb.application.chat.ConversationApplicationService;
import com.brolei.aikb.common.config.AiKbProperties;
import com.brolei.aikb.domain.chat.model.Conversation;
import com.brolei.aikb.domain.chat.model.ConversationId;
import com.brolei.aikb.domain.chat.model.Message;
import com.brolei.aikb.domain.knowledge.model.KnowledgeBaseId;
import com.brolei.aikb.domain.user.model.UserId;
import com.brolei.aikb.interfaces.dto.ApiResult;
import com.brolei.aikb.interfaces.dto.chat.ConversationResponse;
import com.brolei.aikb.interfaces.dto.chat.CreateConversationRequest;
import com.brolei.aikb.interfaces.dto.chat.MessageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 对话会话相关接口的 REST 控制器. */
@Tag(name = "对话会话", description = "创建、查询、归档对话会话，并查看会话消息历史")
@RestController
@RequestMapping("/api/v1/conversations")
public class ConversationController {

  private final ConversationApplicationService conversationApplicationService;
  private final AiKbProperties aiKbProperties;

  public ConversationController(
      ConversationApplicationService conversationApplicationService,
      AiKbProperties aiKbProperties) {
    this.conversationApplicationService = conversationApplicationService;
    this.aiKbProperties = aiKbProperties;
  }

  /** 创建对话会话. */
  @Operation(summary = "创建对话会话", description = "在指定知识库下创建一个连续问答会话。")
  @PostMapping
  public ResponseEntity<ApiResult<ConversationResponse>> createConversation(
      Authentication authentication, @Valid @RequestBody CreateConversationRequest request) {
    UserId userId = (UserId) authentication.getPrincipal();
    KnowledgeBaseId kbId = KnowledgeBaseId.of(request.knowledgeBaseId());
    Conversation conversation =
        conversationApplicationService.createConversation(userId, kbId, request.title());
    URI location = URI.create("/api/v1/conversations/" + conversation.id().value());
    return ResponseEntity.created(location)
        .body(ApiResult.ok(ConversationResponse.from(conversation)));
  }

  /** 查询我的对话会话列表. */
  @Operation(summary = "查询会话列表", description = "查询当前用户的会话列表，可按知识库 ID 过滤。")
  @GetMapping
  public ResponseEntity<ApiResult<List<ConversationResponse>>> listConversations(
      Authentication authentication,
      @Parameter(description = "知识库 ID，不传则查询全部会话", example = "4c9f0d1a-0a3a-4c48-bb6a-7c8b69e1b001")
          @RequestParam(required = false)
          String knowledgeBaseId) {
    UserId userId = (UserId) authentication.getPrincipal();
    KnowledgeBaseId kbId = knowledgeBaseId != null ? KnowledgeBaseId.of(knowledgeBaseId) : null;
    List<Conversation> conversations =
        conversationApplicationService.listMyConversations(userId, kbId);
    List<ConversationResponse> data =
        conversations.stream().map(ConversationResponse::from).toList();
    int maxResults = maxListResults();
    boolean hasMore = data.size() > maxResults;
    List<ConversationResponse> page = hasMore ? data.subList(0, maxResults) : data;
    return ResponseEntity.ok()
        .header("X-Has-More", String.valueOf(hasMore))
        .header("X-Total-Count", String.valueOf(data.size()))
        .body(ApiResult.ok(page));
  }

  /** 查询单个会话详情. */
  @Operation(summary = "查询会话详情", description = "根据会话 ID 查询会话标题、状态和所属知识库。")
  @GetMapping("/{id}")
  public ResponseEntity<ApiResult<ConversationResponse>> getConversation(
      Authentication authentication,
      @Parameter(description = "会话 ID", example = "8f6d7b2b-0a54-40da-a6d6-5af81e04d102")
          @PathVariable
          String id) {
    UserId userId = (UserId) authentication.getPrincipal();
    ConversationId conversationId = ConversationId.of(id);
    Conversation conversation =
        conversationApplicationService.getConversation(conversationId, userId);
    return ResponseEntity.ok(ApiResult.ok(ConversationResponse.from(conversation)));
  }

  /** 查询会话消息列表. */
  @Operation(summary = "查询会话消息", description = "查看某个会话下的用户问题和 AI 回答历史。")
  @GetMapping("/{id}/messages")
  public ResponseEntity<ApiResult<List<MessageResponse>>> getMessages(
      Authentication authentication,
      @Parameter(description = "会话 ID", example = "8f6d7b2b-0a54-40da-a6d6-5af81e04d102")
          @PathVariable
          String id) {
    UserId userId = (UserId) authentication.getPrincipal();
    ConversationId conversationId = ConversationId.of(id);
    List<Message> messages = conversationApplicationService.getMessages(conversationId, userId);
    List<MessageResponse> data = messages.stream().map(MessageResponse::from).toList();
    int maxResults = maxListResults();
    boolean hasMore = data.size() > maxResults;
    List<MessageResponse> page =
        hasMore ? data.subList(data.size() - maxResults, data.size()) : data;
    return ResponseEntity.ok()
        .header("X-Has-More", String.valueOf(hasMore))
        .header("X-Total-Count", String.valueOf(data.size()))
        .body(ApiResult.ok(page));
  }

  /** 归档对话会话. */
  @Operation(summary = "归档会话", description = "将指定会话标记为归档，不再作为活跃会话展示。")
  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResult<Void>> archiveConversation(
      Authentication authentication,
      @Parameter(description = "会话 ID", example = "8f6d7b2b-0a54-40da-a6d6-5af81e04d102")
          @PathVariable
          String id) {
    UserId userId = (UserId) authentication.getPrincipal();
    ConversationId conversationId = ConversationId.of(id);
    conversationApplicationService.archiveConversation(conversationId, userId);
    return ResponseEntity.ok(ApiResult.ok(null));
  }

  private int maxListResults() {
    return Math.max(1, aiKbProperties.getChat().getMaxListResults());
  }
}
