package com.open.rbac.openrbac.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    @Value("${keycloak.base-url:http://localhost:4000}")
    private String keycloakBaseUrl;

    private final CustomAuthenticationEntryPoint authenticationEntryPoint;
    private final CustomAccessDeniedHandler accessDeniedHandler;

    private final ConcurrentHashMap<String, JwtDecoder> jwtDecoders = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                .anyRequest().authenticated()
            )
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(authenticationEntryPoint)
                .accessDeniedHandler(accessDeniedHandler)
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.decoder(multiRealmJwtDecoder()))
                .authenticationEntryPoint(authenticationEntryPoint)
            );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        List<String> allowedOrigins = List.of(
            "http://localhost:4200",
            "http://localhost:3000",
            "http://127.0.0.1:4200"
        );

        configuration.setAllowedOrigins(allowedOrigins);
        configuration.setAllowedMethods(List.of(
            HttpMethod.GET.name(),
            HttpMethod.POST.name(),
            HttpMethod.PUT.name(),
            HttpMethod.DELETE.name(),
            HttpMethod.PATCH.name(),
            HttpMethod.OPTIONS.name()
        ));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(List.of("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public JwtDecoder multiRealmJwtDecoder() {
        return token -> {
            try {
                log.debug("Attempting to decode JWT token...");

                // Decode payload
                String[] parts = token.split("\\.");
                if (parts.length != 3) {
                    throw new JwtException("Invalid JWT format");
                }

                String payloadJsonStr = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
                JsonNode payload = objectMapper.readTree(payloadJsonStr);
                String issuer = payload.get("iss").asText();
                String realm = extractRealmFromIssuer(issuer);

                log.debug("Decoded JWT issuer: {}, realm: {}", issuer, realm);

                // Get or create decoder for this realm
                JwtDecoder decoder = jwtDecoders.computeIfAbsent(realm, this::createDecoderForRealm);
                var jwt = decoder.decode(token);

                log.debug("JWT successfully decoded for realm: {}", realm);
                return jwt;

            } catch (Exception e) {
                log.error("JWT decoding failed: {}", e.getMessage(), e);
                throw new JwtException("JWT validation failed: " + e.getMessage(), e);
            }
        };
    }

    private JwtDecoder createDecoderForRealm(String realm) {
        String jwkSetUri = keycloakBaseUrl + "/realms/" + realm + "/protocol/openid-connect/certs";
        log.debug("Creating JwtDecoder for realm {} with JWK URI: {}", realm, jwkSetUri);
        return NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
    }

    private String extractRealmFromIssuer(String issuer) {
        // Expect issuer format: http://localhost:4000/realms/{realmName}
        String[] parts = issuer.split("/realms/");
        if (parts.length < 2 || parts[1].isEmpty()) {
            throw new JwtException("Cannot extract realm from issuer: " + issuer);
        }
        return parts[1];
    }
}
