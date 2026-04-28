package com.brolei.aikb.infrastructure.persistence.user.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.brolei.aikb.common.exception.BusinessException;
import com.brolei.aikb.common.exception.ErrorCode;
import com.brolei.aikb.domain.user.model.User;
import com.brolei.aikb.domain.user.model.UserId;
import com.brolei.aikb.domain.user.repository.UserRepository;
import com.brolei.aikb.infrastructure.persistence.user.assembler.UserPoAssembler;
import com.brolei.aikb.infrastructure.persistence.user.mapper.UserMapper;
import com.brolei.aikb.infrastructure.persistence.user.po.UserPo;
import java.util.Optional;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;

/** 基于 MyBatis-Plus 的 {@link UserRepository} 实现. */
@Repository
public class UserRepositoryImpl implements UserRepository {

  private final UserMapper userMapper;
  private final UserPoAssembler assembler;

  public UserRepositoryImpl(UserMapper userMapper, UserPoAssembler assembler) {
    this.userMapper = userMapper;
    this.assembler = assembler;
  }

  @Override
  public Optional<User> findById(UserId id) {
    UserPo po = userMapper.selectById(id.value());
    return Optional.ofNullable(po).map(assembler::toDomain);
  }

  @Override
  public Optional<User> findByUsername(String username) {
    UserPo po =
        userMapper.selectOne(new LambdaQueryWrapper<UserPo>().eq(UserPo::getUsername, username));
    return Optional.ofNullable(po).map(assembler::toDomain);
  }

  @Override
  public boolean existsByUsername(String username) {
    return userMapper.exists(new LambdaQueryWrapper<UserPo>().eq(UserPo::getUsername, username));
  }

  @Override
  public User save(User user) {
    UserPo po = assembler.toPo(user);
    try {
      if (userMapper.selectById(po.getId()) != null) {
        userMapper.updateById(po);
      } else {
        userMapper.insert(po);
      }
    } catch (DuplicateKeyException e) {
      throw new BusinessException(ErrorCode.USERNAME_EXISTS);
    }
    return user;
  }
}
