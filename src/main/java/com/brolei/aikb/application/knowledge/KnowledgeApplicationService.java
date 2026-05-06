package com.brolei.aikb.application.knowledge;

import com.brolei.aikb.application.common.FileValidator;
import com.brolei.aikb.common.config.AiKbProperties;
import com.brolei.aikb.common.exception.BusinessException;
import com.brolei.aikb.common.exception.ErrorCode;
import com.brolei.aikb.domain.knowledge.model.DocumentChunk;
import com.brolei.aikb.domain.knowledge.model.KnowledgeBase;
import com.brolei.aikb.domain.knowledge.model.KnowledgeBaseId;
import com.brolei.aikb.domain.knowledge.model.KnowledgeDocument;
import com.brolei.aikb.domain.knowledge.model.KnowledgeDocumentId;
import com.brolei.aikb.domain.knowledge.model.VectorChunk;
import com.brolei.aikb.domain.knowledge.repository.DocumentChunkRepository;
import com.brolei.aikb.domain.knowledge.repository.KnowledgeBaseRepository;
import com.brolei.aikb.domain.knowledge.repository.KnowledgeDocumentRepository;
import com.brolei.aikb.domain.knowledge.service.FileStorage;
import com.brolei.aikb.domain.knowledge.service.TextSplitter;
import com.brolei.aikb.domain.knowledge.service.VectorStore;
import com.brolei.aikb.domain.llm.EmbeddingProvider;
import com.brolei.aikb.domain.user.model.UserId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 知识库相关用例的应用服务.
 *
 * <p>负责编排知识库的创建、文档上传与索引等用例流程。 只依赖领域层接口和 Spring，不依赖 infrastructure 实现类。
 */
@Service
public class KnowledgeApplicationService {

  private static final Logger log = LoggerFactory.getLogger(KnowledgeApplicationService.class);

  private final KnowledgeBaseRepository knowledgeBaseRepository;
  private final KnowledgeDocumentRepository knowledgeDocumentRepository;
  private final DocumentChunkRepository documentChunkRepository;
  private final FileStorage fileStorage;
  private final TextSplitter textSplitter;
  private final EmbeddingProvider embeddingProvider;
  private final VectorStore vectorStore;
  private final FileValidator fileValidator;
  private final DocumentTextExtractor documentTextExtractor;
  private final AiKbProperties aiKbProperties;

  /** 构造知识库应用服务. */
  public KnowledgeApplicationService(
      KnowledgeBaseRepository knowledgeBaseRepository,
      KnowledgeDocumentRepository knowledgeDocumentRepository,
      DocumentChunkRepository documentChunkRepository,
      FileStorage fileStorage,
      TextSplitter textSplitter,
      EmbeddingProvider embeddingProvider,
      VectorStore vectorStore,
      FileValidator fileValidator,
      DocumentTextExtractor documentTextExtractor,
      AiKbProperties aiKbProperties) {
    this.knowledgeBaseRepository = knowledgeBaseRepository;
    this.knowledgeDocumentRepository = knowledgeDocumentRepository;
    this.documentChunkRepository = documentChunkRepository;
    this.fileStorage = fileStorage;
    this.textSplitter = textSplitter;
    this.embeddingProvider = embeddingProvider;
    this.vectorStore = vectorStore;
    this.fileValidator = fileValidator;
    this.documentTextExtractor = documentTextExtractor;
    this.aiKbProperties = aiKbProperties;
  }

  /** 创建新的知识库. */
  @Transactional
  public KnowledgeBase createKnowledgeBase(UserId ownerId, String name, String description) {
    KnowledgeBase knowledgeBase = KnowledgeBase.create(ownerId, name, description);
    KnowledgeBase saved = knowledgeBaseRepository.save(knowledgeBase);
    log.info("Knowledge base created: id={}, ownerId={}", saved.id().value(), ownerId.value());
    return saved;
  }

  /** 列出当前用户的所有知识库. */
  @Transactional(readOnly = true)
  public List<KnowledgeBase> listMyKnowledgeBases(UserId ownerId) {
    return knowledgeBaseRepository.findByOwnerId(ownerId);
  }

