package com.open.rbac.openrbac.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to require ALL of the specified roles
 * User must have every single role listed (AND logic)
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireAllRoles {
    
    /**
     * Array of role names - user must have ALL of them
     */
    String[] value();
    
    /**
     * Parameter name that contains the realm ID (optional)
     * If specified, user must have roles in that realm
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
    String message() default "All required roles not found";
}