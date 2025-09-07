package com.open.rbac.openrbac.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.open.rbac.openrbac.models.Permission;
import com.open.rbac.openrbac.models.User;
import lombok.Builder;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public record MeDTO(
        @JsonUnwrapped UserDTO user,
        RealmDTO realm,
        List<RoleDTO> roles,
        List<PermissionDTO> permissions
        ) {

    public static MeDTO from(User user) {
        return MeDTO.builder()
                .user(UserDTO.from(user))
                .realm(RealmDTO.from(user.getRealm()))
                .roles(Optional.ofNullable(user.getRoles()).orElse(Set.of()).stream().map(RoleDTO::from).collect(Collectors.toList()))
                .permissions(Optional.ofNullable(user.getPermissions()).orElse(Set.of()).stream().map(PermissionDTO::from).collect(Collectors.toList()))
                .build();
    }
}
