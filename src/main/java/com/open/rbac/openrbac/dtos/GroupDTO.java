package com.open.rbac.openrbac.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.open.rbac.openrbac.enums.EntityStatus;
import com.open.rbac.openrbac.models.Group;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record GroupDTO(
        Long id,
        String name,
        String description,
        String path,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        EntityStatus status) {
    public static GroupDTO from(Group group) {
        if (group == null)
            return null;
        return new GroupDTO(
                group.getId(),
                group.getName(),
                group.getDescription(),
                group.getPath(),
                group.getCreatedAt(),
                group.getUpdatedAt(),
                group.getStatus());
    }
}