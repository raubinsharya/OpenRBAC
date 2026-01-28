package com.open.rbac.openrbac.specifications;

import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import com.open.rbac.openrbac.models.User;

import java.time.LocalDateTime;

public class UserSpecification {

    public static Specification<User> hasStatus(String status) {
        return (root, query, cb) -> {
            if (status == null || status.trim().isEmpty())
                return null;
            return cb.equal(cb.lower(root.get("status")), status.toLowerCase());
        };

    }

    public static Specification<User> hasUserId(Long userId, Long realmId) {
        return (root, query, cb) -> {
            if (userId == null)
                return null;
            var realmJoin = root.join("realm", JoinType.INNER);
            Predicate userPredicate = cb.equal(root.get("id"), userId);
            Predicate realmPredicate = cb.equal(realmJoin.get("id"), realmId);

            return cb.and(userPredicate, realmPredicate);
        };
    }

    public static Specification<User> hasUserId(Long userId, String realmIdentifier) {
        return (root, query, cb) -> {
            if (userId == null || realmIdentifier == null)
                return null;

            var realmJoin = root.join("realm", JoinType.INNER);
            Predicate userPredicate = cb.equal(root.get("id"), userId);

            Predicate realmPredicate;
            try {
                Long id = Long.parseLong(realmIdentifier);
                realmPredicate = cb.equal(realmJoin.get("id"), id);
            } catch (NumberFormatException e) {
                Predicate namePredicate = cb.equal(realmJoin.get("name"), realmIdentifier);
                Predicate realmIdPredicate = cb.equal(realmJoin.get("realmId"), realmIdentifier);
                realmPredicate = cb.or(namePredicate, realmIdPredicate);
            }

            return cb.and(userPredicate, realmPredicate);
        };
    }

    public static Specification<User> hasUserName(String userName) {
        return (root, query, cb) -> {
            if (userName == null || userName.trim().isEmpty())
                return null;
            return cb.like(cb.lower(root.get("username")), "%" + userName.toLowerCase() + "%");
        };
    }

    public static Specification<User> includeUserRoles(boolean includeUserRoles) {
        return (root, query, cb) -> {
            if (includeUserRoles) {
                root.fetch("roles", JoinType.LEFT);
            }
            return null;
        };
    }

    public static Specification<User> includeUserPermissions(boolean includeUserPermissions) {
        return (root, query, cb) -> {
            if (includeUserPermissions)
                root.fetch("permissions", JoinType.LEFT);
            return null;
        };
    }

    public static Specification<User> includeRealm(boolean includeRealm) {
        return (root, query, cb) -> {
            if (includeRealm)
                root.fetch("realm", JoinType.LEFT);
            return null;
        };
    }

    public static Specification<User> hasRealmId(Long id) {
        return (root, query, cb) -> {
            if (id == null)
                return null;
            return cb.equal(root.get("realm").get("id"), id);
        };
    }

    public static Specification<User> hasRealm(String realmIdentifier) {
        return (root, query, cb) -> {
            if (realmIdentifier == null || realmIdentifier.trim().isEmpty())
                return null;

            var realmJoin = root.join("realm", JoinType.INNER);
            if (query != null) {
                query.distinct(true);
            }

            try {
                Long id = Long.parseLong(realmIdentifier);
                return cb.equal(realmJoin.get("id"), id);
            } catch (NumberFormatException e) {
                Predicate namePredicate = cb.equal(realmJoin.get("name"), realmIdentifier);
                Predicate realmIdPredicate = cb.equal(realmJoin.get("realmId"), realmIdentifier);
                return cb.or(namePredicate, realmIdPredicate);
            }
        };
    }

    public static Specification<User> hasEmail(String email) {
        return (root, query, cb) -> {
            if (email == null || email.trim().isEmpty())
                return null;
            return cb.like(cb.lower(root.get("email")), "%" + email.toLowerCase() + "%");
        };
    }

    public static Specification<User> hasFirstName(String firstName) {
        return (root, query, cb) -> {
            if (firstName == null || firstName.trim().isEmpty())
                return null;
            return cb.like(cb.lower(root.get("firstName")), "%" + firstName.toLowerCase() + "%");
        };
    }

    public static Specification<User> hasLastName(String lastName) {
        return (root, query, cb) -> {
            if (lastName == null || lastName.trim().isEmpty())
                return null;
            return cb.like(cb.lower(root.get("lastName")), "%" + lastName.toLowerCase() + "%");
        };
    }

    public static Specification<User> hasKeycloakUserId(String keycloakUserId) {
        return (root, query, cb) -> {
            if (keycloakUserId == null || keycloakUserId.trim().isEmpty())
                return null;
            return cb.equal(root.get("keycloakUserId"), keycloakUserId);
        };
    }

    public static Specification<User> createdAfter(LocalDateTime createdAfter) {
        return (root, query, cb) -> {
            if (createdAfter == null)
                return null;
            return cb.greaterThanOrEqualTo(root.get("createdAt"), createdAfter);
        };
    }

    public static Specification<User> createdBefore(LocalDateTime createdBefore) {
        return (root, query, cb) -> {
            if (createdBefore == null)
                return null;
            return cb.lessThanOrEqualTo(root.get("createdAt"), createdBefore);
        };
    }

    public static Specification<User> updatedAfter(LocalDateTime updatedAfter) {
        return (root, query, cb) -> {
            if (updatedAfter == null)
                return null;
            return cb.greaterThanOrEqualTo(root.get("updatedAt"), updatedAfter);
        };
    }

    public static Specification<User> updatedBefore(LocalDateTime updatedBefore) {
        return (root, query, cb) -> {
            if (updatedBefore == null)
                return null;
            return cb.lessThanOrEqualTo(root.get("updatedAt"), updatedBefore);
        };
    }

    public static Specification<User> hasName(String name) {
        return (root, query, cb) -> {
            if (name == null || name.trim().isEmpty())
                return null;
            return cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
        };
    }

    public static Specification<User> hasDescription(String description) {
        return (root, query, cb) -> {
            if (description == null || description.trim().isEmpty())
                return null;
            return cb.like(cb.lower(root.get("description")), "%" + description.toLowerCase() + "%");
        };
    }
}