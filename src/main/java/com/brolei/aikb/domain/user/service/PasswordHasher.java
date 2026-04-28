package com.brolei.aikb.domain.user.service;

/** 密码哈希服务接口. */
public interface PasswordHasher {

  String hash(String rawPassword);

  boolean matches(String rawPassword, String encodedPassword);
}
