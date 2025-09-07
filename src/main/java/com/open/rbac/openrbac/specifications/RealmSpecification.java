package com.open.rbac.openrbac.specifications;

import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import com.open.rbac.openrbac.models.Realm;

import java.util.Objects;


public class RealmSpecification {

    public static Specification<Realm> hasStatus(String status) {
        return (root, query, cb) -> {
            if(status == null || status.trim().isEmpty()) return cb.conjunction();
            return cb.equal(cb.lower(root.get("status")), status.toLowerCase());
        };
    }

    public static Specification<Realm> includeUsers() {
        return (root, query, cb) -> {
            Objects.requireNonNull(query).distinct(true);
            root.fetch("users", JoinType.INNER);
            return cb.conjunction();
        };
    }
}