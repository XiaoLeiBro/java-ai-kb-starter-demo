package com.brolei.aikb.application.common;

import com.brolei.aikb.common.exception.BusinessException;
import com.brolei.aikb.common.exception.ErrorCode;
import java.util.List;
import org.springframework.stereotype.Component;

/** 文件上传校验辅助组件. */
@Component
public class FileValidator {

  /**
   * 校验上传文件的合法性.
   *
   * @param content 文件内容
   * @param originalFilename 原始文件名
   * @param maxFileSize 最大文件大小（字节）
   * @param allowedExtensions 允许的扩展名列表（小写，不含点）
   */
  public void validate(
      byte[] content, String originalFilename, long maxFileSize, List<String> allowedExtensions) {

    if (content == null || content.length == 0) {
      throw new BusinessException(ErrorCode.VALIDATION_ERROR, "文件为空");
    }

    String extension = extractExtension(originalFilename);
    if (extension == null
        || allowedExtensions.stream().noneMatch(ext -> ext.equalsIgnoreCase(extension))) {
      throw new BusinessException(ErrorCode.UNSUPPORTED_FILE_TYPE, "不支持的文件类型: " + extension);
    }

    if (content.length > maxFileSize) {
      throw new BusinessException(ErrorCode.VALIDATION_ERROR, "文件过大");
    }
  }

  /**
   * 从文件名中提取扩展名（小写）.
   *
   * @param filename 文件名
   * @return 扩展名（不含点），如文件无扩展名则返回 null
   */
  private static String extractExtension(String filename) {
    if (filename == null || filename.isBlank()) {
      return null;
    }
    int dotIndex = filename.lastIndexOf('.');
    if (dotIndex == -1 || dotIndex == filename.length() - 1) {
      return null;
    }
    return filename.substring(dotIndex + 1).toLowerCase();
  }
}
