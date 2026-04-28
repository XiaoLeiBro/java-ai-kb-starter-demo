package com.brolei.aikb.infrastructure.persistence.knowledge.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/** 知识库持久化对象. */
@Getter
@Setter
@ToString
@TableName("knowledge_bases")
public class KnowledgeBasePo {

  @TableId(type = IdType.INPUT)
  private String id;

  private String ownerId;
  private String name;
  private String description;
  private String status;
  private Instant createdAt;
  private Instant updatedAt;
}
