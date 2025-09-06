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
        return new RealmDTO(
                realm.getId(),
                realm.getName(),
                realm.getRealmId(),
                realm.getDescription(),
                realm.getStatus(),
                realm.getCreatedAt(),
                realm.getUpdatedAt(),
                null);
    }

    public static RealmDTO fromWithUsers(Realm realm) {
        return new RealmDTO(
                realm.getId(),
                realm.getName(),
                realm.getRealmId(),
                realm.getDescription(),
                realm.getStatus(),
                realm.getCreatedAt(),
                realm.getUpdatedAt(),
                Optional.ofNullable(realm.getUsers())
                        .map(users -> users.stream().map(UserDTO::from).toList())
                        .orElse(List.of()));
    }
}