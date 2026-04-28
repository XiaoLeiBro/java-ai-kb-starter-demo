package com.brolei.aikb.infrastructure.persistence.chat.message;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/** 对话消息持久化对象. */
@Getter
@Setter
@ToString
@TableName("messages")
public class MessagePo {

  @TableId(type = IdType.INPUT)
  private String id;

  private String conversationId;
  private String role;
  private String content;
  private Instant createdAt;
}
