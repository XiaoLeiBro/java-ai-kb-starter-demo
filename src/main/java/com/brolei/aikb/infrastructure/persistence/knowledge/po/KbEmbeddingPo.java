package com.brolei.aikb.infrastructure.persistence.knowledge.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/** 向量嵌入持久化对象. */
@Getter
@Setter
@ToString
@TableName("kb_embeddings")
public class KbEmbeddingPo {

  @TableId(type = IdType.INPUT)
  private String id;

  private String knowledgeBaseId;
  private String documentId;
  private String chunkId;

  @TableField(typeHandler = com.brolei.aikb.infrastructure.vector.PgvectorTypeHandler.class)
  private float[] embedding;

  private Instant createdAt;

  /** 相似度分数（非数据库字段，查询结果填充）. */
  @TableField(exist = false)
  private Double score;
}
