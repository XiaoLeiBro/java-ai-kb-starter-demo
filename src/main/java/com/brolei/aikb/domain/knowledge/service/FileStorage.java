package com.brolei.aikb.domain.knowledge.service;

import com.brolei.aikb.domain.knowledge.model.KnowledgeBaseId;
import com.brolei.aikb.domain.knowledge.model.KnowledgeDocumentId;
import com.brolei.aikb.domain.user.model.UserId;

/**
 * 文件存储领域服务接口.
 *
 * <p>抽象文件物理存储操作，领域层不依赖具体的存储实现。
 */
public interface FileStorage {

  /**
   * 存储文件并返回存储路径.
   *
   * @param userId 用户 ID
   * @param kbId 知识库 ID
   * @param docId 文档 ID
   * @param originalFilename 原始文件名
   * @param content 文件字节内容
   * @return 存储路径
   */
  String store(
      UserId userId,
      KnowledgeBaseId kbId,
      KnowledgeDocumentId docId,
      String originalFilename,
      byte[] content);

  /**
   * 根据存储路径读取文件内容.
   *
   * @param storagePath 存储路径
   * @return 文件字节内容
   */
  byte[] read(String storagePath);
}
