package com.open.rbac.openrbac.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.open.rbac.openrbac.enums.EntityStatus;
import com.open.rbac.openrbac.models.Realm;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record RealmDTO(
        Long id,
        String name,
        String realmId,
        String description,
        EntityStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Set<UserDTO> users,
        Set<RoleDTO> roles,
        Set<PermissionDTO> permissions
) {

    /**
     * Main from method with flags
     */
    public static RealmDTO from(Realm realm, boolean includeUsers, boolean includeRoles, boolean includePermissions) {
        if (realm == null) return null;

        return new RealmDTO(
                realm.getId(),
                realm.getName(),
                realm.getRealmId(),
                realm.getDescription(),
                realm.getStatus(),
                realm.getCreatedAt(),
                realm.getUpdatedAt(),
                includeUsers
                        ? Optional.ofNullable(realm.getUsers())
                        .map(list -> list.stream().map(UserDTO::from).collect(Collectors.toSet()))
                        .orElse(Set.of())
                        : null,
                includeRoles
                        ? Optional.ofNullable(realm.getRoles())
                        .map(set -> set.stream().map(RoleDTO::from).collect(Collectors.toSet()))
                        .orElse(Set.of())
                        : null,
                includePermissions
                        ? Optional.ofNullable(realm.getPermissions())
                        .map(set -> set.stream().map(PermissionDTO::from).collect(Collectors.toSet()))
                        .orElse(Set.of())
                        : null
        );
    }

    /**
     * Overloaded from method: defaults to not including associations
     */
    public static RealmDTO from(Realm realm) {
        return from(realm, false, false, false);
    }

    /**
     * Overloaded from method: defaults to not including associations
     */
    public static RealmDTO from(Realm realm, boolean includeRoles, boolean includePermissions) {
        return from(realm, false, includeRoles, includePermissions);
    }
}
