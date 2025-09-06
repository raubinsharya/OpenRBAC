package com.open.rbac.openrbac.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to require ANY of the specified permissions
 * User must have at least one of the resource:action combinations (OR logic)
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireAnyPermission {
    
    /**
     * Array of permission specifications in "resource:action" format
     * e.g., {"USER:CREATE", "USER:UPDATE", "ADMIN:MANAGE"}
     */
    String[] value();
    
    /**
     * Parameter name that contains the realm ID (optional)
     * If specified, user must have permission in that realm
     */
    String realmIdParam() default "";
    
    /**
     * Custom error message when access is denied
     */
    String message() default "Required permission not found";
}