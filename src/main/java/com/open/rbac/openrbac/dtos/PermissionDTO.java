package com.open.rbac.openrbac.dtos;

import com.open.rbac.openrbac.enums.EntityStatus;
import com.open.rbac.openrbac.models.Permission;
import java.time.LocalDateTime;

public record PermissionDTO(Long id,
                            String name,
                            String description,
                            EntityStatus status,
                            LocalDateTime createdAt,
                            LocalDateTime updatedAt) {
    public static PermissionDTO from(Permission permission) {
        return new PermissionDTO(
                permission.getId(),
                permission.getName(),
                permission.getDescription(),
                permission.getStatus(),
                permission.getCreatedAt(),
                permission.getUpdatedAt()
        );
    }
}
