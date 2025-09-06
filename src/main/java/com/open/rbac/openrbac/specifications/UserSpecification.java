package com.open.rbac.openrbac.specifications;

import org.springframework.data.jpa.domain.Specification;

import com.open.rbac.openrbac.models.User;

public class UserSpecification {

    public static Specification<User> hasStatus(String status) {
        return (root, query, cb) -> {
            if (status == null || status.trim().isEmpty())
                return cb.conjunction();
            return cb.equal(cb.lower(root.get("status")), status.toLowerCase());
        };
    }
}