package com.open.rbac.openrbac.dtos;

import com.open.rbac.openrbac.enums.EntityStatus;
import com.open.rbac.openrbac.models.User;

public record UserDTO(Long id, String firstName, String lastName, String email, String username, EntityStatus status) {
    public static UserDTO from(User user) {
        return new UserDTO(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getUsername(),
                user.getStatus());
    }
}