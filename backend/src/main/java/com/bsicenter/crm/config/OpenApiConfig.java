package com.bsicenter.crm.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
  @Bean
  OpenAPI crmApi() {
    return new OpenAPI()
        .info(
            new Info()
                .title("BsiCRM API")
                .version("1.3.0")
                .description(
                    "ورود از /api/auth/login، سپس قراردادن accessToken در Authorize. تمام مبالغ"
                        + " تومان هستند. عملیات تجاری نقش و شعبه را کنترل می‌کنند."))
        .servers(List.of(new Server().url("/")))
        .components(
            new Components()
                .addSecuritySchemes(
                    "bearerAuth",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT"))
                .addSecuritySchemes(
                    "smsCallback",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.HEADER)
                        .name("X-SMS-Secret")))
        .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
  }

  @Bean
  OpenApiCustomizer securityDocumentation() {
    return api ->
        api.getPaths()
            .forEach(
                (path, item) ->
                    item.readOperations()
                        .forEach(
                            operation -> {
                              if (List.of(
                                          "/api/auth/login",
                                          "/api/auth/refresh",
                                          "/api/auth/logout")
                                      .contains(path)
                                  || path.startsWith("/api/public/"))
                                operation.setSecurity(List.of());
                              if (path.equals("/api/integrations/sms/delivery"))
                                operation.setSecurity(
                                    List.of(new SecurityRequirement().addList("smsCallback")));
                            }));
  }
}
