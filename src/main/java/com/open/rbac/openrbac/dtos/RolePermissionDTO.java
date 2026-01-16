package com.open.rbac.openrbac.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.open.rbac.openrbac.enums.EntityStatus;
import com.open.rbac.openrbac.models.RolePermission;
import lombok.Builder;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Builder
public record RolePermissionDTO(
                @JsonUnwrapped @JsonIgnoreProperties({
                                "id", "status", "name" }) PermissionDTO permission,
                Long permissionId,
                Long rolePermissionId,
                Long roleId,
                String roleName,
                String permissionName,
                EntityStatus roleStatus,
                EntityStatus permissionStatus,
                LocalDateTime assignedAt,
                String assignedBy) {

        public static RolePermissionDTO from(RolePermission rolePermission) {
                if (rolePermission == null)
                        return null;
                return RolePermissionDTO.builder()
                                .rolePermissionId(rolePermission.getId())
                                .roleId(rolePermission.getRole() != null ? rolePermission.getRole().getId() : null)
                                .permissionId(rolePermission.getPermission().getId())
                                .permission(PermissionDTO.from(rolePermission.getPermission()))
                                .roleName(rolePermission.getRole().getName())
                                .permissionName(rolePermission.getPermission().getName())
                                .roleStatus(rolePermission.getRole().getStatus())
                                .permissionStatus(rolePermission.getPermission().getStatus())
                                .assignedAt(rolePermission.getCreatedAt())
                                .assignedBy(rolePermission.getAssignedBy() != null
                                                ? rolePermission.getAssignedBy().getDisplayName()
                                                : "Unknown")
                                .build();
        }
}
