package com.brolei.aikb.infrastructure.persistence.knowledge.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/** 知识文档持久化对象. */
@Getter
@Setter
@ToString
@TableName("knowledge_documents")
public class KnowledgeDocumentPo {

  @TableId(type = IdType.INPUT)
  private String id;

  private String knowledgeBaseId;
  private String ownerId;
  private String originalFilename;
  private String storagePath;
  private String contentType;
  private Long fileSize;
  private String status;
  private Integer chunkCount;
  private String errorMessage;
  private Instant createdAt;
  private Instant updatedAt;
}
