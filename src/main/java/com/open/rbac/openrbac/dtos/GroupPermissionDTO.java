package com.open.rbac.openrbac.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.open.rbac.openrbac.enums.EntityStatus;
import com.open.rbac.openrbac.models.GroupEffectivePermission;
import com.open.rbac.openrbac.models.GroupPermission;
import lombok.Builder;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Builder
public record GroupPermissionDTO(
                @JsonUnwrapped @JsonIgnoreProperties({
                                "id", "status", "name", "createdAt",
                                "updatedAt" }) PermissionDTO permission,
                Long permissionId,
                Long groupPermissionId,
                Long groupId,
                String groupName,
                String permissionName,
                EntityStatus groupStatus,
                EntityStatus permissionStatus,
                LocalDateTime assignedAt,
                String assignedBy,
                @JsonInclude(JsonInclude.Include.NON_NULL) LocalDateTime permissionExpiryDate,
                Boolean isActive,
                Boolean isInherited,
                Long sourceGroupId,
                Boolean allowInheritance,
                Integer maxInheritanceDepth,
                String assignmentType) {

        public static GroupPermissionDTO from(GroupPermission groupPermission) {
                return from(groupPermission, null);
        }

        public static GroupPermissionDTO from(GroupPermission groupPermission, Long requestedGroupId) {
                if (groupPermission == null)
                        return null;

                boolean effectiveIsInherited = (requestedGroupId != null
                                && !groupPermission.getGroup().getId().equals(requestedGroupId))
                                || Boolean.TRUE.equals(groupPermission.getIsInherited());

                Long effectiveSourceGroupId = (requestedGroupId != null
                                && !groupPermission.getGroup().getId().equals(requestedGroupId))
                                                ? groupPermission.getGroup().getId()
                                                : (groupPermission.getSourceGroup() != null
                                                                ? groupPermission.getSourceGroup().getId()
                                                                : null);

                return GroupPermissionDTO.builder()
                                .groupPermissionId(groupPermission.getId())
                                .groupId(requestedGroupId != null ? requestedGroupId
                                                : groupPermission.getGroup().getId())
                                .permissionId(groupPermission.getPermission().getId())
                                .permission(PermissionDTO.from(groupPermission.getPermission()))
                                .groupName(groupPermission.getGroup().getName())
                                .permissionName(groupPermission.getPermission().getName())
                                .groupStatus(groupPermission.getGroup().getStatus())
                                .permissionStatus(groupPermission.getPermission().getStatus())
                                .assignedAt(groupPermission.getCreatedAt())
                                .assignedBy(groupPermission.getAssignedBy() != null
                                                ? groupPermission.getAssignedBy().getDisplayName()
                                                : "Unknown")
                                .permissionExpiryDate(groupPermission.getExpiryDate())
                                .isActive(groupPermission.getIsActive())
                                .isInherited(effectiveIsInherited)
                                .sourceGroupId(effectiveSourceGroupId)
                                .allowInheritance(groupPermission.getAllowInheritance())
                                .maxInheritanceDepth(groupPermission.getMaxInheritanceDepth())
                                .assignmentType("DIRECT")
                                .build();
        }

        public static GroupPermissionDTO from(GroupEffectivePermission effectivePermission, Long requestedGroupId) {
                if (effectivePermission == null)
                        return null;

                boolean effectiveIsInherited = (requestedGroupId != null
                                && !effectivePermission.getGroup().getId().equals(requestedGroupId));

                Long effectiveSourceGroupId = (requestedGroupId != null
                                && !effectivePermission.getGroup().getId().equals(requestedGroupId))
                                                ? effectivePermission.getGroup().getId()
                                                : null;

                // Calculate the correct assignment type
                String calculatedAssignmentType;
                if (effectiveIsInherited) {
                        // Permission is inherited from a parent group
                        calculatedAssignmentType = "INHERITED";
                } else {
                        // Use the assignment type from the view (DIRECT or ROLE)
                        calculatedAssignmentType = effectivePermission.getAssignmentType();
                }

                return GroupPermissionDTO.builder()
                                .groupPermissionId(null) // Synthetic ID
                                .groupId(requestedGroupId != null ? requestedGroupId
                                                : effectivePermission.getGroup().getId())
                                .permissionId(effectivePermission.getPermission().getId())
                                .permission(PermissionDTO.from(effectivePermission.getPermission()))
                                .groupName(effectivePermission.getGroup().getName())
                                .permissionName(effectivePermission.getPermission().getName())
                                .groupStatus(effectivePermission.getGroup().getStatus())
                                .permissionStatus(effectivePermission.getPermission().getStatus())
                                .assignedAt(effectivePermission.getCreatedAt())
                                .assignedBy(effectivePermission.getAssignedBy() != null
                                                ? effectivePermission.getAssignedBy().getDisplayName()
                                                : "Unknown")
                                .permissionExpiryDate(effectivePermission.getExpiryDate())
                                .isActive(effectivePermission.getIsActive())
                                .isInherited(effectiveIsInherited)
                                .sourceGroupId(effectiveSourceGroupId)
                                .allowInheritance(
                                                "DIRECT".equals(calculatedAssignmentType)
                                                                ? effectivePermission.getAllowInheritance()
                                                                : null)
                                .maxInheritanceDepth(
                                                "DIRECT".equals(calculatedAssignmentType)
                                                                ? effectivePermission.getMaxInheritanceDepth()
                                                                : null)
                                .assignmentType(calculatedAssignmentType)
                                .build();
        }
}
