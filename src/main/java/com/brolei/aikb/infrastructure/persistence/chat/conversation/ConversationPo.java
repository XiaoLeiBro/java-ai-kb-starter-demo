package com.brolei.aikb.infrastructure.persistence.chat.conversation;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/** 对话会话持久化对象. */
@Getter
@Setter
@ToString
@TableName("conversations")
public class ConversationPo {

  @TableId(type = IdType.INPUT)
  private String id;

  private String ownerId;
  private String knowledgeBaseId;
  private String title;
  private String status;
  private Instant createdAt;
  private Instant updatedAt;
}
