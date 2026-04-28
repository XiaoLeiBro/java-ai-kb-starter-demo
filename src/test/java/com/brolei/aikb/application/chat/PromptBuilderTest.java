package com.brolei.aikb.application.chat;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.brolei.aikb.domain.knowledge.model.DocumentChunkId;
import com.brolei.aikb.domain.knowledge.model.KnowledgeBaseId;
import com.brolei.aikb.domain.knowledge.model.KnowledgeDocumentId;
import com.brolei.aikb.domain.knowledge.model.RetrievedChunk;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** {@link PromptBuilder} 的单元测试. */
class PromptBuilderTest {

  private PromptBuilder promptBuilder;

  @BeforeEach
  void setUp() {
    promptBuilder = new PromptBuilder();
  }

  @Test
  void buildSystemPromptShouldReturnNoResultHintWhenReferencesEmpty() {
    String prompt = promptBuilder.buildSystemPrompt(Collections.emptyList());
    assertTrue(prompt.contains("当前知识库中没有找到相关信息"));
  }

  @Test
  void buildSystemPromptShouldReturnNoResultHintWhenReferencesNull() {
    String prompt = promptBuilder.buildSystemPrompt(null);
    assertTrue(prompt.contains("当前知识库中没有找到相关信息"));
  }

  @Test
  void buildSystemPromptShouldContainChunkContent() {
    List<RetrievedChunk> refs =
        List.of(
            new RetrievedChunk(
                KnowledgeBaseId.of("kb-1"),
                KnowledgeDocumentId.of("doc-1"),
                DocumentChunkId.of("chunk-1"),
                "policy.md",
                0,
                "年假为每年5天",
                0.95));
    String prompt = promptBuilder.buildSystemPrompt(refs);
    assertTrue(prompt.contains("年假为每年5天"));
    assertTrue(prompt.contains("policy.md"));
    assertTrue(prompt.contains("[片段1]"));
  }

  @Test
  void buildSystemPromptShouldSortByChunkIndex() {
    List<RetrievedChunk> refs =
        List.of(
            new RetrievedChunk(
                KnowledgeBaseId.of("kb-1"),
                KnowledgeDocumentId.of("doc-1"),
                DocumentChunkId.of("chunk-3"),
                "doc.md",
                2,
                "第三条内容",
                0.8),
            new RetrievedChunk(
                KnowledgeBaseId.of("kb-1"),
                KnowledgeDocumentId.of("doc-1"),
                DocumentChunkId.of("chunk-1"),
                "doc.md",
                0,
                "第一条内容",
                0.9),
            new RetrievedChunk(
                KnowledgeBaseId.of("kb-1"),
                KnowledgeDocumentId.of("doc-1"),
                DocumentChunkId.of("chunk-2"),
                "doc.md",
                1,
                "第二条内容",
                0.85));
    String prompt = promptBuilder.buildSystemPrompt(refs);
    int pos1 = prompt.indexOf("[片段1]");
    int pos2 = prompt.indexOf("[片段2]");
    int pos3 = prompt.indexOf("[片段3]");
    assertTrue(pos1 < pos2, "[片段1] should appear before [片段2]");
    assertTrue(pos2 < pos3, "[片段2] should appear before [片段3]");
  }

  @Test
  void buildUserMessageShouldContainQuestion() {
    List<RetrievedChunk> refs = Collections.emptyList();
    String msg = promptBuilder.buildUserMessage("年假有多少天？", refs);
    assertTrue(msg.contains("年假有多少天？"));
    assertTrue(msg.contains("请在回答时注明引用的片段编号"));
  }
}
