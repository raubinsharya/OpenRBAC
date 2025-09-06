package com.open.rbac.openrbac.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to require ALL of the specified permissions
 * User must have every single resource:action combination (AND logic)
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireAllPermissions {
    
    /**
     * Array of permission specifications in "resource:action" format
     * e.g., {"USER:CREATE", "USER:UPDATE", "USER:DELETE"}
     */
    String[] value();
    
    /**
     * Parameter name that contains the realm ID (optional)
     * If specified, user must have permissions in that realm
     */
    String realmIdParam() default "";
    
    /**
     * Custom error message when access is denied
     */
    String message() default "All required permissions not found";
}