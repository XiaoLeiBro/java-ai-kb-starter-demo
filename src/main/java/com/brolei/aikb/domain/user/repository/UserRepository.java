package com.brolei.aikb.domain.user.repository;

import com.brolei.aikb.domain.user.model.User;
import com.brolei.aikb.domain.user.model.UserId;
import java.util.Optional;

/** 用户仓储接口. */
public interface UserRepository {

  Optional<User> findById(UserId id);

  Optional<User> findByUsername(String username);

  boolean existsByUsername(String username);

  User save(User user);
}
