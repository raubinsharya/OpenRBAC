package com.open.rbac.openrbac.requests;

import com.open.rbac.openrbac.enums.EntityStatus;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record UpdateRoleRequest(
        @Size(min = 2, max = 100, message = "Role name must be between 2 and 100 characters")
        String name,
        
        String description,
        
        EntityStatus status,
        
        Set<Long> permissionIds
) {
}