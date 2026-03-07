package com.open.rbac.openrbac.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.open.rbac.openrbac.enums.EntityStatus;
import com.open.rbac.openrbac.models.User;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public record UserDTO(
        Long id,
        String keycloakUserId,
        String firstName,
        String lastName,
        String email,
        String username,
        EntityStatus status,
        String displayName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime accountExpiryDate,
        boolean isAccountExpired,
        RealmDTO realm,
        Set<RoleDTO> roles,
        Set<PermissionDTO> permissions) {

    // Main method with flags
    public static UserDTO from(User user, boolean includeRealm) {
        if (user == null)
            return null;
        return UserDTO.builder()
                .id(user.getId())
                .keycloakUserId(user.getKeycloakUserId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .username(user.getUsername())
                .status(user.getStatus())
                .displayName(user.getDisplayName())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .accountExpiryDate(user.getAccountExpiryDate())
                .isAccountExpired(user.isAccountExpired())
                .realm(includeRealm
                        ? Optional.ofNullable(user.getRealm()).map(RealmDTO::from).orElse(null)
                        : null)
                .build();
    }

    // Overloaded method: only User
    public static UserDTO from(User user) {
        return UserDTO.from(user, false);
    }
}
