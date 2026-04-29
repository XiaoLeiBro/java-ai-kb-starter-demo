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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 认证相关接口的 REST 控制器. */
@Tag(name = "用户认证", description = "用户注册、登录、当前用户信息和退出登录")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

  private final UserApplicationService userApplicationService;

  public AuthController(UserApplicationService userApplicationService) {
    this.userApplicationService = userApplicationService;
  }

  /** 注册新用户账号. */
  @Operation(summary = "注册用户", description = "创建一个 demo 用户账号，后续接口需要先登录获取 JWT Token。")
  @PostMapping("/register")
  public ResponseEntity<ApiResult<UserResponse>> register(
      @Valid @RequestBody RegisterRequest request) {
    User user =
        userApplicationService.register(request.username(), request.password(), request.email());
    return ResponseEntity.ok(ApiResult.ok(UserResponse.from(user)));
  }

  /** 认证并获取 JWT 令牌. */
  @Operation(summary = "用户登录", description = "使用用户名和密码登录，成功后返回 JWT Token 和用户信息。")
  @PostMapping("/login")
  public ResponseEntity<ApiResult<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
    LoginResult result = userApplicationService.login(request.username(), request.password());
    return ResponseEntity.ok(
        ApiResult.ok(new LoginResponse(result.token(), UserResponse.from(result.user()))));
  }

  /** 获取当前已认证用户的个人信息. */
  @Operation(summary = "获取当前用户", description = "根据 Authorization Bearer Token 查询当前登录用户信息。")
  @GetMapping("/me")
  public ResponseEntity<ApiResult<UserResponse>> me(Authentication authentication) {
    UserId userId = (UserId) authentication.getPrincipal();
    User user = userApplicationService.currentUser(userId);
    return ResponseEntity.ok(ApiResult.ok(UserResponse.from(user)));
  }

  @Operation(summary = "退出登录", description = "无状态 JWT 场景下仅返回成功，客户端应自行丢弃本地 Token。")
  @PostMapping("/logout")
  public ResponseEntity<Void> logout() {
    return ResponseEntity.noContent().build();
  }
}
