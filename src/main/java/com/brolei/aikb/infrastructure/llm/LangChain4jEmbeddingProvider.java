package com.brolei.aikb.infrastructure.llm;

import com.brolei.aikb.common.exception.BusinessException;
import com.brolei.aikb.common.exception.ErrorCode;
import com.brolei.aikb.domain.llm.EmbeddingProvider;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Component;

/** 基于 LangChain4j 的 Embedding 提供者实现. */
@Component
public class LangChain4jEmbeddingProvider implements EmbeddingProvider {

  private final EmbeddingModel embeddingModel;

  /** 注入 LangChain4j 自动配置的 EmbeddingModel Bean. */
  public LangChain4jEmbeddingProvider(EmbeddingModel embeddingModel) {
    this.embeddingModel = embeddingModel;
  }

  /**
   * 对多条文本进行向量化.
   *
   * <p>逐条调用 EmbeddingModel，将返回的 Embedding 对象转换为 float[]。 如果 API Key 缺失或调用失败，抛出 BusinessException。
   *
   * @param texts 待向量化的文本列表
   * @return 每条文本对应的 float[] 向量
   */
  @Override
  public List<float[]> embedAll(List<String> texts) {
    if (texts == null || texts.isEmpty()) {
      return Collections.emptyList();
    }

    List<float[]> result = new ArrayList<>();
    for (String text : texts) {
      try {
        Embedding embedding = embeddingModel.embed(text).content();
        float[] vector = new float[embedding.vector().length];
        for (int i = 0; i < vector.length; i++) {
          vector[i] = embedding.vector()[i];
        }
        result.add(vector);
      } catch (Exception e) {
        throw new BusinessException(
            ErrorCode.LLM_PROVIDER_ERROR, "Embedding 调用失败: " + e.getMessage());
      }
    }
    return result;
  }
}
