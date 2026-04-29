package com.brolei.aikb.interfaces.rest;

import com.brolei.aikb.application.chat.InvocationLogApplicationService;
import com.brolei.aikb.common.config.AiKbProperties;
import com.brolei.aikb.domain.chat.model.InvocationLog;
import com.brolei.aikb.domain.knowledge.model.KnowledgeBaseId;
import com.brolei.aikb.domain.user.model.UserId;
import com.brolei.aikb.interfaces.dto.ApiResult;
import com.brolei.aikb.interfaces.dto.chat.InvocationLogResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 调用记录查询接口的 REST 控制器. */
@Tag(name = "调用记录", description = "查询 AI 调用记录、模型名称、Token 用量、耗时和失败原因")
@RestController
@RequestMapping("/api/v1/invocation-logs")
public class InvocationLogController {

  private final InvocationLogApplicationService invocationLogApplicationService;
  private final AiKbProperties aiKbProperties;

  public InvocationLogController(
      InvocationLogApplicationService invocationLogApplicationService,
      AiKbProperties aiKbProperties) {
    this.invocationLogApplicationService = invocationLogApplicationService;
    this.aiKbProperties = aiKbProperties;
  }

  /** 查询调用记录列表. */
  @Operation(summary = "查询调用记录", description = "按知识库和日期范围查询当前用户的 AI 调用记录，用于 demo 展示和成本追踪。")
  @GetMapping
  public ResponseEntity<ApiResult<List<InvocationLogResponse>>> listInvocationLogs(
      Authentication authentication,
      @Parameter(
              description = "知识库 ID，不传则查询全部知识库",
              example = "4c9f0d1a-0a3a-4c48-bb6a-7c8b69e1b001")
          @RequestParam(required = false)
          String knowledgeBaseId,
      @Parameter(description = "开始日期，格式 yyyy-MM-dd", example = "2026-04-01")
          @RequestParam(required = false)
          @DateTimeFormat(pattern = "yyyy-MM-dd")
          LocalDate dateFrom,
      @Parameter(description = "结束日期，格式 yyyy-MM-dd", example = "2026-04-30")
          @RequestParam(required = false)
          @DateTimeFormat(pattern = "yyyy-MM-dd")
          LocalDate dateTo) {
    UserId userId = (UserId) authentication.getPrincipal();
    KnowledgeBaseId kbId = knowledgeBaseId != null ? KnowledgeBaseId.of(knowledgeBaseId) : null;
    Instant from = dateFrom != null ? dateFrom.atStartOfDay().toInstant(ZoneOffset.UTC) : null;
    Instant to =
        dateTo != null ? dateTo.atTime(23, 59, 59, 999_999_999).toInstant(ZoneOffset.UTC) : null;
    List<InvocationLog> logs =
        invocationLogApplicationService.listMyInvocationLogs(userId, kbId, from, to);
    List<InvocationLogResponse> data = logs.stream().map(InvocationLogResponse::from).toList();
    int maxResults = maxListResults();
    boolean hasMore = data.size() > maxResults;
    List<InvocationLogResponse> page = hasMore ? data.subList(0, maxResults) : data;
    return ResponseEntity.ok()
        .header("X-Has-More", String.valueOf(hasMore))
        .header("X-Total-Count", String.valueOf(data.size()))
        .body(ApiResult.ok(page));
  }

  private int maxListResults() {
    return Math.max(1, aiKbProperties.getChat().getMaxListResults());
  }
}
