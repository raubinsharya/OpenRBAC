package com.open.rbac.openrbac.specifications;

import com.open.rbac.openrbac.models.Role;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.function.Predicate;

public class RoleSpecification {

    public static Specification<Role> hasStatus(String status) {
        return (root, query, cb) -> {
            if (status == null || status.trim().isEmpty())
                return cb.conjunction();
            return cb.equal(cb.lower(root.get("status")), status.toLowerCase());
        };
    }

    public static Specification<Role> isSystemRole(Boolean isSystemRole) {
        return (root, query, cb) -> {
            if (isSystemRole == null)
                return cb.conjunction();
            return cb.equal(root.get("isSystemRole"), isSystemRole);
        };
    }

    public static Specification<Role> hasRealm(Long realmId) {
        return (root, query, cb) -> {
            if (realmId == null)
                return cb.conjunction();
            return cb.equal(root.get("realm").get("id"), realmId);
        };
    }

    public static Specification<Role> searchByNameIgnoreCase(String search) {
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

    public static Specification<Role> hasCreatedAfter(LocalDateTime dateTime) {
        return (root, query, cb) -> {
            if (dateTime == null) return cb.conjunction();
            return cb.greaterThan(root.get("createdAt"), dateTime);
        };
    }

    public static Specification<Role> hasCreatedBefore(LocalDateTime dateTime) {
        return (root, query, cb) -> {
            if (dateTime == null) return cb.conjunction();
            return cb.lessThan(root.get("createdAt"), dateTime);
        };
    }

    public static Specification<Role> hasUpdatedAfter(LocalDateTime dateTime) {
        return (root, query, cb) -> {
            if (dateTime == null) return cb.conjunction();
            return cb.greaterThan(root.get("updatedAt"), dateTime);
        };
    }

    public static Specification<Role> hasUpdatedBefore(LocalDateTime dateTime) {
        return (root, query, cb) -> {
            if (dateTime == null) return cb.conjunction();
            return cb.lessThan(root.get("updatedAt"), dateTime);
        };
    }

    public static Specification<Role> ofUser(String userName) {
        return (root, query, cb) -> {
            if (userName == null || userName.isEmpty()) return cb.conjunction();
            // Join with users collection
            var users = root.join("users");
            return cb.equal(cb.lower(users.get("username")), userName.toLowerCase());
        };
    }


}