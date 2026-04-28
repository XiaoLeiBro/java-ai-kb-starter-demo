package com.brolei.aikb.infrastructure.persistence.user.assembler;

import com.brolei.aikb.domain.user.model.User;
import com.brolei.aikb.domain.user.model.UserId;
import com.brolei.aikb.domain.user.model.UserStatus;
import com.brolei.aikb.infrastructure.persistence.user.po.UserPo;
import org.springframework.stereotype.Component;

/** User 与 UserPo 之间的转换器. */
@Component
public class UserPoAssembler {

  /** 将持久化对象转换为领域对象. */
  public User toDomain(UserPo po) {
    if (po == null) {
      return null;
    }
    return User.restore(
        UserId.of(po.getId()),
        po.getUsername(),
        po.getPasswordHash(),
        po.getEmail(),
        UserStatus.valueOf(po.getStatus()),
        po.getCreatedAt(),
        po.getUpdatedAt());
  }

  /** 将领域对象转换为持久化对象. */
  public UserPo toPo(User user) {
    if (user == null) {
      return null;
    }
    UserPo po = new UserPo();
    po.setId(user.id().value());
    po.setUsername(user.username());
    po.setPasswordHash(user.passwordHash());
    po.setEmail(user.email());
    po.setStatus(user.status().name());
    po.setCreatedAt(user.createdAt());
    po.setUpdatedAt(user.updatedAt());
    return po;
  }
}
