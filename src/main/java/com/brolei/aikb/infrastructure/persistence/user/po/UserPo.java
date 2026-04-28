package com.brolei.aikb.infrastructure.persistence.user.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/** 用户持久化对象. */
@Getter
@Setter
@ToString(exclude = "passwordHash")
@TableName("users")
public class UserPo {

  @TableId(type = IdType.INPUT)
  private String id;

  private String username;
  private String passwordHash;
  private String email;
  private String status;
  private Instant createdAt;
  private Instant updatedAt;
}
