package com.open.rbac.openrbac.requests;

import com.open.rbac.openrbac.enums.EntityStatus;
import jakarta.validation.constraints.Size;

public record UpdatePermissionRequest(
        @Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters") String name,

        @Size(min = 2, max = 50, message = "Resource must be between 2 and 50 characters") String resource,

        @Size(min = 2, max = 50, message = "Action must be between 2 and 50 characters") String action,

        String description,

        EntityStatus status) {
}
