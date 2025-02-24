package run.bemin.api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
  @Bean
  public OpenAPI openAPI() {
    Info info = new Info()
        .title("BEmin Server API")
        .version("v1.0.0")
        .description("AI 활용 비즈니스 프로젝트 - [BEmin] 의 REST API 명세서입니다.");

    return new OpenAPI()
        .components(new Components())
        .info(info);
  }
}
