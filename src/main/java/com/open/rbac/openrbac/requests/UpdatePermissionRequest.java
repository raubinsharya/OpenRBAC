package com.open.rbac.openrbac.requests;

import com.open.rbac.openrbac.enums.EntityStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdatePermissionRequest(
        @NotBlank(message = "Name is required for updates") @Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters") String name,

        @NotBlank(message = "Resource is required for updates") @Size(min = 2, max = 50, message = "Resource must be between 2 and 50 characters") String resource,

        @NotBlank(message = "Action is required for updates") @Size(min = 2, max = 50, message = "Action must be between 2 and 50 characters") String action,

        String description,

        @NotNull(message = "Status cannot be null") EntityStatus status) {
}
