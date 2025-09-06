package com.open.rbac.openrbac.specifications;

import org.springframework.data.jpa.domain.Specification;

import com.open.rbac.openrbac.models.User;

public class UserSpecification {

    public static Specification<User> hasStatus(String status) {
        return (root, query, cb) -> status == null ? cb.conjunction() : cb.equal(root.get("status"), status);
    }
}