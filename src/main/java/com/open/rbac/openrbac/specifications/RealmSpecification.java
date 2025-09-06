package com.open.rbac.openrbac.specifications;

import org.springframework.data.jpa.domain.Specification;

import com.open.rbac.openrbac.models.Realm;



public class RealmSpecification {

    public static Specification<Realm> hasStatus(String status) {
        return (root, query, cb) -> {
            if(status == null || status.trim().isEmpty()) return cb.conjunction();
            return cb.equal(cb.lower(root.get("status")), status.toLowerCase());
        };
    }
}