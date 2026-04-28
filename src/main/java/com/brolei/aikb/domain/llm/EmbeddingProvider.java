package com.brolei.aikb.domain.llm;

import java.util.List;

/**
 * 嵌入向量提供者接口.
 *
 * <p>领域层只表达"需要对文本进行向量化"的需求，不依赖任何具体 LLM SDK。
 */
public interface EmbeddingProvider {

  /**
   * 对一批文本进行向量嵌入.
   *
   * @param texts 待嵌入的文本列表
   * @return 每条文本对应的向量（float 数组），顺序与输入一致
   */
  List<float[]> embedAll(List<String> texts);
}
