package com.brolei.aikb.application.chat;

import com.brolei.aikb.common.exception.BusinessException;
import com.brolei.aikb.common.exception.ErrorCode;
import com.brolei.aikb.domain.chat.model.InvocationLog;
import com.brolei.aikb.domain.chat.repository.InvocationLogRepository;
import com.brolei.aikb.domain.knowledge.model.KnowledgeBaseId;
import com.brolei.aikb.domain.user.model.UserId;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 调用记录查询的应用服务. */
@Service
public class InvocationLogApplicationService {

  private final InvocationLogRepository invocationLogRepository;

  public InvocationLogApplicationService(InvocationLogRepository invocationLogRepository) {
    this.invocationLogRepository = invocationLogRepository;
  }

  /** 列出当前用户的调用记录. */
  @Transactional(readOnly = true)
  public List<InvocationLog> listMyInvocationLogs(
      UserId ownerId, KnowledgeBaseId knowledgeBaseId, Instant dateFrom, Instant dateTo) {
    if (dateFrom != null && dateTo != null && dateFrom.isAfter(dateTo)) {
      throw new BusinessException(ErrorCode.VALIDATION_ERROR, "dateFrom must not be after dateTo");
    }
    List<InvocationLog> logs;
    if (knowledgeBaseId != null) {
      logs = invocationLogRepository.findByOwnerIdAndKnowledgeBaseId(ownerId, knowledgeBaseId);
    } else {
      logs = invocationLogRepository.findByOwnerId(ownerId);
    }
    if (dateFrom != null || dateTo != null) {
      Instant from = dateFrom != null ? dateFrom : Instant.EPOCH;
      Instant to = dateTo != null ? dateTo : Instant.now();
      logs =
          logs.stream()
              .filter(log -> !log.createdAt().isBefore(from) && !log.createdAt().isAfter(to))
              .toList();
    }
    return logs;
  }
}
