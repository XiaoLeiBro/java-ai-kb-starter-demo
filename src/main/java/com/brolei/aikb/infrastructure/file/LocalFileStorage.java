package com.brolei.aikb.infrastructure.file;

import com.brolei.aikb.common.config.AiKbProperties;
import com.brolei.aikb.common.exception.BusinessException;
import com.brolei.aikb.common.exception.ErrorCode;
import com.brolei.aikb.domain.knowledge.model.KnowledgeBaseId;
import com.brolei.aikb.domain.knowledge.model.KnowledgeDocumentId;
import com.brolei.aikb.domain.knowledge.service.FileStorage;
import com.brolei.aikb.domain.user.model.UserId;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.stereotype.Component;

/** 基于本地文件系统的文件存储实现. */
@Component
public class LocalFileStorage implements FileStorage {

  private final String rootDir;

  /** 通过配置注入上传根目录. */
  public LocalFileStorage(AiKbProperties properties) {
    this.rootDir = properties.getUpload().getRootDir();
  }

  /**
   * 存储文件到本地文件系统.
   *
   * <p>路径格式：{rootDir}/{userId}/{knowledgeBaseId}/{documentId}/{originalFilename}
   *
   * @param userId 用户标识
   * @param kbId 知识库标识
   * @param docId 文档标识
   * @param originalFilename 原始文件名
   * @param content 文件内容
   * @return 存储的相对路径
   */
  @Override
  public String store(
      UserId userId,
      KnowledgeBaseId kbId,
      KnowledgeDocumentId docId,
      String originalFilename,
      byte[] content) {
    String safeFilename = sanitizeFilename(originalFilename);
    Path dir = Paths.get(rootDir, userId.value(), kbId.value(), docId.value());
    Path filePath = dir.resolve(safeFilename);

    try {
      Files.createDirectories(dir);
      Files.write(filePath, content);
    } catch (IOException e) {
      throw new BusinessException(ErrorCode.INTERNAL_ERROR, "文件存储失败: " + e.getMessage());
    }

    // 返回相对路径
    return Paths.get(userId.value(), kbId.value(), docId.value(), safeFilename).toString();
  }

  /**
   * 读取存储路径对应的文件内容.
   *
   * @param storagePath 存储路径
   * @return 文件字节内容
   */
  @Override
  public byte[] read(String storagePath) {
    String safePath = sanitizePath(storagePath);
    Path filePath = Paths.get(rootDir, safePath);

    try {
      return Files.readAllBytes(filePath);
    } catch (IOException e) {
      throw new BusinessException(ErrorCode.INTERNAL_ERROR, "文件读取失败: " + e.getMessage());
    }
  }

  /**
   * 对文件名进行安全处理，移除路径穿越字符.
   *
   * @param filename 原始文件名
   * @return 安全的文件名
   */
  static String sanitizeFilename(String filename) {
    if (filename == null || filename.isBlank()) {
      return "unnamed";
    }
    return filename.replace("..", "").replace("/", "_").replace("\\", "_");
  }

  /**
   * 对存储路径进行安全处理，移除路径穿越字符.
   *
   * @param path 原始路径
   * @return 安全的路径
   */
  static String sanitizePath(String path) {
    if (path == null || path.isBlank()) {
      throw new BusinessException(ErrorCode.VALIDATION_ERROR, "存储路径不能为空");
    }
    return path.replace("..", "").replace("\\", "/");
  }
}
