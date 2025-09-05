package com.open.rbac.openrbac.enums;

/**
 * Status enumeration for all RBAC entities (realms, groups, roles, permissions, users)
 * Provides consistent status management across the system
 */
public enum EntityStatus {
    /**
     * Entity is active and fully functional
     */
    ACTIVE,
    
    /**
     * User is unauthenticated/unverified (only applicable to users)
     * Used for users who have registered but not verified their email
     */
    UNAUTH,
    
    /**
     * Entity is temporarily blocked but can be reactivated
     * Blocked entities are excluded from permission calculations
     */
    BLOCKED,
    
    /**
     * Entity is disabled and requires administrative intervention to reactivate
     * Disabled entities are completely excluded from the system
     */
    DISABLED
}