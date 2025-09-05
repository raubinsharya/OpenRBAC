package com.open.rbac.openrbac.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
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

        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("timestamp", LocalDateTime.now().toString());
        errorDetails.put("status", 401);
        errorDetails.put("error", "Unauthorized");
        errorDetails.put("message", "Authentication required");
        
        // Only include sensitive details in development
        if ("local".equals(activeProfile)) {
            errorDetails.put("path", request.getRequestURI());
            errorDetails.put("details", authException.getMessage());
            
            // Add debug info only in development
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null) {
                errorDetails.put("debug", "No Authorization header provided");
            } else if (!authHeader.startsWith("Bearer ")) {
                errorDetails.put("debug", "Authorization header doesn't start with 'Bearer '");
            } else {
                errorDetails.put("debug", "JWT token validation failed");
            }
        }

        response.getOutputStream().println(objectMapper.writeValueAsString(errorDetails));
    }
}