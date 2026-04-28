package com.brolei.aikb.infrastructure.persistence.chat.invocation;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/** LLM 调用记录持久化对象. */
@Getter
@Setter
@ToString
@TableName("invocation_logs")
public class InvocationLogPo {

  @TableId(type = IdType.INPUT)
  private String id;

  private String ownerId;
  private String knowledgeBaseId;
  private String conversationId;
  private String messageId;
  private String modelName;
  private int promptTokens;
  private int completionTokens;
  private int totalTokens;
  private long durationMs;
  private String status;
  private String errorMessage;
  private Instant createdAt;
}
