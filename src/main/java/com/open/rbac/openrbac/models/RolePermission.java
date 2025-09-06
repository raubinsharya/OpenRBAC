package com.open.rbac.openrbac.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Association entity for Role-Permission assignments
 * Defines which permissions are contained within each role
 */
@Entity
@Table(name = "role_permissions",
       indexes = {
           @Index(name = "idx_role_permission_role", columnList = "role_id"),
           @Index(name = "idx_role_permission_permission", columnList = "permission_id")
       },
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_role_permission", columnNames = {"role_id", "permission_id"})
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RolePermission {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "permission_id", nullable = false)
    private Permission permission;
    
    @Column(name = "assigned_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime assignedAt = LocalDateTime.now();
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by")
    private User assignedBy;
    
    /**
     * Check if role-permission assignment is valid
     * Both role and permission must be active
     */
    public boolean isValid() {
        return (role == null || role.isActive()) && 
               (permission == null || permission.isActive());
    }
    
    /**
     * Get permission identifier for this assignment
     */
    public String getPermissionIdentifier() {
        return permission != null ? permission.getPermissionIdentifier() : null;
    }
    
    /**
     * Get role identifier for this assignment
     */
    public String getRoleIdentifier() {
        return role != null ? role.getRoleIdentifier() : null;
    }
    
    /**
     * Check if both role and permission belong to the same realm
     */
    public boolean isRealmConsistent() {
        if (role == null || permission == null || 
            role.getRealm() == null || permission.getRealm() == null) {
            return false;
        }
        return role.getRealm().getId().equals(permission.getRealm().getId());
    }
}