package com.open.rbac.openrbac.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to require a specific role
 * User must have the exact role specified
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRole {
    
    /**
     * Role name (e.g., "REALM_ADMIN", "GROUP_ADMIN", "USER")
     */
    String value();
    
    /**
     * Parameter name that contains the realm ID (optional)
     * If specified, user must have role in that realm
     */
    String realmIdParam() default "";
    
    /**
     * Custom error message when access is denied
     */
    String message() default "Required role not found";
}