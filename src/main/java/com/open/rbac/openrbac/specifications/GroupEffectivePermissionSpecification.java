package com.open.rbac.openrbac.specifications;

import com.open.rbac.openrbac.enums.EntityStatus;
import com.open.rbac.openrbac.models.GroupEffectivePermission;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class GroupEffectivePermissionSpecification {

    public static Specification<GroupEffectivePermission> ofGroup(Long groupId, Collection<Long> ancestorIds,
            Integer groupLevel, Long realmId) {
        return (root, query, cb) -> {
            if (groupId == null || realmId == null) {
                return null;
            }

            var groupJoin = root.join("group", JoinType.INNER);
            var groupRealmJoin = groupJoin.join("realm", JoinType.INNER);
            var permissionJoin = root.join("permission", JoinType.INNER);
            var permissionRealmJoin = permissionJoin.join("realm", JoinType.INNER);

            // Direct assignment (or Role via Group) to the requested group
            Predicate isDirectToGroup = cb.equal(groupJoin.get("id"), groupId);

            Predicate groupPredicate;
            if (ancestorIds != null && !ancestorIds.isEmpty()) {
                // Inherited assignment logic
                // The view contains 'allow_inheritance' column from either gp or gr
                Predicate isInheritedCandidate = cb.and(
                        groupJoin.get("id").in(ancestorIds),
                        cb.equal(root.get("allowInheritance"), true));

                // Depth check
                Predicate depthValid = cb.or(
                        cb.isNull(root.get("maxInheritanceDepth")),
                        cb.lessThanOrEqualTo(
                                cb.diff(groupLevel, groupJoin.get("level")),
                                root.get("maxInheritanceDepth")));

                groupPredicate = cb.or(isDirectToGroup, cb.and(isInheritedCandidate, depthValid));
            } else {
                groupPredicate = isDirectToGroup;
            }

            return cb.and(
                    groupPredicate,
                    cb.equal(groupRealmJoin.get("id"), realmId),
                    cb.equal(permissionRealmJoin.get("id"), realmId));
        };
    }

    public static Specification<GroupEffectivePermission> hasPermissionName(String name) {
        return (root, query, cb) -> {
            if (name == null || name.isEmpty())
                return null;
            return cb.like(cb.lower(root.get("permission").get("name")), "%" + name.toLowerCase() + "%");
        };
    }

    public static Specification<GroupEffectivePermission> hasResource(String resource) {
        return (root, query, cb) -> {
            if (resource == null || resource.isEmpty())
                return null;
            return cb.equal(cb.lower(root.get("permission").get("resource")), resource.toLowerCase());
        };
    }

    public static Specification<GroupEffectivePermission> hasAction(String action) {
        return (root, query, cb) -> {
            if (action == null || action.isEmpty())
                return null;
            return cb.equal(cb.lower(root.get("permission").get("action")), action.toLowerCase());
        };
    }

    public static Specification<GroupEffectivePermission> hasPermissionStatus(EntityStatus status) {
        return (root, query, cb) -> {
            if (status == null)
                return null;
            return cb.equal(root.get("permission").get("status"), status);
        };
    }

    public static Specification<GroupEffectivePermission> hasGroupStatus(EntityStatus status) {
        return (root, query, cb) -> {
            if (status == null)
                return null;
            return cb.equal(root.get("group").get("status"), status);
        };
    }

    public static Specification<GroupEffectivePermission> assignedBy(String assignedBy) {
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

    public static Specification<GroupEffectivePermission> isActive(Boolean isActive) {
        return (root, query, cb) -> {
            if (isActive == null)
                return null;
            return cb.equal(root.get("isActive"), isActive);
        };
    }

    public static Specification<GroupEffectivePermission> isInherited(Long requestedGroupId, Boolean isInherited) {
        return (root, query, cb) -> {
            if (isInherited == null || requestedGroupId == null)
                return null;
            if (isInherited) {
                return cb.notEqual(root.get("group").get("id"), requestedGroupId);
            } else {
                return cb.equal(root.get("group").get("id"), requestedGroupId);
            }
        };
    }

    public static Specification<GroupEffectivePermission> assignedAtBefore(LocalDateTime date) {
        return (root, query, cb) -> {
            if (date == null)
                return null;
            return cb.lessThanOrEqualTo(root.get("createdAt"), date);
        };
    }

    public static Specification<GroupEffectivePermission> assignedAtAfter(LocalDateTime date) {
        return (root, query, cb) -> {
            if (date == null)
                return null;
            return cb.greaterThanOrEqualTo(root.get("createdAt"), date);
        };
    }

    public static Specification<GroupEffectivePermission> expiryDateBefore(LocalDateTime date) {
        return (root, query, cb) -> {
            if (date == null)
                return null;
            return cb.lessThanOrEqualTo(root.get("expiryDate"), date);
        };
    }

    public static Specification<GroupEffectivePermission> expiryDateAfter(LocalDateTime date) {
        return (root, query, cb) -> {
            if (date == null)
                return null;
            return cb.greaterThanOrEqualTo(root.get("expiryDate"), date);
        };
    }

    public static Specification<GroupEffectivePermission> isNotExpired() {
        return (root, query, cb) -> {
            return cb.or(
                    cb.isNull(root.get("expiryDate")),
                    cb.greaterThan(root.get("expiryDate"), LocalDateTime.now()));
        };
    }

    public static Specification<GroupEffectivePermission> fromRole(Boolean fromRole) {
        return (root, query, cb) -> {
            // If fromRole is true, we include role-based permissions (show all: DIRECT +
            // ROLE)
            // If fromRole is false or null, we only show DIRECT permissions (default
            // behavior)
            if (Boolean.TRUE.equals(fromRole)) {
                return null;
            }
            return cb.equal(root.get("assignmentType"), "DIRECT");
        };
    }
}
