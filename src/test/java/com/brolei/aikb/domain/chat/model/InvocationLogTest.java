package com.brolei.aikb.domain.chat.model;

import static org.junit.jupiter.api.Assertions.*;

import com.brolei.aikb.domain.knowledge.model.KnowledgeBaseId;
import com.brolei.aikb.domain.user.model.UserId;
import org.junit.jupiter.api.Test;

class InvocationLogTest {

  private final UserId ownerId = UserId.generate();
  private final KnowledgeBaseId kbId = KnowledgeBaseId.generate();
  private final ConversationId convId = ConversationId.generate();
  private final MessageId msgId = MessageId.generate();

  @Test
  void recordSuccessShouldCreateWithCorrectTotalTokens() {
    InvocationLog log =
        InvocationLog.recordSuccess(ownerId, kbId, convId, msgId, "test-model", 100, 50, 500);
    assertEquals(150, log.totalTokens());
    assertEquals(100, log.promptTokens());
    assertEquals(50, log.completionTokens());
    assertEquals(InvocationStatus.SUCCESS, log.status());
    assertEquals(msgId, log.messageId());
  }

  @Test
  void recordSuccessShouldFailWithNegativePromptTokens() {
    assertThrows(
        IllegalArgumentException.class,
        () -> InvocationLog.recordSuccess(ownerId, kbId, convId, msgId, "test-model", -1, 0, 0));
  }

  @Test
  void recordSuccessShouldFailWithNegativeCompletionTokens() {
    assertThrows(
        IllegalArgumentException.class,
        () -> InvocationLog.recordSuccess(ownerId, kbId, convId, msgId, "test-model", 0, -1, 0));
  }

  @Test
  void recordSuccessShouldFailWithNegativeDuration() {
    assertThrows(
        IllegalArgumentException.class,
        () -> InvocationLog.recordSuccess(ownerId, kbId, convId, msgId, "test-model", 0, 0, -1));
  }

  @Test
  void recordFailureShouldCreateWithErrorMessage() {
    InvocationLog log =
        InvocationLog.recordFailure(ownerId, kbId, convId, msgId, "test-model", 100, "Timeout");
    assertEquals(InvocationStatus.FAILED, log.status());
    assertEquals("Timeout", log.errorMessage());
    assertEquals(0, log.totalTokens());
  }

  @Test
  void recordFailureShouldFailWithNullErrorMessage() {
    assertThrows(
        IllegalArgumentException.class,
        () -> InvocationLog.recordFailure(ownerId, kbId, convId, msgId, "test-model", 0, null));
  }

  @Test
  void recordFailureShouldFailWithBlankErrorMessage() {
    assertThrows(
        IllegalArgumentException.class,
        () -> InvocationLog.recordFailure(ownerId, kbId, convId, msgId, "test-model", 0, "  "));
  }
}
