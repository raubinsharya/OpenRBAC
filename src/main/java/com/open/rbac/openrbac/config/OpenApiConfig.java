package com.open.rbac.openrbac.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openRBACAPI() {
        return new OpenAPI()
                .info(new Info().title("OpenRBAC API")
                        .description("High-performance, Role-Based Access Control (RBAC) engine")
                        .version("v0.0.1")
                        .license(new License().name("MIT Trust").url("https://opensource.org/licenses/MIT")))
                .externalDocs(new ExternalDocumentation()
                        .description("Project Wiki")
                        .url("https://github.com/raubinsharya/OpenRBAC/wiki"))
                .addSecurityItem(new io.swagger.v3.oas.models.security.SecurityRequirement().addList("bearer-jwt"))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("bearer-jwt",
                                new io.swagger.v3.oas.models.security.SecurityScheme()
                                        .type(io.swagger.v3.oas.models.security.SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }
}
