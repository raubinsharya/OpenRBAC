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
        Boolean isActive,
        String assignmentType,
        Long sourceGroupId) {

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
                .assignmentType("DIRECT")
                .sourceGroupId(null)
                .build();
    }

    public static UserRoleDTO from(com.open.rbac.openrbac.models.GroupRole groupRole,
            com.open.rbac.openrbac.models.UserGroup userGroup,
            Long effectiveSourceGroupId) {
        if (groupRole == null || userGroup == null)
            return null;

        // Effective expiry is the sooner of the UserGroup membership expiry or the
        // GroupRole assignment expiry
        LocalDateTime effectiveExpiry = null;
        if (groupRole.getExpiryDate() != null && userGroup.getExpiryDate() != null) {
            effectiveExpiry = groupRole.getExpiryDate().isBefore(userGroup.getExpiryDate())
                    ? groupRole.getExpiryDate()
                    : userGroup.getExpiryDate();
        } else if (groupRole.getExpiryDate() != null) {
            effectiveExpiry = groupRole.getExpiryDate();
        } else if (userGroup.getExpiryDate() != null) {
            effectiveExpiry = userGroup.getExpiryDate();
        }

        // Active only if both are active (and expiry check passes)
        boolean isActive = Boolean.TRUE.equals(groupRole.getIsActive())
                && Boolean.TRUE.equals(userGroup.getIsActive())
                && (effectiveExpiry == null || effectiveExpiry.isAfter(LocalDateTime.now()));

        return UserRoleDTO.builder()
                .userRoleId(null) // Synthetic
                .userId(userGroup.getUser().getId())
                .roleId(groupRole.getRole().getId())
                .role(RoleDTO.from(groupRole.getRole()))
                .userName(userGroup.getUser().getUsername())
                .roleName(groupRole.getRole().getName())
                .userStatus(userGroup.getUser().getStatus())
                .roleStatus(groupRole.getRole().getStatus())
                .assignedAt(userGroup.getCreatedAt()) // Using UserGroup creation as assignment time
                .assignedBy(groupRole.getAssignedBy() != null ? groupRole.getAssignedBy().getDisplayName() : "Unknown")
                .expiryDate(effectiveExpiry)
                .isActive(isActive)
                .assignmentType("GROUP")
                .sourceGroupId(effectiveSourceGroupId)
                .build();
    }

    public static UserRoleDTO from(com.open.rbac.openrbac.models.UserEffectiveRole effectiveRole) {
        if (effectiveRole == null)
            return null;
        return UserRoleDTO.builder()
                .userRoleId(null)
                .userId(effectiveRole.getUser().getId())
                .roleId(effectiveRole.getRole().getId())
                .role(RoleDTO.from(effectiveRole.getRole()))
                .userName(effectiveRole.getUser().getUsername())
                .roleName(effectiveRole.getRole().getName())
                .userStatus(effectiveRole.getUser().getStatus())
                .roleStatus(effectiveRole.getRole().getStatus())
                .assignedAt(effectiveRole.getCreatedAt())
                .assignedBy(effectiveRole.getAssignedBy() != null ? effectiveRole.getAssignedBy().getDisplayName()
                        : "Unknown")
                .expiryDate(effectiveRole.getExpiryDate())
                .isActive(effectiveRole.getIsActive())
                .assignmentType(effectiveRole.getAssignmentType())
                .sourceGroupId(effectiveRole.getSourceGroup() != null ? effectiveRole.getSourceGroup().getId() : null)
                .build();
    }
}
