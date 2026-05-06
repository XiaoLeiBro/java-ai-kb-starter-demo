package com.brolei.aikb.interfaces.rest;

import com.brolei.aikb.application.knowledge.DocumentDownload;
import com.brolei.aikb.application.knowledge.KnowledgeApplicationService;
import com.brolei.aikb.common.exception.BusinessException;
import com.brolei.aikb.common.exception.ErrorCode;
import com.brolei.aikb.domain.knowledge.model.KnowledgeBase;
import com.brolei.aikb.domain.knowledge.model.KnowledgeBaseId;
import com.brolei.aikb.domain.knowledge.model.KnowledgeDocument;
import com.brolei.aikb.domain.knowledge.model.KnowledgeDocumentId;
import com.brolei.aikb.domain.user.model.UserId;
import com.brolei.aikb.interfaces.dto.ApiResult;
import com.brolei.aikb.interfaces.dto.knowledge.CreateKnowledgeBaseRequest;
import com.brolei.aikb.interfaces.dto.knowledge.DocumentResponse;
import com.brolei.aikb.interfaces.dto.knowledge.KnowledgeBaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/** 知识库相关接口的 REST 控制器. */
@Tag(name = "知识库管理", description = "创建知识库、查看知识库列表、上传文档和查看文档状态")
@RestController
@RequestMapping("/api/v1/knowledge-bases")
public class KnowledgeBaseController {

  private final KnowledgeApplicationService knowledgeApplicationService;

  public KnowledgeBaseController(KnowledgeApplicationService knowledgeApplicationService) {
    this.knowledgeApplicationService = knowledgeApplicationService;
  }

  /** 创建新知识库. */
  @Operation(summary = "创建知识库", description = "为当前登录用户创建一个知识库，用于后续上传文档和 AI 问答。")
  @PostMapping
  public ResponseEntity<ApiResult<KnowledgeBaseResponse>> create(
      Authentication authentication, @Valid @RequestBody CreateKnowledgeBaseRequest request) {
    UserId ownerId = (UserId) authentication.getPrincipal();
    KnowledgeBase kb =
        knowledgeApplicationService.createKnowledgeBase(
            ownerId, request.name(), request.description());
    return ResponseEntity.ok(ApiResult.ok(KnowledgeBaseResponse.from(kb)));
  }

  /** 获取当前用户的知识库列表. */
  @Operation(summary = "查询我的知识库", description = "返回当前登录用户创建的全部知识库。")
  @GetMapping
  public ResponseEntity<ApiResult<List<KnowledgeBaseResponse>>> listMine(
      Authentication authentication) {
    UserId ownerId = (UserId) authentication.getPrincipal();
    List<KnowledgeBase> kbs = knowledgeApplicationService.listMyKnowledgeBases(ownerId);
    List<KnowledgeBaseResponse> responses = kbs.stream().map(KnowledgeBaseResponse::from).toList();
    return ResponseEntity.ok(ApiResult.ok(responses));
  }

  /** 向指定知识库上传文档，并触发索引. */
  @Operation(summary = "上传知识库文档", description = "上传文档到指定知识库，系统会解析、切分并写入向量索引。")
  @PostMapping("/{id}/documents")
  public ResponseEntity<ApiResult<DocumentResponse>> uploadDocument(
      Authentication authentication,
      @Parameter(description = "知识库 ID", example = "4c9f0d1a-0a3a-4c48-bb6a-7c8b69e1b001")
          @PathVariable
          String id,
      @Parameter(description = "要上传的文档文件，demo 支持 Markdown、纯文本或文本型 PDF") @RequestParam("file")
          MultipartFile file) {
    UserId ownerId = (UserId) authentication.getPrincipal();
    KnowledgeBaseId kbId = KnowledgeBaseId.of(id);
    String originalFilename = file.getOriginalFilename();
    byte[] content;
    try {
      content = file.getBytes();
    } catch (IOException e) {
      throw new BusinessException(ErrorCode.VALIDATION_ERROR, "无法读取上传文件内容");
    }
    KnowledgeDocument doc =
        knowledgeApplicationService.uploadAndIndexDocument(
            ownerId, kbId, originalFilename, content);
    return ResponseEntity.ok(ApiResult.ok(DocumentResponse.from(doc)));
  }

  /** 获取指定知识库下的文档列表. */
  @Operation(summary = "查询知识库文档", description = "查看指定知识库下已上传文档的处理状态和切分数量。")
  @GetMapping("/{id}/documents")
  public ResponseEntity<ApiResult<List<DocumentResponse>>> listDocuments(
      Authentication authentication,
      @Parameter(description = "知识库 ID", example = "4c9f0d1a-0a3a-4c48-bb6a-7c8b69e1b001")
          @PathVariable
          String id) {
    UserId ownerId = (UserId) authentication.getPrincipal();
    KnowledgeBaseId kbId = KnowledgeBaseId.of(id);
    List<KnowledgeDocument> docs = knowledgeApplicationService.listDocuments(ownerId, kbId);
    List<DocumentResponse> responses = docs.stream().map(DocumentResponse::from).toList();
    return ResponseEntity.ok(ApiResult.ok(responses));
  }

  /** 下载指定知识库下的原始文档. */
  @Operation(summary = "下载知识库原始文档", description = "下载当前用户已上传到指定知识库的原始文件。")
  @GetMapping("/{id}/documents/{documentId}/download")
  public ResponseEntity<byte[]> downloadDocument(
      Authentication authentication,
      @Parameter(description = "知识库 ID") @PathVariable String id,
      @Parameter(description = "文档 ID") @PathVariable String documentId) {
    UserId ownerId = (UserId) authentication.getPrincipal();
    DocumentDownload download =
        knowledgeApplicationService.downloadDocument(
            ownerId, KnowledgeBaseId.of(id), KnowledgeDocumentId.of(documentId));
    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType(download.contentType()))
        .header(
            HttpHeaders.CONTENT_DISPOSITION,
            ContentDisposition.attachment()
                .filename(download.filename(), StandardCharsets.UTF_8)
                .build()
                .toString())
        .body(download.content());
  }
}
