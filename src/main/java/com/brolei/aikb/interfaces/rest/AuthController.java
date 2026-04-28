package com.brolei.aikb.interfaces.rest;

import com.brolei.aikb.application.user.LoginResult;
import com.brolei.aikb.application.user.UserApplicationService;
import com.brolei.aikb.domain.user.model.User;
import com.brolei.aikb.domain.user.model.UserId;
import com.brolei.aikb.interfaces.dto.ApiResult;
import com.brolei.aikb.interfaces.dto.auth.LoginRequest;
import com.brolei.aikb.interfaces.dto.auth.LoginResponse;
import com.brolei.aikb.interfaces.dto.auth.RegisterRequest;
import com.brolei.aikb.interfaces.dto.auth.UserResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 认证相关接口的 REST 控制器. */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

  private final UserApplicationService userApplicationService;

  public AuthController(UserApplicationService userApplicationService) {
    this.userApplicationService = userApplicationService;
  }

  /** 注册新用户账号. */
  @PostMapping("/register")
  public ResponseEntity<ApiResult<UserResponse>> register(
      @Valid @RequestBody RegisterRequest request) {
    User user =
        userApplicationService.register(request.username(), request.password(), request.email());
    return ResponseEntity.ok(ApiResult.ok(UserResponse.from(user)));
  }

  /** 认证并获取 JWT 令牌. */
  @PostMapping("/login")
  public ResponseEntity<ApiResult<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
    LoginResult result = userApplicationService.login(request.username(), request.password());
    return ResponseEntity.ok(
        ApiResult.ok(new LoginResponse(result.token(), UserResponse.from(result.user()))));
  }

  /** 获取当前已认证用户的个人信息. */
  @GetMapping("/me")
  public ResponseEntity<ApiResult<UserResponse>> me(Authentication authentication) {
    UserId userId = (UserId) authentication.getPrincipal();
    User user = userApplicationService.currentUser(userId);
    return ResponseEntity.ok(ApiResult.ok(UserResponse.from(user)));
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout() {
    return ResponseEntity.noContent().build();
  }
}
