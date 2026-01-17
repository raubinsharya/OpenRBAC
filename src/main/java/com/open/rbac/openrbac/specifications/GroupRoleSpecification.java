package com.open.rbac.openrbac.specifications;

import com.open.rbac.openrbac.enums.EntityStatus;
import com.open.rbac.openrbac.models.GroupRole;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class GroupRoleSpecification {

    public static Specification<GroupRole> ofGroup(Long groupId, Collection<Long> ancestorIds, Integer groupLevel,
            Long realmId) {
        return (root, query, cb) -> {
            if (groupId == null || realmId == null) {
                return null;
            }
            var groupJoin = root.join("group", JoinType.INNER);
            var groupRealmJoin = groupJoin.join("realm", JoinType.INNER);

            var roleJoin = root.join("role", JoinType.INNER);
            var roleRealmJoin = roleJoin.join("realm", JoinType.INNER);

            // Fetch relations for data queries
            if (query != null && GroupRole.class.equals(query.getResultType())) {
                root.fetch("group", JoinType.INNER);
                root.fetch("role", JoinType.INNER);
                root.fetch("assignedBy", JoinType.LEFT);
                root.fetch("sourceGroup", JoinType.LEFT);
            }

            // Direct assignment to the requested group
            Predicate isDirect = cb.equal(groupJoin.get("id"), groupId);

            Predicate groupPredicate;
            if (ancestorIds != null && !ancestorIds.isEmpty()) {
                // Inherited assignment logic
                Predicate isInheritedCandidate = cb.and(
                        groupJoin.get("id").in(ancestorIds),
                        cb.equal(root.get("allowInheritance"), true));

                // Depth check: groupLevel - ancestor.level <= maxInheritanceDepth
                Predicate depthValid = cb.or(
                        cb.isNull(root.get("maxInheritanceDepth")),
                        cb.lessThanOrEqualTo(
                                cb.diff(groupLevel, groupJoin.get("level")),
                                root.get("maxInheritanceDepth")));

                groupPredicate = cb.or(isDirect, cb.and(isInheritedCandidate, depthValid));
            } else {
                groupPredicate = isDirect;
            }

            return cb.and(
                    groupPredicate,
                    cb.equal(groupRealmJoin.get("id"), realmId),
                    cb.equal(roleRealmJoin.get("id"), realmId));
        };
    }

    public static Specification<GroupRole> hasRoleName(String name) {
        return (root, query, cb) -> {
            if (name == null || name.isEmpty())
                return null;
            return cb.like(cb.lower(root.get("role").get("name")), "%" + name.toLowerCase() + "%");
        };
    }

    public static Specification<GroupRole> hasRoleStatus(EntityStatus status) {
        return (root, query, cb) -> {
            if (status == null)
                return null;
            return cb.equal(root.get("role").get("status"), status);
        };
    }

    public static Specification<GroupRole> hasGroupStatus(EntityStatus status) {
        return (root, query, cb) -> {
            if (status == null)
                return null;
            return cb.equal(root.get("group").get("status"), status);
        };
    }

    public static Specification<GroupRole> assignedBy(String assignedBy) {
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

    public static Specification<GroupRole> isActive(Boolean isActive) {
        return (root, query, cb) -> {
            if (isActive == null)
                return null;
            return cb.equal(root.get("isActive"), isActive);
        };
    }

    public static Specification<GroupRole> isInherited(Long requestedGroupId, Boolean isInherited) {
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

    public static Specification<GroupRole> assignedAtBefore(LocalDateTime date) {
        return (root, query, cb) -> {
            if (date == null)
                return null;
            return cb.lessThanOrEqualTo(root.get("createdAt"), date);
        };
    }

    public static Specification<GroupRole> assignedAtAfter(LocalDateTime date) {
        return (root, query, cb) -> {
            if (date == null)
                return null;
            return cb.greaterThanOrEqualTo(root.get("createdAt"), date);
        };
    }

    public static Specification<GroupRole> expiryDateBefore(LocalDateTime date) {
        return (root, query, cb) -> {
            if (date == null)
                return null;
            return cb.lessThanOrEqualTo(root.get("expiryDate"), date);
        };
    }

    public static Specification<GroupRole> expiryDateAfter(LocalDateTime date) {
        return (root, query, cb) -> {
            if (date == null)
                return null;
            return cb.greaterThanOrEqualTo(root.get("expiryDate"), date);
        };
    }

    public static Specification<GroupRole> isNotExpired() {
        return (root, query, cb) -> {
            return cb.or(
                    cb.isNull(root.get("expiryDate")),
                    cb.greaterThan(root.get("expiryDate"), LocalDateTime.now()));
        };
    }

    public static Specification<GroupRole> hasRoleId(Long roleId) {
        return (root, query, cb) -> {
            if (roleId == null)
                return null;
            return cb.equal(root.get("role").get("id"), roleId);
        };
    }
}
