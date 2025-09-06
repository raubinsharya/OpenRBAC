package com.open.rbac.openrbac.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.open.rbac.openrbac.enums.EntityStatus;
import com.open.rbac.openrbac.models.Realm;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record RealmDTO(
        Long id,
        String name,
        String realmId,
        String description,
        EntityStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<UserDTO> users) {
    public static RealmDTO from(Realm realm) {
        return from(realm, false);
    }

    public static RealmDTO fromWithUsers(Realm realm) {
        return from(realm, true);
    }

    private static RealmDTO from(Realm realm, boolean includeUsers) {
        List<UserDTO> userDTOs = Optional.of(includeUsers)
                .filter(Boolean::booleanValue) // only proceed if true
                .map(flag -> Optional.ofNullable(realm.getUsers())
                        .orElseGet(List::of)
                        .stream()
                        .map(UserDTO::from)
                        .toList())
                .orElseGet(List::of);
        return new RealmDTO(
                realm.getId(),
                realm.getName(),
                realm.getRealmId(),
                realm.getDescription(),
                realm.getStatus(),
                realm.getCreatedAt(),
                realm.getUpdatedAt(),
                userDTOs);
    }
}
