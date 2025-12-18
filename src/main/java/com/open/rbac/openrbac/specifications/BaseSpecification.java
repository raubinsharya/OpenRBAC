package com.open.rbac.openrbac.specifications;

import com.open.rbac.openrbac.RequestParams.BaseFilter;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class BaseSpecification {

    /**
     * Applies standard filters found in BaseFilter (dates, etc.) to any generic
     * Entity.
     * Assumes the entity has standard fields: createdAt, updatedAt.
     */
    public static <T> Specification<T> withBaseFilters(BaseFilter filter) {
        return (root, query, cb) -> {
            if (filter == null)
                return null;

            List<Predicate> predicates = new ArrayList<>();

            // createdAt filters
            if (filter.getCreatedAfter() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), filter.getCreatedAfter()));
            }
            if (filter.getCreatedBefore() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), filter.getCreatedBefore()));
            }

            // updatedAt filters
            if (filter.getUpdatedAfter() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("updatedAt"), filter.getUpdatedAfter()));
            }
            if (filter.getUpdatedBefore() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("updatedAt"), filter.getUpdatedBefore()));
            }

            if (predicates.isEmpty())
                return null;

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
