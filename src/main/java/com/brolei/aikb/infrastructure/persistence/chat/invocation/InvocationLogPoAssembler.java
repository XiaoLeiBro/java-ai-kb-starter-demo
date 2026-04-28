package com.brolei.aikb.infrastructure.persistence.chat.invocation;

import com.brolei.aikb.domain.chat.model.ConversationId;
import com.brolei.aikb.domain.chat.model.InvocationLog;
import com.brolei.aikb.domain.chat.model.InvocationLogId;
import com.brolei.aikb.domain.chat.model.InvocationStatus;
import com.brolei.aikb.domain.chat.model.MessageId;
import com.brolei.aikb.domain.knowledge.model.KnowledgeBaseId;
import com.brolei.aikb.domain.user.model.UserId;
import org.springframework.stereotype.Component;

/** InvocationLog 与 InvocationLogPo 之间的转换器. */
@Component
public class InvocationLogPoAssembler {

  public InvocationLog toDomain(InvocationLogPo po) {
    if (po == null) {
      return null;
    }
    return InvocationLog.rehydrate(
        InvocationLogId.of(po.getId()),
        UserId.of(po.getOwnerId()),
        po.getKnowledgeBaseId() != null ? KnowledgeBaseId.of(po.getKnowledgeBaseId()) : null,
        po.getConversationId() != null ? ConversationId.of(po.getConversationId()) : null,
        po.getMessageId() != null ? MessageId.of(po.getMessageId()) : null,
        po.getModelName(),
        po.getPromptTokens(),
        po.getCompletionTokens(),
        po.getTotalTokens(),
        po.getDurationMs(),
        InvocationStatus.valueOf(po.getStatus()),
        po.getErrorMessage(),
        po.getCreatedAt());
  }

  public InvocationLogPo toPo(InvocationLog invocationLog) {
    if (invocationLog == null) {
      return null;
    }
    InvocationLogPo po = new InvocationLogPo();
    po.setId(invocationLog.id().value());
    po.setOwnerId(invocationLog.ownerId().value());
    po.setKnowledgeBaseId(
        invocationLog.knowledgeBaseId() != null ? invocationLog.knowledgeBaseId().value() : null);
    po.setConversationId(
        invocationLog.conversationId() != null ? invocationLog.conversationId().value() : null);
    po.setMessageId(invocationLog.messageId() != null ? invocationLog.messageId().value() : null);
    po.setModelName(invocationLog.modelName());
    po.setPromptTokens(invocationLog.promptTokens());
    po.setCompletionTokens(invocationLog.completionTokens());
    po.setTotalTokens(invocationLog.totalTokens());
    po.setDurationMs(invocationLog.durationMs());
    po.setStatus(invocationLog.status().name());
    po.setErrorMessage(invocationLog.errorMessage());
    po.setCreatedAt(invocationLog.createdAt());
    return po;
  }
}
