package com.brolei.aikb.infrastructure.persistence.knowledge.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/** 文档切片持久化对象. */
@Getter
@Setter
@ToString
@TableName("document_chunks")
public class DocumentChunkPo {

  @TableId(type = IdType.INPUT)
  private String id;

  private String knowledgeBaseId;
  private String documentId;
  private Integer chunkIndex;
  private String content;
  private Integer charCount;
  private Instant createdAt;
}
