package com.brolei.aikb.infrastructure.document;

import com.brolei.aikb.application.knowledge.DocumentTextExtractor;
import com.brolei.aikb.common.exception.BusinessException;
import com.brolei.aikb.common.exception.ErrorCode;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

/** 基于 PDFBox 的上传文档文本提取器. */
@Component
public class PdfBoxDocumentTextExtractor implements DocumentTextExtractor {

  @Override
  public String extract(String originalFilename, byte[] content) {
    String extension = extractExtension(originalFilename);
    if ("pdf".equals(extension)) {
      return extractPdf(content);
    }
    return new String(content, StandardCharsets.UTF_8);
  }

  private String extractPdf(byte[] content) {
    try (PDDocument document = Loader.loadPDF(content)) {
      String text = new PDFTextStripper().getText(document);
      if (text == null || text.isBlank()) {
        throw new BusinessException(
            ErrorCode.VALIDATION_ERROR, "PDF 未提取到文本，当前仅支持文本型 PDF，不支持扫描件 OCR");
      }
      return text;
    } catch (InvalidPasswordException e) {
      throw new BusinessException(ErrorCode.VALIDATION_ERROR, "不支持加密 PDF 文件");
    } catch (IOException e) {
      throw new BusinessException(ErrorCode.VALIDATION_ERROR, "PDF 文件解析失败，请确认文件未损坏");
    }
  }

  private static String extractExtension(String filename) {
    if (filename == null || filename.isBlank()) {
      return "";
    }
    int dotIndex = filename.lastIndexOf('.');
    if (dotIndex == -1 || dotIndex == filename.length() - 1) {
      return "";
    }
    return filename.substring(dotIndex + 1).toLowerCase();
  }
}
