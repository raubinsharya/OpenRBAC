package com.open.rbac.openrbac.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.open.rbac.openrbac.enums.EntityStatus;
import com.open.rbac.openrbac.models.UserPermission;
import com.open.rbac.openrbac.models.UserEffectivePermission;
import lombok.Builder;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Builder
public record UserPermissionDTO(
                @JsonUnwrapped @JsonIgnoreProperties({
                                "id", "status", "name", "description", "resource", "action", "createdAt",
                                "userId",
                                "updatedAt" }) PermissionDTO permission,
                Long permissionId,
                String resource,
                String action,
                Long id,
                Long userId,
                String userName,
                String permissionName,
                EntityStatus userStatus,
                EntityStatus permissionStatus,
                LocalDateTime assignedAt,
                String assignedBy,
                LocalDateTime expiryDate,
                Boolean isActive,
                String assignmentType) {
        public static UserPermissionDTO from(UserPermission userPermission) {
                if (userPermission == null)
                        return null;
                return UserPermissionDTO.builder()
                                .id(userPermission.getId())
                                .userId(userPermission.getUser().getId())
                                .permissionId(userPermission.getPermission().getId())
                                .resource(userPermission.getPermission().getResource())
                                .action(userPermission.getPermission().getAction())
                                .permission(PermissionDTO.from(userPermission.getPermission()))
                                .userName(userPermission.getUser().getUsername())
                                .permissionName(userPermission.getPermission().getName())
                                .userStatus(userPermission.getUser().getStatus())
                                .permissionStatus(userPermission.getPermission().getStatus())
                                .assignedAt(userPermission.getCreatedAt())
                                .assignedBy(userPermission.getAssignedBy() != null
                                                ? userPermission.getAssignedBy().getDisplayName()
                                                : "Unknown")
                                .expiryDate(userPermission.getExpiryDate())
                                .isActive(userPermission.getIsActive())
                                .assignmentType("DIRECT")
                                .build();
        }

        public static UserPermissionDTO from(UserEffectivePermission effectivePermission) {
                if (effectivePermission == null)
                        return null;
                return UserPermissionDTO.builder()
                                .id(null) // Synthetic ID inside EffectivePermission, DTO expects Long
                                .userId(effectivePermission.getUser().getId())
                                .permissionId(effectivePermission.getPermission().getId())
                                .resource(effectivePermission.getPermission().getResource())
                                .action(effectivePermission.getPermission().getAction())
                                .permission(PermissionDTO.from(effectivePermission.getPermission()))
                                .userName(effectivePermission.getUser().getUsername())
                                .permissionName(effectivePermission.getPermission().getName())
                                .userStatus(effectivePermission.getUser().getStatus())
                                .permissionStatus(effectivePermission.getPermission().getStatus())
                                .assignedAt(effectivePermission.getCreatedAt())
                                .assignedBy(effectivePermission.getAssignedBy() != null
                                                ? effectivePermission.getAssignedBy().getDisplayName()
                                                : "Unknown")
                                .expiryDate(effectivePermission.getExpiryDate())
                                .isActive(effectivePermission.getIsActive())
                                .assignmentType(effectivePermission.getAssignmentType())
                                .build();
        }
}
