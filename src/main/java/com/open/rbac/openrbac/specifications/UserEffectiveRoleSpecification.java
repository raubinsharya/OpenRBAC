package com.open.rbac.openrbac.specifications;

import com.open.rbac.openrbac.enums.EntityStatus;
import com.open.rbac.openrbac.models.UserEffectiveRole;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class UserEffectiveRoleSpecification {

    public static Specification<UserEffectiveRole> ofUser(Long userId, Long realmId) {
        return (root, query, cb) -> {
            if (userId == null || realmId == null) {
                return null;
            }

            // Fetch Optimization ONLY for entity queries, NOT for count/exists
            if (query.getResultType().equals(UserEffectiveRole.class)) {
                root.fetch("role", JoinType.LEFT);
                root.fetch("user", JoinType.LEFT);
                root.fetch("sourceGroup", JoinType.LEFT);
                root.fetch("assignedBy", JoinType.LEFT);
            }

            return cb.and(
                    cb.equal(root.get("user").get("id"), userId),
                    cb.equal(root.get("role").get("realm").get("id"), realmId));
        };
    }

    public static Specification<UserEffectiveRole> hasRoleName(String roleName) {
        return (root, query, cb) -> {
            if (roleName == null || roleName.isEmpty())
                return null;
            return cb.like(cb.lower(root.get("role").get("name")), "%" + roleName.toLowerCase() + "%");
        };
    }

    public static Specification<UserEffectiveRole> hasRoleNameIn(java.util.Collection<String> roleNames) {
        return (root, query, cb) -> {
            if (roleNames == null || roleNames.isEmpty())
                return null;
            return root.get("role").get("name").in(roleNames);
        };
    }

    public static Specification<UserEffectiveRole> hasRoleStatus(EntityStatus status) {
        return (root, query, cb) -> {
            if (status == null)
                return null;
            return cb.equal(root.get("role").get("status"), status);
        };
    }

    public static Specification<UserEffectiveRole> hasUserStatus(EntityStatus status) {
        return (root, query, cb) -> {
            if (status == null)
                return null;
            return cb.equal(root.get("user").get("status"), status);
        };
    }

    public static Specification<UserEffectiveRole> assignedBy(String assignedBy) {
        return (root, query, cb) -> {
            if (assignedBy == null || assignedBy.isEmpty()) {
                return null;
            }
            var assignedByUser = root.join("assignedBy", JoinType.LEFT);
            String pattern = "%" + assignedBy.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(assignedByUser.get("username")), pattern),
                    cb.like(cb.lower(assignedByUser.get("firstName")), pattern),
                    cb.like(cb.lower(assignedByUser.get("lastName")), pattern));
        };
    }

    public static Specification<UserEffectiveRole> isActive(Boolean isActive) {
        return (root, query, cb) -> {
            if (isActive == null)
                return null;
            return cb.equal(root.get("isActive"), isActive);
        };
    }

    public static Specification<UserEffectiveRole> assignedAtBefore(LocalDateTime date) {
        return (root, query, cb) -> {
            if (date == null)
                return null;
            return cb.lessThanOrEqualTo(root.get("createdAt"), date);
        };
    }

    public static Specification<UserEffectiveRole> assignedAtAfter(LocalDateTime date) {
        return (root, query, cb) -> {
            if (date == null)
                return null;
            return cb.greaterThanOrEqualTo(root.get("createdAt"), date);
        };
    }

    public static Specification<UserEffectiveRole> expiryDateBefore(LocalDateTime date) {
        return (root, query, cb) -> {
            if (date == null)
                return null;
            return cb.lessThanOrEqualTo(root.get("expiryDate"), date);
        };
    }

    public static Specification<UserEffectiveRole> expiryDateAfter(LocalDateTime date) {
        return (root, query, cb) -> {
            if (date == null)
                return null;
            return cb.greaterThanOrEqualTo(root.get("expiryDate"), date);
        };
    }

    public static Specification<UserEffectiveRole> assignmentType(String assignmentType) {
        return (root, query, cb) -> {
            if (assignmentType == null || assignmentType.isEmpty())
                return null;
            return cb.equal(root.get("assignmentType"), assignmentType.toUpperCase());
        };
    }

    public static Specification<UserEffectiveRole> isNotExpired() {
        return (root, query, cb) -> {
            return cb.or(
                    cb.isNull(root.get("expiryDate")),
                    cb.greaterThan(root.get("expiryDate"), LocalDateTime.now()));
        };
    }
}
