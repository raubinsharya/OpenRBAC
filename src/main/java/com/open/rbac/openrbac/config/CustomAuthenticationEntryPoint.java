package com.open.rbac.openrbac.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.open.rbac.openrbac.responses.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${spring.profiles.active:prod}")
    private String activeProfile;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        Map<String, String> debugInfo = new HashMap<>();
        if ("local".equals(activeProfile)) {
            debugInfo.put("details", authException.getMessage() != null ? authException.getMessage() : "No details");
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null) debugInfo.put("debug", "No Authorization header provided");
            else if (!authHeader.startsWith("Bearer ")) debugInfo.put("debug", "Authorization header doesn't start with 'Bearer '");
            else debugInfo.put("debug", "JWT token validation failed");
        }

        ErrorResponse errorResponse = ErrorResponse.builder()
                .message("Authentication required")
                .status(HttpStatus.UNAUTHORIZED.value())
                .timestamp(new Date())
                .path(request.getRequestURI())
                .errors(debugInfo.isEmpty() ? null : debugInfo)
                .build();

        response.getOutputStream().println(objectMapper.writeValueAsString(errorResponse));
    }
}
