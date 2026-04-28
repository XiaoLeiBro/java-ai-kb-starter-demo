package com.brolei.aikb.interfaces.rest;

import com.brolei.aikb.application.chat.InvocationLogApplicationService;
import com.brolei.aikb.common.config.AiKbProperties;
import com.brolei.aikb.domain.chat.model.InvocationLog;
import com.brolei.aikb.domain.knowledge.model.KnowledgeBaseId;
import com.brolei.aikb.domain.user.model.UserId;
import com.brolei.aikb.interfaces.dto.ApiResult;
import com.brolei.aikb.interfaces.dto.chat.InvocationLogResponse;
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
  @GetMapping
  public ResponseEntity<ApiResult<List<InvocationLogResponse>>> listInvocationLogs(
      Authentication authentication,
      @RequestParam(required = false) String knowledgeBaseId,
      @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateFrom,
      @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateTo) {
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
