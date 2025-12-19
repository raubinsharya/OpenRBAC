package com.open.rbac.openrbac.specifications;

import com.open.rbac.openrbac.enums.EntityStatus;
import com.open.rbac.openrbac.models.UserGroup;

import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

import java.util.ArrayList;
import java.util.List;

public class GroupMemberSpecification {

    public static Specification<UserGroup> ofGroup(Long groupId, Long realmId) {
        return (root, query, cb) -> {
            if (groupId == null || realmId == null)
                return null;
            var groupJoin = root.join("group", JoinType.INNER);
            var groupRealmJoin = groupJoin.join("realm", JoinType.INNER);
            var userRealmJoin = root.join("user", JoinType.INNER).join("realm", JoinType.INNER);

            root.fetch("group", JoinType.INNER);
            root.fetch("user", JoinType.INNER);
            root.fetch("assignedBy", JoinType.LEFT);

            Predicate groupPredicate = cb.equal(groupJoin.get("id"), groupId);
            Predicate groupRealmPredicate = cb.equal(groupRealmJoin.get("id"), realmId);
            Predicate userRealmPredicate = cb.equal(userRealmJoin.get("id"), realmId);

            return cb.and(groupPredicate, groupRealmPredicate, userRealmPredicate);
        };
    }

    public static Specification<UserGroup> hasId(Long id) {
        return (root, query, cb) -> {
            if (id == null) {
                return null;
            }
            return cb.equal(root.get("id"), id);
        };
    }

    public static Specification<UserGroup> hasKeycloakUserId(String keycloakUserId) {
        return (root, query, cb) -> {
            if (keycloakUserId == null) {
                return null;
            }
            return cb.equal(root.get("user").get("keycloakUserId"), keycloakUserId);
        };
    }

    public static Specification<UserGroup> hasDisplayName(String displayName) {
        return (root, query, cb) -> {
            if (displayName == null || displayName.isEmpty()) {
                return null;
            }
            String pattern = "%" + displayName.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("user").get("firstName")), pattern),
                    cb.like(cb.lower(root.get("user").get("lastName")), pattern),
                    cb.like(cb.lower(root.get("user").get("username")), pattern),
                    cb.like(cb.lower(root.get("user").get("email")), pattern));
        };
    }

    public static Specification<UserGroup> hasEmail(String email) {
        return (root, query, cb) -> {
            if (email == null) {
                return null;
            }
            return cb.equal(root.get("user").get("email"), email);
        };
    }

    public static Specification<UserGroup> assignedBy(String assignedBy) {
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

    public static Specification<UserGroup> hasStatus(EntityStatus status) {
        return (root, query, cb) -> {
            if (status == null) {
                return null;
            }
            return cb.equal(root.get("user").get("status"), status);
        };
    }

    public static Specification<UserGroup> hasGroupStatus(EntityStatus groupStatus) {
        return (root, query, cb) -> {
            if (groupStatus == null) {
                return null;
            }
            return cb.equal(root.get("group").get("status"), groupStatus);
        };
    }

    public static Specification<UserGroup> assignedAtBefore(LocalDateTime assignedAt) {
        return (root, query, cb) -> {
            if (assignedAt == null) {
                return null;
            }
            return cb.lessThanOrEqualTo(root.get("createdAt"), assignedAt);
        };
    }

    public static Specification<UserGroup> assignedAtAfter(LocalDateTime assignedAt) {
        return (root, query, cb) -> {
            if (assignedAt == null) {
                return null;
            }
            return cb.greaterThanOrEqualTo(root.get("createdAt"), assignedAt);
        };
    }

    public static Specification<UserGroup> groupMemberExpiryBefore(LocalDateTime groupMemberExpiry) {
        return (root, query, cb) -> {
            if (groupMemberExpiry == null) {
                return null;
            }
            return cb.lessThanOrEqualTo(root.get("expiryDate"), groupMemberExpiry);
        };
    }

    public static Specification<UserGroup> groupMemberExpiryAfter(LocalDateTime groupMemberExpiry) {
        return (root, query, cb) -> {
            if (groupMemberExpiry == null) {
                return null;
            }
            return cb.greaterThanOrEqualTo(root.get("expiryDate"), groupMemberExpiry);
        };
    }

    public static Specification<UserGroup> isGroupMembershipExpired(Boolean isExpired) {
        return (root, query, cb) -> {
            if (isExpired == null) {
                return null;
            }
            Expression<LocalDateTime> now = cb.currentTimestamp().as(LocalDateTime.class);
            if (isExpired) {
                // Expired: expiryDate is not null AND expiryDate < now
                return cb.and(
                        cb.isNotNull(root.get("expiryDate")),
                        cb.lessThan(root.get("expiryDate"), now));
            } else {
                // Not Expired: expiryDate is null OR expiryDate >= now
                return cb.or(
                        cb.isNull(root.get("expiryDate")),
                        cb.greaterThanOrEqualTo(root.get("expiryDate"), now));
            }
        };
    }

    public static Specification<UserGroup> isGroupMembershipValid(Boolean isValid) {
        return (root, query, cb) -> {
            if (isValid == null) {
                return null;
            }
            Expression<LocalDateTime> now = cb.currentTimestamp().as(LocalDateTime.class);
            if (isValid) {
                // Valid: isActive is true AND (expiryDate is null OR expiryDate > now)
                return cb.and(
                        cb.equal(root.get("isActive"), true),
                        cb.or(
                                cb.isNull(root.get("expiryDate")),
                                cb.greaterThan(root.get("expiryDate"), now)));
            } else {
                // Invalid: isActive is false OR (expiryDate is not null AND expiryDate <= now)
                return cb.or(
                        cb.equal(root.get("isActive"), false),
                        cb.and(
                                cb.isNotNull(root.get("expiryDate")),
                                cb.lessThanOrEqualTo(root.get("expiryDate"), now)));
            }
        };
    }
}