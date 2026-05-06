package com.brolei.aikb.application.knowledge;

/** 从上传文档中提取可用于切分和向量化的纯文本. */
public interface DocumentTextExtractor {

  /**
   * 提取上传文件中的文本.
   *
   * @param originalFilename 原始文件名
   * @param content 文件二进制内容
   * @return 可切分、可向量化的文本
   */
  String extract(String originalFilename, byte[] content);
}
