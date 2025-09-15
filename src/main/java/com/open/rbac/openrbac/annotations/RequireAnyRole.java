package com.open.rbac.openrbac.annotations;

import java.lang.annotation.*;

/**
 * Annotation to require ANY of the specified roles
 * User must have at least one of the listed roles (OR logic)
 */
@Target({ElementType.METHOD,  ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireAnyRole {

    /**
     * Array of role names - user must have at least one
     */
    String[] value();

    /**
     * Parameter name that contains the realm ID (optional)
     * If specified, user must have role in that realm
     */
    String realmIdParam() default "";

    /**
     * Whether to check if user is in system realm (default: false)
     * Set to true if roles should only be checked in system realm
     */
    boolean requireSystemRealm() default false;

    /**
     * Custom error message when access is denied
     */
    String message() default "Required role not found";
}