  /**
   * 上传文档并触发索引流程.
   *
   * <p>整个流程为同步实现（v0.2），包含文件校验、文件存储、文本切分、向量嵌入与存储。 LLM 嵌入调用不在数据库事务中执行。
   */
  public KnowledgeDocument uploadAndIndexDocument(
      UserId ownerId, KnowledgeBaseId kbId, String originalFilename, byte[] fileContent) {

    // 1. 校验知识库归属
    KnowledgeBase kb =
        knowledgeBaseRepository
            .findById(kbId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
    if (!kb.ownerId().equals(ownerId)) {
      throw new BusinessException(ErrorCode.NOT_FOUND);
    }

    // 2. 文件校验（扩展名、空文件、大小）
    fileValidator.validate(
        fileContent,
        originalFilename,
        aiKbProperties.getUpload().getMaxFileSize(),
        aiKbProperties.getUpload().getAllowedExt());

    // 3. 生成文档 ID 并存储文件
    KnowledgeDocumentId docId = KnowledgeDocumentId.generate();
    String storagePath = fileStorage.store(ownerId, kbId, docId, originalFilename, fileContent);
    String contentType = deriveContentType(originalFilename);

    // 4. 创建文档并保存（状态：UPLOADED）
    KnowledgeDocument document =
        KnowledgeDocument.create(
            docId, kbId, ownerId, originalFilename, storagePath, contentType, fileContent.length);
    document = saveDocumentInTransaction(document);

    // 5. 标记为索引中（状态：INDEXING，事务内）
    document.markAsIndexing();
    saveDocumentInTransaction(document);

    try {
      // 6. 读取文本（不在事务中）
      String text = documentTextExtractor.extract(originalFilename, fileContent);

      // 7. 切分文本（不在事务中）
      List<String> chunkTexts = textSplitter.split(text);
      if (chunkTexts.isEmpty()) {
        throw new BusinessException(ErrorCode.INTERNAL_ERROR, "文本切分后没有产生任何片段");
      }

      // 8. 向量嵌入（不在事务中，LLM 调用）
      List<float[]> embeddings = embeddingProvider.embedAll(chunkTexts);

      // 9. 创建切片和向量
      List<DocumentChunk> chunks = new ArrayList<>();
      List<VectorChunk> vectorChunks = new ArrayList<>();
      for (int i = 0; i < chunkTexts.size(); i++) {
        DocumentChunk chunk = DocumentChunk.create(kbId, document.id(), i, chunkTexts.get(i));
        chunks.add(chunk);
        vectorChunks.add(
            new VectorChunk(
                UUID.randomUUID().toString(),
                kbId,
                document.id(),
                chunk.id(),
                chunkTexts.get(i),
                embeddings.get(i)));
      }

      // 10. 持久化切片与向量（事务内）
      persistChunksAndFinalize(document, chunks, vectorChunks);

      log.info(
          "Document indexed successfully: docId={}, chunks={}",
          document.id().value(),
          chunks.size());
      return document;

    } catch (BusinessException e) {
      // 业务异常直接标记失败并重新抛出
      markDocumentFailed(document, e.getMessage());
      throw e;
    } catch (Exception e) {
      // 非预期异常标记失败后包装为业务异常
      log.error("Document index failed: docId={}", document.id().value(), e);
      markDocumentFailed(document, e.getMessage());
      throw new BusinessException(ErrorCode.INTERNAL_ERROR, e.getMessage());
    }
  }

  /** 保存文档记录（事务内）. */
  @Transactional
  KnowledgeDocument saveDocumentInTransaction(KnowledgeDocument document) {
    return knowledgeDocumentRepository.save(document);
  }

  /** 持久化切片与向量并标记文档为就绪（事务内）. */
  @Transactional
  void persistChunksAndFinalize(
      KnowledgeDocument document, List<DocumentChunk> chunks, List<VectorChunk> vectorChunks) {
    documentChunkRepository.saveAll(chunks);
    vectorStore.saveAll(vectorChunks);
    document.markAsReady(chunks.size());
    knowledgeDocumentRepository.save(document);
  }

  /** 列出知识库下的文档列表（仅当前用户的文档）. */
  @Transactional(readOnly = true)
  public List<KnowledgeDocument> listDocuments(UserId ownerId, KnowledgeBaseId kbId) {
    KnowledgeBase kb =
        knowledgeBaseRepository
            .findById(kbId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
    if (!kb.ownerId().equals(ownerId)) {
      throw new BusinessException(ErrorCode.NOT_FOUND);
    }
    return knowledgeDocumentRepository.findByKnowledgeBaseIdAndOwnerId(kbId, ownerId);
  }

  /** 下载当前用户有权访问的原始文档. */
  @Transactional(readOnly = true)
  public DocumentDownload downloadDocument(
      UserId ownerId, KnowledgeBaseId kbId, KnowledgeDocumentId documentId) {
    KnowledgeDocument document =
        knowledgeDocumentRepository
            .findById(documentId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
    if (!document.ownerId().equals(ownerId) || !document.knowledgeBaseId().equals(kbId)) {
      throw new BusinessException(ErrorCode.NOT_FOUND);
    }
    byte[] content = fileStorage.read(document.storagePath());
    return new DocumentDownload(document.originalFilename(), document.contentType(), content);
  }

  /** 根据文件名扩展名推断内容类型. */
  private String deriveContentType(String filename) {
    if (filename == null) {
      return "application/octet-stream";
    }
    String lower = filename.toLowerCase();
    if (lower.endsWith(".md")) {
      return "text/markdown";
    }
    if (lower.endsWith(".txt")) {
      return "text/plain";
    }
    if (lower.endsWith(".pdf")) {
      return "application/pdf";
    }
    return "application/octet-stream";
  }

  /** 标记文档为失败状态并持久化. */
  private void markDocumentFailed(KnowledgeDocument document, String errorMessage) {
    try {
      document.markAsFailed(errorMessage);
      knowledgeDocumentRepository.save(document);
    } catch (Exception ex) {
      log.error("Failed to mark document as failed: docId={}", document.id().value(), ex);
    }
  }
}
