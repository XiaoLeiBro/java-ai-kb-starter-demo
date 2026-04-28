package com.brolei.aikb.infrastructure.security;

import com.brolei.aikb.common.config.AiKbProperties;
import com.brolei.aikb.domain.user.model.UserId;
import com.brolei.aikb.domain.user.service.TokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Component;

/** 签发和解析 JWT Token 的服务. */
@Component
public class JwtService implements TokenService {

  private final AiKbProperties properties;

  public JwtService(AiKbProperties properties) {
    this.properties = properties;
  }

  /** 为指定用户签发已签名的 JWT. */
  public String issue(UserId userId, String username) {
    Instant now = Instant.now();
    Instant exp = now.plusSeconds(properties.getSecurity().getJwt().getExpirationMinutes() * 60L);
    SecretKeySpec key = signingKey();

    return Jwts.builder()
        .subject(userId.value())
        .claim("username", username)
        .issuedAt(Date.from(now))
        .expiration(Date.from(exp))
        .signWith(key)
        .compact();
  }

  /** 解析并校验 JWT，如果有效则返回其 claims. */
  public Optional<Claims> parse(String token) {
    try {
      SecretKeySpec key = signingKey();
      Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
      return Optional.of(claims);
    } catch (ExpiredJwtException e) {
      return Optional.empty();
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  private SecretKeySpec signingKey() {
    String secret = properties.getSecurity().getJwt().getSecret();
    byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
    return new SecretKeySpec(keyBytes, "HmacSHA256");
  }
}
