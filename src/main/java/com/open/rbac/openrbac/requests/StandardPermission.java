package com.open.rbac.openrbac.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record StandardPermission(
        @NotBlank(message = "Resource is required")
        @Size(min = 2, max = 100, message = "Resource must be between 2 and 100 characters")
        String resource,
        String description) {

}
