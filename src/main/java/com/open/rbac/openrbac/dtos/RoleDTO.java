package com.open.rbac.openrbac.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.open.rbac.openrbac.enums.EntityStatus;
import com.open.rbac.openrbac.models.Role;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record RoleDTO(
        Long id,
        String name,
        String description,
        EntityStatus status,
        Boolean isSystemRole,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static RoleDTO from(Role role) {
        if (role == null) return null;
        return new RoleDTO(
                role.getId(),
                role.getName(),
                role.getDescription(),
                role.getStatus(),
                role.getIsSystemRole(),
                role.getCreatedAt(),
                role.getUpdatedAt()
        );
    }
}