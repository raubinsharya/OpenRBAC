package com.open.rbac.openrbac.specifications;

import com.open.rbac.openrbac.models.Group;
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
            try {
                Long id = Long.parseLong(realmIdentifier);
                return cb.equal(root.get("realm").get("id"), id);
            } catch (NumberFormatException e) {
                Predicate namePredicate = cb.equal(root.get("realm").get("name"), realmIdentifier);
                Predicate realmIdPredicate = cb.equal(root.get("realm").get("realmId"), realmIdentifier);
                return cb.or(namePredicate, realmIdPredicate);
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
}