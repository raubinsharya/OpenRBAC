package com.open.rbac.openrbac.specifications;

import com.open.rbac.openrbac.models.Permission;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class PermissionSpecification {

    public static Specification<Permission> hasStatus(String status) {
        return (root, query, cb) -> {
            if (status == null || status.trim().isEmpty())
                return cb.conjunction();
            return cb.equal(cb.lower(root.get("status")), status.toLowerCase());
        };
    }

    public static Specification<Permission> hasRealmId(Long realmId) {
        return (root, query, cb) -> {
            if (realmId == null)
                return cb.conjunction();
            return cb.equal(root.get("realm").get("id"), realmId);
        };
    }

    public static Specification<Permission> hasResource(String resource) {
        return (root, query, cb) -> {
            if (resource == null || resource.trim().isEmpty())
                return cb.conjunction();
            return cb.equal(cb.lower(root.get("resource")), resource.toLowerCase());
        };
    }

    public static Specification<Permission> hasAction(String action) {
        return (root, query, cb) -> {
            if (action == null || action.trim().isEmpty())
                return cb.conjunction();
            return cb.equal(cb.lower(root.get("action")), action.toLowerCase());
        };
    }

    public static Specification<Permission> ofUser(String userName) {
        return (root, query, cb) -> {
            if (userName == null || userName.isEmpty()) return cb.conjunction();
            // Join with users collection to get permissions directly assigned to user
            var users = root.join("users");
            return cb.equal(cb.lower(users.get("username")), userName.toLowerCase());
        };
    }

    public static Specification<Permission> searchByNameIgnoreCase(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isEmpty()) {
                return cb.conjunction(); // no filtering if search is empty
            }
            return cb.like(
                    cb.lower(root.get("name")),
                    "%" + search.toLowerCase() + "%" // match anywhere in the string
            );
        };
    }

    public static Specification<Permission> hasCreatedAfter(LocalDateTime dateTime) {
        return (root, query, cb) -> {
            if (dateTime == null) return cb.conjunction();
            return cb.greaterThan(root.get("createdAt"), dateTime);
        };
    }

    public static Specification<Permission> hasCreatedBefore(LocalDateTime dateTime) {
        return (root, query, cb) -> {
            if (dateTime == null) return cb.conjunction();
            return cb.lessThan(root.get("createdAt"), dateTime);
        };
    }

    public static Specification<Permission> hasUpdatedAfter(LocalDateTime dateTime) {
        return (root, query, cb) -> {
            if (dateTime == null) return cb.conjunction();
            return cb.greaterThan(root.get("updatedAt"), dateTime);
        };
    }

    public static Specification<Permission> hasUpdatedBefore(LocalDateTime dateTime) {
        return (root, query, cb) -> {
            if (dateTime == null) return cb.conjunction();
            return cb.lessThan(root.get("updatedAt"), dateTime);
        };
    }
}