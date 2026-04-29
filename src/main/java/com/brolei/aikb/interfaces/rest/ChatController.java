package com.brolei.aikb.interfaces.rest;

import com.brolei.aikb.application.chat.ChatApplicationService;
import com.brolei.aikb.application.chat.ChatResult;
import com.brolei.aikb.domain.chat.model.ConversationId;
import com.brolei.aikb.domain.knowledge.model.KnowledgeBaseId;
import com.brolei.aikb.domain.user.model.UserId;
import com.brolei.aikb.interfaces.dto.ApiResult;
import com.brolei.aikb.interfaces.dto.chat.ChatRequest;
import com.brolei.aikb.interfaces.dto.chat.ChatResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 聊天（RAG 问答）相关接口的 REST 控制器. */
@Tag(name = "AI 问答", description = "基于知识库检索结果进行 RAG 问答")
@RestController
@RequestMapping("/api/v1/chat")
public class ChatController {

  private final ChatApplicationService chatApplicationService;

  public ChatController(ChatApplicationService chatApplicationService) {
    this.chatApplicationService = chatApplicationService;
  }

  /** 基于知识库进行 RAG 检索并生成回答. */
  @Operation(summary = "知识库 AI 问答", description = "从指定知识库检索相关片段，并调用 Chat 模型生成回答；可选传入会话 ID 以保留上下文。")
  @PostMapping
  public ResponseEntity<ApiResult<ChatResponse>> chat(
      Authentication authentication, @Valid @RequestBody ChatRequest request) {
    UserId userId = (UserId) authentication.getPrincipal();
    KnowledgeBaseId kbId = KnowledgeBaseId.of(request.knowledgeBaseId());
    ChatResult result;
    if (request.conversationId() != null && !request.conversationId().isBlank()) {
      ConversationId conversationId = ConversationId.of(request.conversationId());
      result =
          chatApplicationService.chat(
              userId, kbId, request.question(), request.topK(), conversationId);
    } else {
      result = chatApplicationService.chat(userId, kbId, request.question(), request.topK());
    }
    return ResponseEntity.ok(ApiResult.ok(ChatResponse.from(result)));
  }
}
