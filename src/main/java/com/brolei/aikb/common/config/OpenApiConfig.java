package com.brolei.aikb.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Swagger/OpenAPI 文档配置. */
@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI aiKbOpenApi() {
    return new OpenAPI()
        .info(
            new Info()
                .title("AI 知识库商业 Demo API")
                .version("v0.4")
                .description("用于演示知识库创建、文档上传、RAG 问答、对话历史和调用记录的后端接口。"));
  }
}
