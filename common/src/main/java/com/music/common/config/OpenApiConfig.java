package com.music.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Shared OpenAPI setup: titles each service by its application name and wires a JWT
// bearer scheme so the Swagger UI "Authorize" button sends "Authorization: Bearer <token>".
@Configuration
public class OpenApiConfig {

    private static final String BEARER_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI openApi(@Value("${spring.application.name:service}") String appName) {
        return new OpenAPI()
                .info(new Info()
                        .title("SoundClown — " + appName)
                        .version("v1"))
                .components(new Components().addSecuritySchemes(BEARER_SCHEME,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME));
    }
}
