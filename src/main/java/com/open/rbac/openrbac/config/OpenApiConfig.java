package com.open.rbac.openrbac.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.Scopes;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
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
                                                .license(new License().name("MIT Trust")
                                                                .url("https://opensource.org/licenses/MIT")))
                                .externalDocs(new ExternalDocumentation()
                                                .description("Project Wiki")
                                                .url("https://github.com/raubinsharya/OpenRBAC/wiki"))
                                .addSecurityItem(new SecurityRequirement().addList("oauth2"))
                                .components(new Components()
                                                .addSecuritySchemes("oauth2",
                                                                new SecurityScheme()
                                                                                .type(SecurityScheme.Type.OAUTH2)
                                                                                .flows(new OAuthFlows()
                                                                                                .authorizationCode(
                                                                                                                new OAuthFlow()
                                                                                                                                .authorizationUrl(
                                                                                                                                                "http://localhost:4000/realms/open_rbac/protocol/openid-connect/auth")
                                                                                                                                .tokenUrl("http://localhost:4000/realms/open_rbac/protocol/openid-connect/token")
                                                                                                                                .scopes(new Scopes()
                                                                                                                                                .addString("openid",
                                                                                                                                                                "OpenID Connect")
                                                                                                                                                .addString("profile",
                                                                                                                                                                "User Profile")
                                                                                                                                                .addString("email",
                                                                                                                                                                "User Email"))))));
        }
}
