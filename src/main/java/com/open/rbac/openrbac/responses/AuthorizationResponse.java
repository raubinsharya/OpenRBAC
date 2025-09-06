package com.open.rbac.openrbac.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthorizationResponse {
    private boolean allowed;
    private String userId;
    private String resource;
    private String action;
    private String reason;
    @Builder.Default
    private String timestamp = java.time.LocalDateTime.now().toString();
}