package com.open.rbac.openrbac.specifications;

import com.open.rbac.openrbac.models.Permission;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class PermissionSpecification {

    public static Specification<Permission> hasId(Long permissionId) {
        if (permissionId == null)
            return null;
        return (root, query, cb) -> cb.equal(root.get("id"), permissionId);
    }

    public static Specification<Permission> hasStatus(String status) {
        return (root, query, cb) -> {
            if (status == null || status.trim().isEmpty())
                return null;
            return cb.equal(cb.lower(root.get("status")), status.toLowerCase());
        };
    }

    public static Specification<Permission> hasRealmId(Long realmId) {
        return (root, query, cb) -> {
            if (realmId == null)
                return null;
            return cb.equal(root.get("realm").get("id"), realmId);
        };
    }

    public static Specification<Permission> hasResource(String resource) {
        return (root, query, cb) -> {
            if (resource == null || resource.trim().isEmpty())
                return null;
            return cb.equal(cb.lower(root.get("resource")), resource.toLowerCase());
        };
    }

    public static Specification<Permission> hasAction(String action) {
        return (root, query, cb) -> {
            if (action == null || action.trim().isEmpty())
                return null;
            return cb.equal(cb.lower(root.get("action")), action.toLowerCase());
        };
    }

    public static Specification<Permission> ofUser(String userName) {
        return (root, query, cb) -> {
            if (userName == null || userName.isEmpty())
                return null;
            // Join with users collection to get permissions directly assigned to user
            var users = root.join("users");
            return cb.equal(cb.lower(users.get("username")), userName.toLowerCase());
        };
    }

    public static Specification<Permission> searchByNameIgnoreCase(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isEmpty()) {
                return null; // no filtering if search is empty
            }
            return cb.like(
                    cb.lower(root.get("name")),
                    "%" + search.toLowerCase() + "%" // match anywhere in the string
            );
        };
    }

    public static Specification<Permission> hasCreatedAfter(LocalDateTime dateTime) {
        return (root, query, cb) -> {
            if (dateTime == null)
                return null;
            return cb.greaterThan(root.get("createdAt"), dateTime);
        };
    }

    public static Specification<Permission> hasCreatedBefore(LocalDateTime dateTime) {
        return (root, query, cb) -> {
            if (dateTime == null)
                return null;
            return cb.lessThan(root.get("createdAt"), dateTime);
        };
    }

    public static Specification<Permission> hasUpdatedAfter(LocalDateTime dateTime) {
        return (root, query, cb) -> {
            if (dateTime == null)
                return null;
            return cb.greaterThan(root.get("updatedAt"), dateTime);
        };
    }

    public static Specification<Permission> hasUpdatedBefore(LocalDateTime dateTime) {
        return (root, query, cb) -> {
            if (dateTime == null)
                return null;
            return cb.lessThan(root.get("updatedAt"), dateTime);
        };
    }

    public static Specification<Permission> hasRealm(Long realmId) {
        return (root, query, cb) -> {
            if (realmId == null)
                return null;
            var realmJoin = root.join("realm");
            return cb.equal(realmJoin.get("id"), realmId);
        };
    }

    public static Specification<Permission> fetchWithCreatedBy() {
        return (root, query, cb) -> {
            if (query != null && Long.class != query.getResultType() && long.class != query.getResultType()) {
                root.fetch("createdBy", jakarta.persistence.criteria.JoinType.LEFT);
            }
            return null;
        };
    }

    public static Specification<Permission> hasCreatedBy(String createdBy) {
        return (root, query, cb) -> {
            if (createdBy == null || createdBy.trim().isEmpty())
                return null;
            return cb.like(cb.lower(root.get("createdBy").get("username")), "%" + createdBy.toLowerCase() + "%");
        };
    }
}