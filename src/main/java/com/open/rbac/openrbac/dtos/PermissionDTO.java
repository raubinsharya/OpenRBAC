package com.open.rbac.openrbac.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.open.rbac.openrbac.enums.EntityStatus;
import com.open.rbac.openrbac.models.Permission;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record PermissionDTO(Long id,
                            String name,
                            String resource,
                            String action,
                            String description,
                            EntityStatus status,
                            LocalDateTime createdAt,
                            LocalDateTime updatedAt) {
    public static PermissionDTO from(Permission permission) {
        return new PermissionDTO(
                permission.getId(),
                permission.getName(),
                permission.getResource(),
                permission.getAction(),
                permission.getDescription(),
                permission.getStatus(),
                permission.getCreatedAt(),
                permission.getUpdatedAt()
        );
    }
}
