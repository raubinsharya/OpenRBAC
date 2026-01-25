package com.open.rbac.openrbac.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.open.rbac.openrbac.enums.EntityStatus;
import com.open.rbac.openrbac.models.Permission;

import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record PermissionDTO(Long id,
        String name,
        String resource,
        String action,
        String description,
        EntityStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String createdBy) {
    public static PermissionDTO from(Permission permission) {
        if (permission == null)
            return null;
        return new PermissionDTO(
                permission.getId(),
                permission.getName(),
                permission.getResource(),
                permission.getAction(),
                permission.getDescription(),
                permission.getStatus(),
                permission.getCreatedAt(),
                permission.getUpdatedAt(),
                permission.getCreatedBy() != null ? permission.getCreatedBy().getDisplayName() : null);
    }
}
