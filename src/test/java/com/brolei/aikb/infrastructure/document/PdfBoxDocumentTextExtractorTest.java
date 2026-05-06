package com.brolei.aikb.infrastructure.document;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.brolei.aikb.common.exception.BusinessException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.Test;

class PdfBoxDocumentTextExtractorTest {

  private final PdfBoxDocumentTextExtractor extractor = new PdfBoxDocumentTextExtractor();

  @Test
  void extractShouldReadPlainTextAsUtf8() {
    String text = extractor.extract("demo.md", "企业知识库".getBytes(StandardCharsets.UTF_8));

    assertEquals("企业知识库", text);
  }

  @Test
  void extractShouldReadTextPdf() throws IOException {
    byte[] pdf = createTextPdf("Customer refund policy");

    String text = extractor.extract("policy.pdf", pdf);

    assertTrue(text.contains("Customer refund policy"));
  }

  @Test
  void extractShouldRejectBlankPdf() throws IOException {
    byte[] pdf = createBlankPdf();

    BusinessException exception =
        assertThrows(BusinessException.class, () -> extractor.extract("blank.pdf", pdf));

    assertTrue(exception.getMessage().contains("文本型 PDF"));
  }

  @Test
  void extractShouldRejectBrokenPdf() {
    BusinessException exception =
        assertThrows(
            BusinessException.class,
            () -> extractor.extract("broken.pdf", "not a pdf".getBytes(StandardCharsets.UTF_8)));

    assertTrue(exception.getMessage().contains("PDF 文件解析失败"));
  }

  private static byte[] createTextPdf(String text) throws IOException {
    try (PDDocument document = new PDDocument();
        ByteArrayOutputStream output = new ByteArrayOutputStream()) {
      PDPage page = new PDPage();
      document.addPage(page);
      try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
        contentStream.beginText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
        contentStream.newLineAtOffset(72, 720);
        contentStream.showText(text);
        contentStream.endText();
      }
      document.save(output);
      return output.toByteArray();
    }
  }

  private static byte[] createBlankPdf() throws IOException {
    try (PDDocument document = new PDDocument();
        ByteArrayOutputStream output = new ByteArrayOutputStream()) {
      document.addPage(new PDPage());
      document.save(output);
      return output.toByteArray();
    }
  }
}
