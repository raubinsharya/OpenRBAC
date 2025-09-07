package com.open.rbac.openrbac.dtos;

import com.open.rbac.openrbac.enums.EntityStatus;
import com.open.rbac.openrbac.models.User;

import java.time.LocalDateTime;

public record UserDTO(Long id, String keycloakUserId, String firstName, String lastName, String email, String username, EntityStatus status,
                      String displayName, LocalDateTime createdAt, LocalDateTime updatedAt) {
    public static UserDTO from(User user) {
        return new UserDTO(
                user.getId(),
                user.getKeycloakUserId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getUsername(),
                user.getStatus(),
                user.getDisplayName(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}