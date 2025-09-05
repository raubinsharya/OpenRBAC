package com.open.rbac.openrbac.config;

import lombok.RequiredArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Value("${keycloak.base-url:http://localhost:4000}")
    private String keycloakBaseUrl;

    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final ConcurrentHashMap<String, JwtDecoder> jwtDecoders = new ConcurrentHashMap<>();

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/api/public/**").permitAll()
                        .requestMatchers("/actuator/health").permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(customAuthenticationEntryPoint))
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.decoder(multiRealmJwtDecoder()))
                        .authenticationEntryPoint(customAuthenticationEntryPoint));

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Allow specific origins
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:4200",
                "http://localhost:3000",
                "http://127.0.0.1:4200"));

        // Allow all HTTP methods
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

        // Allow all headers
        configuration.setAllowedHeaders(Arrays.asList("*"));

        // Allow credentials (for JWT tokens)
        configuration.setAllowCredentials(true);

        // Expose Authorization header to frontend
        configuration.setExposedHeaders(Arrays.asList("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    @Bean
    public JwtDecoder multiRealmJwtDecoder() {
        return token -> {
            try {
                System.out.println("🔍 Attempting to decode JWT token...");

                // Extract realm from issuer claim
                String[] parts = token.split("\\.");
                if (parts.length != 3) {
                    System.out.println("❌ Invalid JWT format - expected 3 parts, got: " + parts.length);
                    throw new JwtException("Invalid JWT format");
                }

                // Decode payload to get issuer
                String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
                System.out.println("📄 JWT Payload: " + payload);

                String issuer = extractIssuerFromPayload(payload);
                String realm = extractRealmFromIssuer(issuer);

                System.out.println("🏛️ Extracted realm: " + realm + " from issuer: " + issuer);
                System.out.println(
                        "🔗 JWK Set URI: " + keycloakBaseUrl + "/realms/" + realm + "/protocol/openid-connect/certs");

                // Get or create decoder for this realm
                JwtDecoder decoder = jwtDecoders.computeIfAbsent(realm, this::createDecoderForRealm);
                var jwt = decoder.decode(token);

                System.out.println("✅ JWT successfully decoded for realm: " + realm);
                return jwt;

            } catch (Exception e) {
                System.out.println("❌ JWT decoding failed: " + e.getMessage());
                e.printStackTrace();
                throw new JwtException("JWT validation failed: " + e.getMessage(), e);
            }
        };
    }

    private JwtDecoder createDecoderForRealm(String realm) {
        String jwkSetUri = keycloakBaseUrl + "/realms/" + realm + "/protocol/openid-connect/certs";
        return NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
    }

    private String extractIssuerFromPayload(String payload) {
        // Simple JSON parsing for issuer - in production use proper JSON parser
        int issStart = payload.indexOf("\"iss\":\"") + 7;
        int issEnd = payload.indexOf("\"", issStart);
        return payload.substring(issStart, issEnd);
    }

    private String extractRealmFromIssuer(String issuer) {
        // Extract realm from issuer URL: http://localhost:4000/realms/REALM_NAME
        String[] parts = issuer.split("/realms/");
        return parts.length > 1 ? parts[1] : "master";
    }
}