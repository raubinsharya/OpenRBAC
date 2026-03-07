package com.open.rbac.openrbac.specifications;

import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import com.open.rbac.openrbac.models.Realm;

import java.util.Objects;

public class RealmSpecification {

    public static Specification<Realm> hasStatus(String status) {
        return (root, query, cb) -> {
            if (status == null || status.trim().isEmpty())
                return null;
            return cb.equal(cb.lower(root.get("status")), status.toLowerCase());
        };
    }

    public static Specification<Realm> hasId(Long id) {
        return (root, query, cb) -> {
            if (id == null)
                return null;
            return cb.equal((root.get("id")), id);
        };
    }

    public static Specification<Realm> hasIdOrName(String realmIdentifier) {
        return (root, query, cb) -> {
            if (realmIdentifier == null)
                return null;
            try {
                Long realmIdLong = Long.parseLong(realmIdentifier);
                return cb.equal((root.get("id")), realmIdLong);
            } catch (Exception e) {
                Predicate namePred = cb.equal(root.get("name"), realmIdentifier);
                Predicate realmIdPred = cb.equal(root.get("realmId"), realmIdentifier);
                return cb.or(namePred, realmIdPred);
            }
        };
    }

    public static Specification<Realm> hasRealmId(String realmId) {
        return (root, query, cb) -> {
            if (realmId == null || realmId.trim().isEmpty())
                return null;
            return cb.equal((root.get("realmId")), realmId);
        };
    }

    public static Specification<Realm> searchByNameIgnoreCase(String search) {
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

    public static Specification<Realm> includeUsers(boolean includeUsers) {
        return (root, query, cb) -> {
            if (!includeUsers)
                return null;
            Objects.requireNonNull(query).distinct(true);
            root.fetch("users", JoinType.LEFT);
            return null;
        };
    }

    public static Specification<Realm> includeRoles(boolean includeRoles) {
        return (root, query, cb) -> {
            if (!includeRoles)
                return null;
            Objects.requireNonNull(query).distinct(true);
            root.fetch("roles", JoinType.LEFT);
            return null;
        };
    }

    public static Specification<Realm> includePermissions(boolean includePermissions) {
        return (root, query, cb) -> {
            if (!includePermissions)
                return null;
            Objects.requireNonNull(query).distinct(true);
            root.fetch("permissions", JoinType.LEFT);
            return null;
        };
    }
}