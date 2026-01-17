package com.open.rbac.openrbac.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.open.rbac.openrbac.enums.EntityStatus;
import com.open.rbac.openrbac.models.GroupRole;
import lombok.Builder;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Builder
public record GroupRoleDTO(
        @JsonUnwrapped @JsonIgnoreProperties({
                "id", "status", "name", "description", "createdAt", "updatedAt", "isSystemRole" }) RoleDTO role,
        Long roleId,
        Long groupRoleId,
        Long groupId,
        String groupName,
        String roleName,
        EntityStatus groupStatus,
        EntityStatus roleStatus,
        LocalDateTime assignedAt,
        String assignedBy,
        @JsonInclude(JsonInclude.Include.NON_NULL) LocalDateTime roleExpiryDate,
        Boolean isActive,
        Boolean isInherited,
        Long sourceGroupId,
        Boolean allowInheritance,
        Integer maxInheritanceDepth) {
    public static GroupRoleDTO from(GroupRole groupRole) {
        if (groupRole == null)
            return null;
        return GroupRoleDTO.builder()
                .groupRoleId(groupRole.getId())
                .groupId(groupRole.getGroup().getId())
                .roleId(groupRole.getRole().getId())
                .role(RoleDTO.from(groupRole.getRole()))
                .groupName(groupRole.getGroup().getName())
                .roleName(groupRole.getRole().getName())
                .groupStatus(groupRole.getGroup().getStatus())
                .roleStatus(groupRole.getRole().getStatus())
                .assignedAt(groupRole.getCreatedAt())
                .assignedBy(groupRole.getAssignedBy() != null ? groupRole.getAssignedBy().getDisplayName() : "Unknown")
                .roleExpiryDate(groupRole.getExpiryDate())
                .isActive(groupRole.getIsActive())
                .isInherited(groupRole.getIsInherited())
                .sourceGroupId(groupRole.getSourceGroup() != null ? groupRole.getSourceGroup().getId() : null)
                .allowInheritance(groupRole.getAllowInheritance())
                .maxInheritanceDepth(groupRole.getMaxInheritanceDepth())
                .build();
    }
}
