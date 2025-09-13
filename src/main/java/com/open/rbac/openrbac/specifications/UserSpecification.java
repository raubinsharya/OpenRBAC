package com.open.rbac.openrbac.specifications;

import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import com.open.rbac.openrbac.models.User;

import java.time.LocalDateTime;

public class UserSpecification {

    public static Specification<User> hasStatus(String status) {
        return (root, query, cb) -> {
            if (status == null || status.trim().isEmpty())
                return cb.conjunction();
            return cb.equal(cb.lower(root.get("status")), status.toLowerCase());
        };

    }

    public static Specification<User> hasUserName(String userName) {
        return (root, query, cb) -> {
            if (userName == null || userName.trim().isEmpty())
                return cb.conjunction();
            return cb.like(cb.lower(root.get("username")), "%" + userName.toLowerCase() + "%");
        };
    }

    public static Specification<User> includeUserRoles(boolean includeUserRoles) {
        return (root, query, cb) -> {
            if (includeUserRoles) {
                root.fetch("roles", JoinType.LEFT);
            }
            return cb.conjunction();
        };
    }

    public static Specification<User> includeUserPermissions(boolean includeUserPermissions) {
        return (root, query, cb) -> {
            if (includeUserPermissions)
                root.fetch("permissions", JoinType.LEFT);
            return cb.conjunction();
        };
    }

    public static Specification<User> includeRealm(boolean includeRealm) {
        return (root, query, cb) -> {
            if (includeRealm)
                root.fetch("realm", JoinType.LEFT);
            return cb.conjunction();
        };
    }

    public static Specification<User> hasRealmId(Long id) {
        return (root, query, cb) -> {
            if (id == null) return cb.conjunction();
            return cb.equal(root.get("realm").get("id"), id);
        };
    }

    public static Specification<User> hasEmail(String email) {
        return (root, query, cb) -> {
            if (email == null || email.trim().isEmpty())
                return cb.conjunction();
            return cb.like(cb.lower(root.get("email")), "%" + email.toLowerCase() + "%");
        };
    }

    public static Specification<User> hasFirstName(String firstName) {
        return (root, query, cb) -> {
            if (firstName == null || firstName.trim().isEmpty())
                return cb.conjunction();
            return cb.like(cb.lower(root.get("firstName")), "%" + firstName.toLowerCase() + "%");
        };
    }

    public static Specification<User> hasLastName(String lastName) {
        return (root, query, cb) -> {
            if (lastName == null || lastName.trim().isEmpty())
                return cb.conjunction();
            return cb.like(cb.lower(root.get("lastName")), "%" + lastName.toLowerCase() + "%");
        };
    }


    public static Specification<User> hasKeycloakUserId(String keycloakUserId) {
        return (root, query, cb) -> {
            if (keycloakUserId == null || keycloakUserId.trim().isEmpty())
                return cb.conjunction();
            return cb.equal(root.get("keycloakUserId"), keycloakUserId);
        };
    }

    public static Specification<User> createdAfter(LocalDateTime createdAfter) {
        return (root, query, cb) -> {
            if (createdAfter == null)
                return cb.conjunction();
            return cb.greaterThanOrEqualTo(root.get("createdAt"), createdAfter);
        };
    }

    public static Specification<User> createdBefore(LocalDateTime createdBefore) {
        return (root, query, cb) -> {
            if (createdBefore == null)
                return cb.conjunction();
            return cb.lessThanOrEqualTo(root.get("createdAt"), createdBefore);
        };
    }

    public static Specification<User> updatedAfter(LocalDateTime updatedAfter) {
        return (root, query, cb) -> {
            if (updatedAfter == null)
                return cb.conjunction();
            return cb.greaterThanOrEqualTo(root.get("updatedAt"), updatedAfter);
        };
    }

    public static Specification<User> updatedBefore(LocalDateTime updatedBefore) {
        return (root, query, cb) -> {
            if (updatedBefore == null)
                return cb.conjunction();
            return cb.lessThanOrEqualTo(root.get("updatedAt"), updatedBefore);
        };
    }

    public static Specification<User> hasName(String name) {
        return (root, query, cb) -> {
            if (name == null || name.trim().isEmpty())
                return cb.conjunction();
            return cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
        };
    }

    public static Specification<User> hasDescription(String description) {
        return (root, query, cb) -> {
            if (description == null || description.trim().isEmpty())
                return cb.conjunction();
            return cb.like(cb.lower(root.get("description")), "%" + description.toLowerCase() + "%");
        };
    }
}