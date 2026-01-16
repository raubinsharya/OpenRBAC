package com.open.rbac.openrbac.specifications;

import com.open.rbac.openrbac.enums.EntityStatus;
import com.open.rbac.openrbac.models.UserPermission;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UserPermissionSpecification {

    public static Specification<UserPermission> ofUser(Long userId, Long realmId) {
        return (root, query, cb) -> {
            if (userId == null || realmId == null) {
                return null;
            }
            var userJoin = root.join("user", JoinType.INNER);
            var userRealmJoin = userJoin.join("realm", JoinType.INNER);

            var permissionJoin = root.join("permission", JoinType.INNER);
            var permissionRealmJoin = permissionJoin.join("realm", JoinType.INNER);

            // Fetch relations to avoid N+1 issues
            root.fetch("user", JoinType.INNER);
            root.fetch("permission", JoinType.INNER);
            root.fetch("assignedBy", JoinType.LEFT);

            return cb.and(
                    cb.equal(userJoin.get("id"), userId),
                    cb.equal(userRealmJoin.get("id"), realmId),
                    cb.equal(permissionRealmJoin.get("id"), realmId));
        };
    }

    public static Specification<UserPermission> hasPermissionName(String name) {
        return (root, query, cb) -> {
            if (name == null || name.isEmpty())
                return null;
            return cb.like(cb.lower(root.get("permission").get("name")), "%" + name.toLowerCase() + "%");
        };
    }

    public static Specification<UserPermission> hasResource(String resource) {
        return (root, query, cb) -> {
            if (resource == null || resource.isEmpty())
                return null;
            return cb.equal(cb.lower(root.get("permission").get("resource")), resource.toLowerCase());
        };
    }

    public static Specification<UserPermission> hasAction(String action) {
        return (root, query, cb) -> {
            if (action == null || action.isEmpty())
                return null;
            return cb.equal(cb.lower(root.get("permission").get("action")), action.toLowerCase());
        };
    }

    public static Specification<UserPermission> hasPermissionStatus(EntityStatus status) {
        return (root, query, cb) -> {
            if (status == null)
                return null;
            return cb.equal(root.get("permission").get("status"), status);
        };
    }

    public static Specification<UserPermission> hasUserStatus(EntityStatus status) {
        return (root, query, cb) -> {
            if (status == null)
                return null;
            return cb.equal(root.get("user").get("status"), status);
        };
    }

    public static Specification<UserPermission> assignedBy(String assignedBy) {
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

    public static Specification<UserPermission> isActive(Boolean isActive) {
        return (root, query, cb) -> {
            if (isActive == null)
                return null;
            return cb.equal(root.get("isActive"), isActive);
        };
    }

    public static Specification<UserPermission> assignedAtBefore(LocalDateTime date) {
        return (root, query, cb) -> {
            if (date == null)
                return null;
            return cb.lessThanOrEqualTo(root.get("assignedAt"), date);
        };
    }

    public static Specification<UserPermission> assignedAtAfter(LocalDateTime date) {
        return (root, query, cb) -> {
            if (date == null)
                return null;
            return cb.greaterThanOrEqualTo(root.get("assignedAt"), date);
        };
    }

    public static Specification<UserPermission> expiryDateBefore(LocalDateTime date) {
        return (root, query, cb) -> {
            if (date == null)
                return null;
            return cb.lessThanOrEqualTo(root.get("expiryDate"), date);
        };
    }

    public static Specification<UserPermission> expiryDateAfter(LocalDateTime date) {
        return (root, query, cb) -> {
            if (date == null)
                return null;
            return cb.greaterThanOrEqualTo(root.get("expiryDate"), date);
        };
    }
}
