package com.open.rbac.openrbac.dtos;

import com.open.rbac.openrbac.models.User;

public record UserDTO(Long id, String firstName, String lastName, String email, String username) {
    public static UserDTO from(User user) {
        return new UserDTO(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getUsername());
    }
}