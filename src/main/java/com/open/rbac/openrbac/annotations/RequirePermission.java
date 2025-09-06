package com.open.rbac.openrbac.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to require a specific permission
 * User must have the exact resource:action permission specified
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequirePermission {
    
    /**
     * Resource name (e.g., "USER", "GROUP", "ROLE")
     */
    String resource();
    
    /**
     * Action name (e.g., "CREATE", "READ", "UPDATE", "DELETE")
     */
    String action();
    
    /**
     * Parameter name that contains the realm ID (optional)
     * If specified, user must have permission in that realm
     */
    String realmIdParam() default "";
    
    /**
     * Custom error message when access is denied
     */
    String message() default "Insufficient permissions";
}