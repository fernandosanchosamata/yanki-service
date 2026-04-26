package com.ntt.yanki.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI yankiOpenApi() {
    return new OpenAPI()
        .info(
            new Info()
                .title("Yanki Service API")
                .version("v1")
                .description("Mobile wallet registration and transfer endpoints."));
  }
}
