package com.brolei.aikb.domain.chat.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class MessageTest {

  private final ConversationId convId = ConversationId.generate();

  @Test
  void createShouldSucceedWithUserRole() {
    Message m = Message.create(convId, MessageRole.USER, "Hello");
    assertEquals(MessageRole.USER, m.role());
    assertEquals("Hello", m.content());
  }

  @Test
  void createShouldSucceedWithAssistantRole() {
    Message m = Message.create(convId, MessageRole.ASSISTANT, "Hi there");
    assertEquals(MessageRole.ASSISTANT, m.role());
  }

  @Test
  void createShouldFailWithNullConversationId() {
    assertThrows(
        NullPointerException.class, () -> Message.create(null, MessageRole.USER, "content"));
  }

  @Test
  void createShouldFailWithNullRole() {
    assertThrows(NullPointerException.class, () -> Message.create(convId, null, "content"));
  }

  @Test
  void createShouldFailWithNullContent() {
    assertThrows(
        IllegalArgumentException.class, () -> Message.create(convId, MessageRole.USER, null));
  }

  @Test
  void createShouldFailWithBlankContent() {
    assertThrows(
        IllegalArgumentException.class, () -> Message.create(convId, MessageRole.USER, "   "));
  }

  @Test
  void createShouldFailWithEmptyContent() {
    assertThrows(
        IllegalArgumentException.class, () -> Message.create(convId, MessageRole.USER, ""));
  }

  @Test
  void createShouldAllowEmptyAssistantContent() {
    Message m = Message.create(convId, MessageRole.ASSISTANT, "");
    assertEquals("", m.content());
  }

  @Test
  void createShouldSucceedWithContentAtLimit() {
    String content = "x".repeat(50_000);
    Message m = Message.create(convId, MessageRole.USER, content);
    assertEquals(50_000, m.content().length());
  }

  @Test
  void createShouldFailWithContentExceedingLimit() {
    String content = "x".repeat(50_001);
    assertThrows(
        IllegalArgumentException.class, () -> Message.create(convId, MessageRole.USER, content));
  }
}
