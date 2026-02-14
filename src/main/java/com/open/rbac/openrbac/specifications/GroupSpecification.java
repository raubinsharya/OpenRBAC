package com.open.rbac.openrbac.specifications;

import com.open.rbac.openrbac.models.Group;
import com.open.rbac.openrbac.utils.ParsingUtils;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class GroupSpecification {

    public static Specification<Group> hasId(Long roleId) {
        return (root, query, cb) -> {
            if (roleId == null)
                return null;
            return cb.equal((root.get("id")), roleId);
        };
    }

    public static Specification<Group> hasStatus(String status) {
        return (root, query, cb) -> {
            if (status == null || status.trim().isEmpty())
                return null;
            return cb.equal(cb.lower(root.get("status")), status.toLowerCase());
        };
    }

    public static Specification<Group> hasRealm(String realmIdentifier) {
        return (root, query, cb) -> {
            if (realmIdentifier == null || realmIdentifier.trim().isEmpty())
                return null;
            Long id = ParsingUtils.safeParseLong(realmIdentifier);
            if (id != null) {
                return cb.equal(root.get("realm").get("id"), id);
            } else {
                return cb.equal(root.get("realm").get("name"), realmIdentifier);
            }
        };
    }

    public static Specification<Group> searchByNameIgnoreCase(String search) {
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

    public static Specification<Group> hasCreatedAfter(LocalDateTime dateTime) {
        return (root, query, cb) -> {
            if (dateTime == null)
                return null;
            return cb.greaterThan(root.get("createdAt"), dateTime);
        };
    }

    public static Specification<Group> hasCreatedBefore(LocalDateTime dateTime) {
        return (root, query, cb) -> {
            if (dateTime == null)
                return null;
            return cb.lessThan(root.get("createdAt"), dateTime);
        };
    }

    public static Specification<Group> hasUpdatedAfter(LocalDateTime dateTime) {
        return (root, query, cb) -> {
            if (dateTime == null)
                return null;
            return cb.greaterThan(root.get("updatedAt"), dateTime);
        };
    }

    public static Specification<Group> hasUpdatedBefore(LocalDateTime dateTime) {
        return (root, query, cb) -> {
            if (dateTime == null)
                return null;
            return cb.lessThan(root.get("updatedAt"), dateTime);
        };
    }

    public static Specification<Group> fetchWithCreatedBy() {
        return (root, query, cb) -> {
            if (query != null && (Long.class != query.getResultType()) && (long.class != query.getResultType())) {
                root.fetch("createdBy", jakarta.persistence.criteria.JoinType.LEFT);
            }
            return null;
        };
    }

    public static Specification<Group> hasCreatedBy(String createdBy) {
        return (root, query, cb) -> {
            if (createdBy == null || createdBy.trim().isEmpty())
                return null;
            return cb.like(cb.lower(root.get("createdBy").get("username")), "%" + createdBy.toLowerCase() + "%");
        };
    }

    public static Specification<Group> hasPath(String path) {
        return (root, query, cb) -> {
            if (path == null || path.trim().isEmpty())
                return null;
            return cb.equal(root.get("path"), path);
        };
    }

    public static Specification<Group> hasPathPrefix(String pathPrefix) {
        return (root, query, cb) -> {
            if (pathPrefix == null || pathPrefix.trim().isEmpty())
                return null;
            return cb.like(root.get("path"), pathPrefix + "%");
        };
    }

    public static Specification<Group> hasLevel(Integer level) {
        return (root, query, cb) -> {
            if (level == null)
                return null;
            return cb.equal(root.get("level"), level);
        };
    }

    public static Specification<Group> isRoot(Boolean isRoot) {
        return (root, query, cb) -> {
            if (isRoot == null)
                return null;
            if (isRoot) {
                return cb.isNull(root.get("parentGroup"));
            } else {
                return cb.isNotNull(root.get("parentGroup"));
            }
        };
    }

    public static Specification<Group> hasParentGroup(Long parentGroupId) {
        return (root, query, cb) -> {
            if (parentGroupId == null)
                return null;
            return cb.equal(root.get("parentGroup").get("id"), parentGroupId);
        };
    }
}