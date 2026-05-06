package com.brolei.aikb.application.chat;

import com.brolei.aikb.domain.knowledge.model.RetrievedChunk;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * 聊天提示词构建器.
 *
 * <p>根据检索到的知识库片段构建 system prompt 和 user message。 纯计算组件，不注入任何外部依赖，可独立进行单元测试。
 */
@Component
public class PromptBuilder {

  /** 无检索片段时的默认 system prompt. */
  private static final String NO_REFERENCES_PROMPT =
      "你是企业知识库问答助手。当前知识库中没有找到相关信息，请直接告诉用户「当前知识库中没有找到相关信息」。不要编造任何信息。";

  /** 有检索片段时的 system prompt 模板头部. */
  private static final String REFERENCE_PROMPT_HEADER =
      "你是企业知识库问答助手。\n"
          + "只能基于给定的知识库片段回答用户问题。\n"
          + "如果片段中没有答案，明确说「当前知识库中没有找到相关信息」。\n"
          + "不要编造制度、金额、日期、负责人。\n\n"
          + "回答格式要求：使用有层次的自然中文纯文本。可以使用标题独立成行、段落空行、中文序号或阿拉伯数字列表；"
          + "不要使用 Markdown 符号或语法，例如 #、*、-、>、```、|、加粗、表格、代码块、链接语法。"
          + "优先用短句和短段落，让非技术用户也能直接阅读。\n\n"
          + "以下是知识库中的相关片段：\n";

  /**
   * 构建系统提示词.
   *
   * @param references 检索到的相关片段列表，按 chunkIndex 升序排列后嵌入 prompt
   * @return 系统提示词字符串
   */
  public String buildSystemPrompt(List<RetrievedChunk> references) {
    if (references == null || references.isEmpty()) {
      return NO_REFERENCES_PROMPT;
    }

    List<RetrievedChunk> sorted =
        references.stream().sorted(Comparator.comparingInt(RetrievedChunk::chunkIndex)).toList();

    StringBuilder sb = new StringBuilder(REFERENCE_PROMPT_HEADER);
    for (int i = 0; i < sorted.size(); i++) {
      RetrievedChunk chunk = sorted.get(i);
      sb.append("[片段")
          .append(i + 1)
          .append("] ")
          .append(chunk.content())
          .append("（来源：")
          .append(chunk.fileName())
          .append("）\n");
    }
    return sb.toString();
  }

  /**
   * 构建用户消息.
   *
   * @param question 用户原始问题
   * @param references 检索到的相关片段列表（用于提示引用格式）
   * @return 拼接后的用户消息字符串
   */
  public String buildUserMessage(String question, List<RetrievedChunk> references) {
    StringBuilder sb = new StringBuilder("用户问题：");
    sb.append(question);
    sb.append("\n\n请用有排版的纯文本回答，并在相关句子后注明引用片段编号，例如 [片段1]、[片段2]。");
    return sb.toString();
  }
}
