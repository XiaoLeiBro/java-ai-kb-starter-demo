package com.brolei.aikb;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/** Spring Boot 应用启动入口. */
@SpringBootApplication
@MapperScan(
    value = "com.brolei.aikb.infrastructure.persistence",
    annotationClass = org.apache.ibatis.annotations.Mapper.class)
@ConfigurationPropertiesScan("com.brolei.aikb.common.config")
public class AiKbApplication {

  public static void main(String[] args) {
    SpringApplication.run(AiKbApplication.class, args);
  }
}
