package com.brolei.aikb.infrastructure.text;

import com.brolei.aikb.common.config.AiKbProperties;
import com.brolei.aikb.domain.knowledge.service.TextSplitter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Component;

/** 基于固定字符数的文本切分实现. */
@Component
public class FixedCharTextSplitter implements TextSplitter {

  private final int chunkSize;
  private final int overlap;

  /** 通过配置注入切分参数. */
  public FixedCharTextSplitter(AiKbProperties properties) {
    this.chunkSize = properties.getTextSplitter().getChunkSize();
    this.overlap = properties.getTextSplitter().getOverlap();
  }

  /**
   * 按固定字符数切分文本，片段之间有重叠.
   *
   * <p>如果文本长度不超过 chunkSize，直接返回包含原文本的列表。 空白片段（trim 后为空）会被自动过滤。
   *
   * @param text 待切分的原始文本
   * @return 切分后的文本片段列表，保留原始顺序
   */
  @Override
  public List<String> split(String text) {
    if (text == null || text.isBlank()) {
      return Collections.emptyList();
    }

    String trimmed = text.strip();
    if (trimmed.length() <= chunkSize) {
      return Collections.singletonList(trimmed);
    }

    List<String> chunks = new ArrayList<>();
    int step = chunkSize - overlap;
    if (step <= 0) {
      step = chunkSize;
    }

    int start = 0;
    while (start < trimmed.length()) {
      int end = Math.min(start + chunkSize, trimmed.length());
      String chunk = trimmed.substring(start, end).strip();
      if (!chunk.isEmpty()) {
        chunks.add(chunk);
      }
      start += step;
    }

    return chunks;
  }
}
