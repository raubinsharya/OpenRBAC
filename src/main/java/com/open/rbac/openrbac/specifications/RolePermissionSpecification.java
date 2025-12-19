package com.open.rbac.openrbac.specifications;

import com.open.rbac.openrbac.enums.EntityStatus;
import com.open.rbac.openrbac.models.RolePermission;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class RolePermissionSpecification {

    public static Specification<RolePermission> ofRole(Long roleId, Long realmId) {
        return (root, query, cb) -> {
            if (roleId == null || realmId == null) {
                return null;
            }
            var roleJoin = root.join("role", JoinType.INNER);
            var roleRealmJoin = roleJoin.join("realm", JoinType.INNER);
            var permissionRealmJoin = root.join("permission", JoinType.INNER).join("realm", JoinType.INNER);

            root.fetch("role", JoinType.INNER);
            root.fetch("permission", JoinType.INNER);
            root.fetch("assignedBy", JoinType.LEFT);

            return cb.and(
                    cb.equal(roleJoin.get("id"), roleId),
                    cb.equal(roleRealmJoin.get("id"), realmId),
                    cb.equal(permissionRealmJoin.get("id"), realmId));
        };
    }

    public static Specification<RolePermission> hasId(Long id) {
        return (root, query, cb) -> {
            if (id == null)
                return null;
            return cb.equal(root.get("id"), id);
        };
    }

    public static Specification<RolePermission> hasPermissionName(String name) {
        return (root, query, cb) -> {
            if (name == null || name.isEmpty())
                return null;
            return cb.like(cb.lower(root.get("permission").get("name")), "%" + name.toLowerCase() + "%");
        };
    }

    public static Specification<RolePermission> hasResource(String resource) {
        return (root, query, cb) -> {
            if (resource == null || resource.isEmpty())
                return null;
            return cb.like(cb.lower(root.get("permission").get("resource")), "%" + resource.toLowerCase() + "%");
        };
    }

    public static Specification<RolePermission> hasAction(String action) {
        return (root, query, cb) -> {
            if (action == null || action.isEmpty())
                return null;
            return cb.like(cb.lower(root.get("permission").get("action")), "%" + action.toLowerCase() + "%");
        };
    }

    public static Specification<RolePermission> hasPermissionDescription(String description) {
        return (root, query, cb) -> {
            if (description == null || description.isEmpty())
                return null;
            return cb.like(cb.lower(root.get("permission").get("description")), "%" + description.toLowerCase() + "%");
        };
    }

    public static Specification<RolePermission> hasPermissionStatus(String status) {
        return (root, query, cb) -> {
            if (status == null || status.isEmpty())
                return null;
            return cb.equal(cb.lower(root.get("permission").get("status")), status.toLowerCase());
        };
    }

    public static Specification<RolePermission> assignedBy(String assignedBy) {
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

    public static Specification<RolePermission> hasRoleStatus(EntityStatus status) {
        return (root, query, cb) -> {
            if (status == null)
                return null;
            return cb.equal(root.get("role").get("status"), status);
        };
    }

    public static Specification<RolePermission> assignedAtBefore(LocalDateTime date) {
        return (root, query, cb) -> {
            if (date == null)
                return null;
            return cb.lessThanOrEqualTo(root.get("createdAt"), date);
        };
    }

    public static Specification<RolePermission> assignedAtAfter(LocalDateTime date) {
        return (root, query, cb) -> {
            if (date == null)
                return null;
            return cb.greaterThanOrEqualTo(root.get("createdAt"), date);
        };
    }
}
