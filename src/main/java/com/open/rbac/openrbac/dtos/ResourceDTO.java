package com.open.rbac.openrbac.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.open.rbac.openrbac.enums.EntityStatus;
import com.open.rbac.openrbac.models.Permission;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ResourceDTO(Long id,
                          String resource,
                          String description,
                          EntityStatus status,
                          LocalDateTime createdAt,
                          LocalDateTime updatedAt) {
    public static ResourceDTO from(Permission permission) {
        if (permission == null) return null;
        return new ResourceDTO(
                permission.getId(),
                permission.getResource(),
                permission.getDescription(),
                permission.getStatus(),
                permission.getCreatedAt(),
                permission.getUpdatedAt()
        );
    }
}
