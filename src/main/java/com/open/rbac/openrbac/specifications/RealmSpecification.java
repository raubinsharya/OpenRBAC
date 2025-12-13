package com.open.rbac.openrbac.specifications;

import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import com.open.rbac.openrbac.models.Realm;

import java.util.Objects;


public class RealmSpecification {

    public static Specification<Realm> hasStatus(String status) {
        return (root, query, cb) -> {
            if (status == null || status.trim().isEmpty()) return null;
            return cb.equal(cb.lower(root.get("status")), status.toLowerCase());
        };
    }

    public static Specification<Realm> hasId(Long id) {
        return (root, query, cb) -> {
            if (id == null) return null;
            return cb.equal((root.get("id")), id);
        };
    }

    public static Specification<Realm> hasRealmId(String realmId) {
        return (root, query, cb) -> {
            if (realmId == null || realmId.trim().isEmpty()) return null;
            return cb.equal((root.get("realmId")), realmId);
        };
    }

    public static Specification<Realm> includeUsers(boolean includeUsers) {
        return (root, query, cb) -> {
            if (!includeUsers) return null;
            Objects.requireNonNull(query).distinct(true);
            root.fetch("users", JoinType.LEFT);
            return null;
        };
    }

    public static Specification<Realm> includeRoles(boolean includeRoles) {
        return (root, query, cb) -> {
            if (!includeRoles) return null;
            Objects.requireNonNull(query).distinct(true);
            root.fetch("roles", JoinType.LEFT);
            return null;
        };
    }

    public static Specification<Realm> includePermissions(boolean includePermissions) {
        return (root, query, cb) -> {
            if (!includePermissions) return null;
            Objects.requireNonNull(query).distinct(true);
            root.fetch("permissions", JoinType.LEFT);
            return null;
        };
    }
}