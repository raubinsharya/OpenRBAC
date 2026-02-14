package com.open.rbac.openrbac.specifications;

import com.open.rbac.openrbac.enums.EntityStatus;
import com.open.rbac.openrbac.models.UserEffectivePermission;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UserEffectivePermissionSpecification {

    public static Specification<UserEffectivePermission> ofUser(Long userId, String realmIdentifier) {
        return (root, query, cb) -> {
            if (userId == null || realmIdentifier == null || realmIdentifier.trim().isEmpty()) {
                return null;
            }
            // Fetch relations to avoid N+1 issues - only for data query, not count query
            if (query != null && query.getResultType() != Long.class && query.getResultType() != long.class) {
                root.fetch("user", JoinType.INNER);
                root.fetch("permission", JoinType.INNER);
                root.fetch("assignedBy", JoinType.LEFT);
            }

            // Joins to user/permission and their realms
            var userJoin = root.join("user", JoinType.INNER);
            var userRealmJoin = userJoin.join("realm", JoinType.INNER);

            var permissionJoin = root.join("permission", JoinType.INNER);
            var permissionRealmJoin = permissionJoin.join("realm", JoinType.INNER);

            Predicate userRealmPredicate;
            Predicate permissionRealmPredicate;

            try {
                var realmId = Long.parseLong(realmIdentifier);
                userRealmPredicate = cb.equal(userRealmJoin.get("id"), realmId);
                permissionRealmPredicate = cb.equal(permissionRealmJoin.get("id"), realmId);
            } catch (Exception e) {
                userRealmPredicate = cb.equal(userRealmJoin.get("name"), realmIdentifier);
                permissionRealmPredicate = cb.equal(permissionRealmJoin.get("name"), realmIdentifier);
            }

            return cb.and(
                    cb.equal(userJoin.get("id"), userId),
                    userRealmPredicate,
                    permissionRealmPredicate);
        };
    }

    public static Specification<UserEffectivePermission> hasPermissionName(String name) {
        return (root, query, cb) -> {
            if (name == null || name.isEmpty())
                return null;
            return cb.like(cb.lower(root.get("permission").get("name")), "%" + name.toLowerCase() + "%");
        };
    }

    public static Specification<UserEffectivePermission> hasResource(String resource) {
        return (root, query, cb) -> {
            if (resource == null || resource.isEmpty())
                return null;
            return cb.equal(cb.lower(root.get("permission").get("resource")), resource.toLowerCase());
        };
    }

    public static Specification<UserEffectivePermission> hasAction(String action) {
        return (root, query, cb) -> {
            if (action == null || action.isEmpty())
                return null;
            return cb.equal(cb.lower(root.get("permission").get("action")), action.toLowerCase());
        };
    }

    public static Specification<UserEffectivePermission> hasPermissionStatus(EntityStatus status) {
        return (root, query, cb) -> {
            if (status == null)
                return null;
            return cb.equal(root.get("permission").get("status"), status);
        };
    }

    public static Specification<UserEffectivePermission> hasUserStatus(EntityStatus status) {
        return (root, query, cb) -> {
            if (status == null)
                return null;
            return cb.equal(root.get("user").get("status"), status);
        };
    }

    public static Specification<UserEffectivePermission> assignedBy(String assignedBy) {
        return (root, query, cb) -> {
            if (assignedBy == null || assignedBy.isEmpty()) {
                return null;
            }

            var assignedByUser = root.join("assignedBy", JoinType.LEFT);
            List<Predicate> predicates = new ArrayList<>();

            if (assignedBy.matches("^\\d+$")) {
                predicates.add(cb.equal(assignedByUser.get("id"), Long.valueOf(assignedBy)));
            }

            String pattern = "%" + assignedBy.toLowerCase() + "%";
            predicates.add(cb.like(cb.lower(assignedByUser.get("firstName")), pattern));
            predicates.add(cb.like(cb.lower(assignedByUser.get("lastName")), pattern));
            predicates.add(cb.like(cb.lower(assignedByUser.get("username")), pattern));
            predicates.add(cb.like(cb.lower(assignedByUser.get("email")), pattern));

            return cb.or(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<UserEffectivePermission> isActive(Boolean isActive) {
        return (root, query, cb) -> {
            if (isActive == null)
                return null;
            return cb.equal(root.get("isActive"), isActive);
        };
    }

    public static Specification<UserEffectivePermission> assignedAtBefore(LocalDateTime date) {
        return (root, query, cb) -> {
            if (date == null)
                return null;
            return cb.lessThanOrEqualTo(root.get("createdAt"), date);
        };
    }

    public static Specification<UserEffectivePermission> assignedAtAfter(LocalDateTime date) {
        return (root, query, cb) -> {
            if (date == null)
                return null;
            return cb.greaterThanOrEqualTo(root.get("createdAt"), date);
        };
    }

    public static Specification<UserEffectivePermission> expiryDateBefore(LocalDateTime date) {
        return (root, query, cb) -> {
            if (date == null)
                return null;
            return cb.lessThanOrEqualTo(root.get("expiryDate"), date);
        };
    }

    public static Specification<UserEffectivePermission> expiryDateAfter(LocalDateTime date) {
        return (root, query, cb) -> {
            if (date == null)
                return null;
            return cb.greaterThanOrEqualTo(root.get("expiryDate"), date);
        };
    }

    public static Specification<UserEffectivePermission> isNotExpired() {
        return (root, query, cb) -> {
            return cb.or(
                    cb.isNull(root.get("expiryDate")),
                    cb.greaterThan(root.get("expiryDate"), LocalDateTime.now()));
        };
    }

    public static Specification<UserEffectivePermission> assignmentType(String assignmentType) {
        return (root, query, cb) -> {
            if (assignmentType == null || assignmentType.isEmpty())
                return null;
            return cb.equal(root.get("assignmentType"), assignmentType.toUpperCase());
        };
    }
}
