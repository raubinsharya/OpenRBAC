package com.open.rbac.openrbac.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.open.rbac.openrbac.enums.EntityStatus;
import com.open.rbac.openrbac.models.UserRole;
import lombok.Builder;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Builder
public record UserRoleDTO(
        @JsonUnwrapped @JsonIgnoreProperties({
                "id", "status", "name", "description", "createdAt", "updatedAt", "isSystemRole" }) RoleDTO role,
        Long roleId,
        Long userRoleId,
        Long userId,
        String userName,
        String roleName,
        EntityStatus userStatus,
        EntityStatus roleStatus,
        LocalDateTime assignedAt,
        String assignedBy,
        LocalDateTime expiryDate,
        Boolean isActive) {
    public static UserRoleDTO from(UserRole userRole) {
        if (userRole == null)
            return null;
        return UserRoleDTO.builder()
                .userRoleId(userRole.getId())
                .userId(userRole.getUser().getId())
                .roleId(userRole.getRole().getId())
                .role(RoleDTO.from(userRole.getRole()))
                .userName(userRole.getUser().getUsername())
                .roleName(userRole.getRole().getName())
                .userStatus(userRole.getUser().getStatus())
                .roleStatus(userRole.getRole().getStatus())
                .assignedAt(userRole.getCreatedAt())
                .assignedBy(userRole.getAssignedBy() != null ? userRole.getAssignedBy().getDisplayName() : "Unknown")
                .expiryDate(userRole.getExpiryDate())
                .isActive(userRole.getIsActive())
                .build();
    }
}
