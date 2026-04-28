package com.brolei.aikb.interfaces.rest;

import com.brolei.aikb.application.knowledge.KnowledgeApplicationService;
import com.brolei.aikb.common.exception.BusinessException;
import com.brolei.aikb.common.exception.ErrorCode;
import com.brolei.aikb.domain.knowledge.model.KnowledgeBase;
import com.brolei.aikb.domain.knowledge.model.KnowledgeBaseId;
import com.brolei.aikb.domain.knowledge.model.KnowledgeDocument;
import com.brolei.aikb.domain.user.model.UserId;
import com.brolei.aikb.interfaces.dto.ApiResult;
import com.brolei.aikb.interfaces.dto.knowledge.CreateKnowledgeBaseRequest;
import com.brolei.aikb.interfaces.dto.knowledge.DocumentResponse;
import com.brolei.aikb.interfaces.dto.knowledge.KnowledgeBaseResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;
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
@RestController
@RequestMapping("/api/v1/knowledge-bases")
public class KnowledgeBaseController {

  private final KnowledgeApplicationService knowledgeApplicationService;

  public KnowledgeBaseController(KnowledgeApplicationService knowledgeApplicationService) {
    this.knowledgeApplicationService = knowledgeApplicationService;
  }

  /** 创建新知识库. */
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
  @GetMapping
  public ResponseEntity<ApiResult<List<KnowledgeBaseResponse>>> listMine(
      Authentication authentication) {
    UserId ownerId = (UserId) authentication.getPrincipal();
    List<KnowledgeBase> kbs = knowledgeApplicationService.listMyKnowledgeBases(ownerId);
    List<KnowledgeBaseResponse> responses = kbs.stream().map(KnowledgeBaseResponse::from).toList();
    return ResponseEntity.ok(ApiResult.ok(responses));
  }

  /** 向指定知识库上传文档，并触发索引. */
  @PostMapping("/{id}/documents")
  public ResponseEntity<ApiResult<DocumentResponse>> uploadDocument(
      Authentication authentication,
      @PathVariable String id,
      @RequestParam("file") MultipartFile file) {
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
  @GetMapping("/{id}/documents")
  public ResponseEntity<ApiResult<List<DocumentResponse>>> listDocuments(
      Authentication authentication, @PathVariable String id) {
    UserId ownerId = (UserId) authentication.getPrincipal();
    KnowledgeBaseId kbId = KnowledgeBaseId.of(id);
    List<KnowledgeDocument> docs = knowledgeApplicationService.listDocuments(ownerId, kbId);
    List<DocumentResponse> responses = docs.stream().map(DocumentResponse::from).toList();
    return ResponseEntity.ok(ApiResult.ok(responses));
  }
}
