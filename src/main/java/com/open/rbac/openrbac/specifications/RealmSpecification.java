package com.open.rbac.openrbac.specifications;

import org.springframework.data.jpa.domain.Specification;

import com.open.rbac.openrbac.models.Realm;



public class RealmSpecification {

    public static Specification<Realm> hasStatus(String status) {
        return (root, query, cb) -> status == null ? cb.conjunction() : cb.equal(root.get("status"), status);
    }
}