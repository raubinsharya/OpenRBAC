package com.open.rbac.openrbac.utils;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;

public class SpecificationUtils {

    /**
     * Creates a predicate that checks for equality on an ID field if the identifier
     * is numeric,
     * otherwise checks for equality on a name field.
     *
     * @param cb         The CriteriaBuilder
     * @param path       The path to the entity (e.g. root.get("role").get("realm"))
     * @param identifier The string identifier (ID or Name)
     * @return A Predicate checking either ID or Name
     */
    public static Predicate byIdOrName(CriteriaBuilder cb, Path<?> path, String identifier) {
        return byIdOrProperty(cb, path, "id", "name", identifier);
    }

    /**
     * Creates a predicate that checks for equality on an ID field if the identifier
     * is numeric,
     * otherwise checks for equality on a specified property field.
     *
     * @param cb            The CriteriaBuilder
     * @param path          The path to the entity
     * @param idField       The name of the ID field (usually "id")
     * @param propertyField The name of the property to check if not numeric (e.g.
     *                      "name", "username")
     * @param identifier    The string identifier
     * @return A Predicate
     */
    public static Predicate byIdOrProperty(CriteriaBuilder cb, Path<?> path, String idField, String propertyField,
            String identifier) {
        if (identifier == null) {
            return null;
        }
        Long id = ParsingUtils.safeParseLong(identifier);
        if (id != null) {
            return cb.equal(path.get(idField), id);
        } else {
            return cb.equal(path.get(propertyField), identifier);
        }
    }
}
