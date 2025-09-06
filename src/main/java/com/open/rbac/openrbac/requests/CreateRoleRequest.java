package com.open.rbac.openrbac.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record CreateRoleRequest(
        @NotBlank(message = "Role name is required")
        @Size(min = 2, max = 100, message = "Role name must be between 2 and 100 characters")
        String name,
        
        String description,
        
        @NotNull(message = "Realm ID is required")
        Long realmId,
        
        Set<Long> permissionIds
) {
}