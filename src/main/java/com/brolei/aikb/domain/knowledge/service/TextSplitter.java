package com.brolei.aikb.domain.knowledge.service;

import java.util.List;

/**
 * 文本分割领域服务接口.
 *
 * <p>将长文本切分为适合嵌入的短片段。
 */
public interface TextSplitter {

  /**
   * 将文本切分为多个片段.
   *
   * @param text 原始文本
   * @return 切分后的文本片段列表
   */
  List<String> split(String text);
}
