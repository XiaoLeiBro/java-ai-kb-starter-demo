package com.brolei.aikb.domain.chat.repository;

import com.brolei.aikb.domain.chat.model.InvocationLog;
import com.brolei.aikb.domain.knowledge.model.KnowledgeBaseId;
import com.brolei.aikb.domain.user.model.UserId;
import java.time.Instant;
import java.util.List;

/** LLM 调用记录仓储接口. */
public interface InvocationLogRepository {

  InvocationLog save(InvocationLog invocationLog);

  List<InvocationLog> findByOwnerId(UserId ownerId);

  List<InvocationLog> findByOwnerIdAndKnowledgeBaseId(
      UserId ownerId, KnowledgeBaseId knowledgeBaseId);

  List<InvocationLog> findByOwnerIdAndDateRange(UserId ownerId, Instant dateFrom, Instant dateTo);
}
