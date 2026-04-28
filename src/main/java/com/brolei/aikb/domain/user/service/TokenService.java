package com.brolei.aikb.domain.user.service;

import com.brolei.aikb.domain.user.model.UserId;

/** Token 服务接口. */
public interface TokenService {

  String issue(UserId userId, String username);
}
