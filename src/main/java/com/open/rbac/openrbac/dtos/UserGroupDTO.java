package com.open.rbac.openrbac.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.open.rbac.openrbac.enums.EntityStatus;
import com.open.rbac.openrbac.models.UserGroup;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Builder
public record UserGroupDTO(
        @JsonUnwrapped @JsonIgnoreProperties("id") UserDTO user,
        Long userId,
        Long groupMemberId,
        Long groupId,
        String groupName,
        EntityStatus groupStatus,
        LocalDateTime groupMemberExpiry,
        LocalDateTime assignedAt,
        String assignedBy,
        boolean isGroupMembershipExpired,
        boolean isGroupMembershipValid) {

    public static UserGroupDTO from(UserGroup userGroup) {
        return UserGroupDTO.builder()
                .groupMemberId(userGroup.getId())
                .groupId(userGroup.getGroup() != null ? userGroup.getGroup().getId() : null)
                .userId(userGroup.getUser().getId())
                .user(UserDTO.from(userGroup.getUser()))
                .groupName(userGroup.getGroup().getName())
                .groupStatus(userGroup.getGroup().getStatus())
                .groupMemberExpiry(userGroup.getExpiryDate())
                .isGroupMembershipExpired(userGroup.isExpired())
                .isGroupMembershipValid(userGroup.isValid())
                .assignedAt(userGroup.getCreatedAt())
                .assignedBy(userGroup.getAssignedBy() != null ? userGroup.getAssignedBy().getDisplayName() : "Unknown")
                .build();
    }
}