package com.open.rbac.openrbac.requests;

import com.open.rbac.openrbac.enums.EntityStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateGroupRequest(
        @NotBlank(message = "Group name is required for updates") @Size(min = 2, max = 100, message = "Group name must be between 2 and 100 characters") String name,

        String description,

        @NotNull(message = "Status cannot be null") EntityStatus status) {
}
