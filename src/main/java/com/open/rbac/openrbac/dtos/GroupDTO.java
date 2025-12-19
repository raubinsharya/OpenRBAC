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
        Long parentGroupId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        EntityStatus status,
        java.util.List<GroupDTO> children,
        GroupDTO parent) {

    public static GroupDTO from(Group group) {
        return from(group, null, null);
    }

    public static GroupDTO from(Group group, java.util.List<GroupDTO> children) {
        return from(group, children, null);
    }

    public static GroupDTO from(Group group, java.util.List<GroupDTO> children, GroupDTO parent) {
        if (group == null)
            return null;
        return new GroupDTO(
                group.getId(),
                group.getName(),
                group.getDescription(),
                group.getPath(),
                group.getParentGroup() != null ? group.getParentGroup().getId() : null,
                group.getCreatedAt(),
                group.getUpdatedAt(),
                group.getStatus(),
                children,
                parent);
    }
}