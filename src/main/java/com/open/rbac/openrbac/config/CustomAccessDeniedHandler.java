package com.open.rbac.openrbac.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.open.rbac.openrbac.responses.ErrorResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");

        ErrorResponse errorResponse = ErrorResponse.builder()
                .message("Forbidden")
                .status(HttpStatus.FORBIDDEN.value())
                .timestamp(new Date())
                .build();

        response.getWriter().write(new ObjectMapper().writeValueAsString(errorResponse));
    }
}
