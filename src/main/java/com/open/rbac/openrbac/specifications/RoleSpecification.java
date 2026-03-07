package com.open.rbac.openrbac.specifications;

import com.open.rbac.openrbac.models.Role;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class RoleSpecification {

    public static Specification<Role> hasId(Long roleId) {
        return (root, query, cb) -> {
            if (roleId == null)
                return null;
            return cb.equal((root.get("id")), roleId);
        };
    }

    public static Specification<Role> hasStatus(String status) {
        return (root, query, cb) -> {
            if (status == null || status.trim().isEmpty())
                return null;
            return cb.equal(cb.lower(root.get("status")), status.toLowerCase());
        };
    }

    public static Specification<Role> isSystemRole(Boolean isSystemRole) {
        return (root, query, cb) -> {
            if (isSystemRole == null)
                return null;
            return cb.equal(root.get("isSystemRole"), isSystemRole);
        };
    }

    public static Specification<Role> hasRealm(Long realmId) {
        return (root, query, cb) -> {
            if (realmId == null)
                return null;
            return cb.equal(root.get("realm").get("id"), realmId);
        };
    }

    public static Specification<Role> hasRealm(String realmIdentifier) {
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

    public static Specification<Role> searchByNameIgnoreCase(String search) {
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

    public static Specification<Role> hasCreatedAfter(LocalDateTime dateTime) {
        return (root, query, cb) -> {
            if (dateTime == null)
                return null;
            return cb.greaterThan(root.get("createdAt"), dateTime);
        };
    }

    public static Specification<Role> hasCreatedBefore(LocalDateTime dateTime) {
        return (root, query, cb) -> {
            if (dateTime == null)
                return null;
            return cb.lessThan(root.get("createdAt"), dateTime);
        };
    }

    public static Specification<Role> hasUpdatedAfter(LocalDateTime dateTime) {
        return (root, query, cb) -> {
            if (dateTime == null)
                return null;
            return cb.greaterThan(root.get("updatedAt"), dateTime);
        };
    }

    public static Specification<Role> hasUpdatedBefore(LocalDateTime dateTime) {
        return (root, query, cb) -> {
            if (dateTime == null)
                return null;
            return cb.lessThan(root.get("updatedAt"), dateTime);
        };
    }

    public static Specification<Role> ofUser(String userName) {
        return (root, query, cb) -> {
            if (userName == null || userName.isEmpty())
                return null;
            // Join with userRoles collection then to User
            var userRoles = root.join("userRoles", JoinType.INNER);
            var user = userRoles.join("user", JoinType.INNER);
            return cb.equal(cb.lower(user.get("username")), userName.toLowerCase());
        };
    }

    public static Specification<Role> ofUserById(Long userId, Long realmId) {
        return (root, query, cb) -> {
            if (userId == null || realmId == null)
                return null;
            var userRoleJoin = root.join("userRoles", JoinType.INNER);
            var userJoin = userRoleJoin.join("user", JoinType.INNER);
            var realmJoin = userJoin.join("realm", JoinType.INNER);
            Predicate userPredicate = cb.equal(userJoin.get("id"), userId);
            Predicate realmPredicate = cb.equal(realmJoin.get("id"), realmId);
            return cb.and(userPredicate, realmPredicate);
        };
    }

    public static Specification<Role> fetchWithCreatedBy() {
        return (root, query, cb) -> {
            if (query != null && Long.class != query.getResultType() && long.class != query.getResultType()) {
                root.fetch("createdBy", jakarta.persistence.criteria.JoinType.LEFT);
            }
            return null;
        };
    }

    public static Specification<Role> hasCreatedBy(String createdBy) {
        return (root, query, cb) -> {
            if (createdBy == null || createdBy.trim().isEmpty())
                return null;
            return cb.like(cb.lower(root.get("createdBy").get("username")), "%" + createdBy.toLowerCase() + "%");
        };
    }
}