package com.open.rbac.openrbac.specifications;

import com.open.rbac.openrbac.enums.EntityStatus;
import com.open.rbac.openrbac.models.UserRole;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UserRoleSpecification {

    public static Specification<UserRole> ofUser(Long userId, Long realmId) {
        return (root, query, cb) -> {
            if (userId == null || realmId == null) {
                return null;
            }
            var userJoin = root.join("user", JoinType.INNER);
            var userRealmJoin = userJoin.join("realm", JoinType.INNER);

            // Check realm consistency via user or role? Usually user is in realm, role is
            // in realm.
            // RolePermissionSpec checks role.realm and permission.realm.
            // Here we check user.realm. We could also check role.realm.

            // Allow roles from SAME realm.
            var roleJoin = root.join("role", JoinType.INNER);
            var roleRealmJoin = roleJoin.join("realm", JoinType.INNER);

            // Fetch relations
            root.fetch("user", JoinType.INNER);
            root.fetch("role", JoinType.INNER);
            root.fetch("assignedBy", JoinType.LEFT);

            return cb.and(
                    cb.equal(userJoin.get("id"), userId),
                    cb.equal(userRealmJoin.get("id"), realmId),
                    cb.equal(roleRealmJoin.get("id"), realmId));
        };
    }

    public static Specification<UserRole> hasRoleName(String name) {
        return (root, query, cb) -> {
            if (name == null || name.isEmpty())
                return null;
            return cb.like(cb.lower(root.get("role").get("name")), "%" + name.toLowerCase() + "%");
        };
    }

    public static Specification<UserRole> hasRoleStatus(EntityStatus status) {
        return (root, query, cb) -> {
            if (status == null)
                return null;
            return cb.equal(root.get("role").get("status"), status);
        };
    }

    public static Specification<UserRole> hasUserStatus(EntityStatus status) {
        return (root, query, cb) -> {
            if (status == null)
                return null;
            return cb.equal(root.get("user").get("status"), status);
        };
    }

    public static Specification<UserRole> assignedBy(String assignedBy) {
        return (root, query, cb) -> {
            if (assignedBy == null || assignedBy.isEmpty()) {
                return null;
            }

            var assignedByUser = root.join("assignedBy", JoinType.LEFT);
            List<Predicate> predicates = new ArrayList<>();

            // If numeric, check ID
            if (assignedBy.matches("^\\d+$")) {
                predicates.add(cb.equal(assignedByUser.get("id"), Long.valueOf(assignedBy)));
            }

            // Check name fields
            String pattern = "%" + assignedBy.toLowerCase() + "%";
            predicates.add(cb.like(cb.lower(assignedByUser.get("firstName")), pattern));
            predicates.add(cb.like(cb.lower(assignedByUser.get("lastName")), pattern));
            predicates.add(cb.like(cb.lower(assignedByUser.get("username")), pattern));
            predicates.add(cb.like(cb.lower(assignedByUser.get("email")), pattern));

            return cb.or(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<UserRole> isActive(Boolean isActive) {
        return (root, query, cb) -> {
            if (isActive == null)
                return null;
            return cb.equal(root.get("isActive"), isActive);
        };
    }

    public static Specification<UserRole> assignedAtBefore(LocalDateTime date) {
        return (root, query, cb) -> {
            if (date == null)
                return null;
            return cb.lessThanOrEqualTo(root.get("assignedAt"), date);
        };
    }

    public static Specification<UserRole> assignedAtAfter(LocalDateTime date) {
        return (root, query, cb) -> {
            if (date == null)
                return null;
            return cb.greaterThanOrEqualTo(root.get("assignedAt"), date);
        };
    }

    public static Specification<UserRole> expiryDateBefore(LocalDateTime date) {
        return (root, query, cb) -> {
            if (date == null)
                return null;
            return cb.lessThanOrEqualTo(root.get("expiryDate"), date);
        };
    }

    public static Specification<UserRole> expiryDateAfter(LocalDateTime date) {
        return (root, query, cb) -> {
            if (date == null)
                return null;
            return cb.greaterThanOrEqualTo(root.get("expiryDate"), date);
        };
    }
}
