package com.open.rbac.openrbac.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Association entity for Group-Permission assignments with inheritance tracking and expiry support
 * Enables direct permission assignment to groups (bypassing roles)
 */
@Entity
@Table(name = "group_permissions",
       indexes = {
           @Index(name = "idx_group_permission_group", columnList = "group_id"),
           @Index(name = "idx_group_permission_permission", columnList = "permission_id"),
           @Index(name = "idx_group_permission_inherited", columnList = "is_inherited"),
           @Index(name = "idx_group_permission_source", columnList = "source_group_id"),
           @Index(name = "idx_group_permission_expiry", columnList = "expiry_date"),
           @Index(name = "idx_group_permission_active", columnList = "is_active")
       },
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_group_permission", columnNames = {"group_id", "permission_id"})
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupPermission {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;
    
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
     * Expiry date for temporary permission assignment
     * NULL means permanent assignment
     */
    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;
    
    /**
     * True if this permission is inherited from a parent group
     * False if directly assigned to this group
     */
    @Column(name = "is_inherited", nullable = false)
    @Builder.Default
    private Boolean isInherited = false;
    
    /**
     * Source group from which this permission is inherited
     * NULL if directly assigned (not inherited)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_group_id")
    private Group sourceGroup;
    
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
    
    /**
     * Controls whether this permission can be inherited by child groups
     * Default: false (no inheritance)
     */
    @Column(name = "allow_inheritance", nullable = false)
    @Builder.Default
    private Boolean allowInheritance = false;
    
    /**
     * Maximum inheritance depth (levels down from this group)
     * NULL means inherit to all descendant levels (leaf groups)
     * 0 means no inheritance (same as allowInheritance = false)
     * 1 means inherit only to direct children
     * 2 means inherit to children and grandchildren, etc.
     */
    @Column(name = "max_inheritance_depth")
    private Integer maxInheritanceDepth;
    
    /**
     * Check if permission assignment is currently valid (not expired and active)
     */
    public boolean isValid() {
        return Boolean.TRUE.equals(isActive) && 
               (expiryDate == null || expiryDate.isAfter(LocalDateTime.now())) &&
               (permission == null || permission.isActive());
    }
    
    /**
     * Check if permission assignment has expired
     */
    public boolean isExpired() {
        return expiryDate != null && expiryDate.isBefore(LocalDateTime.now());
    }
    
    /**
     * Get remaining time until expiry in minutes
     */
    public Long getRemainingMinutes() {
        if (expiryDate == null) {
            return null; // Permanent assignment
        }
        
        LocalDateTime now = LocalDateTime.now();
        if (expiryDate.isBefore(now)) {
            return 0L; // Already expired
        }
        
        return java.time.Duration.between(now, expiryDate).toMinutes();
    }
    
    /**
     * Check if permission assignment is temporary (has expiry date)
     */
    public boolean isTemporary() {
        return expiryDate != null;
    }
    
    /**
     * Check if this is a directly assigned permission (not inherited)
     */
    public boolean isDirect() {
        return !Boolean.TRUE.equals(isInherited);
    }
    
    /**
     * Get the inheritance path description
     */
    public String getInheritancePath() {
        if (isDirect()) {
            return "Direct assignment";
        }
        return "Inherited from group: " + (sourceGroup != null ? sourceGroup.getName() : "Unknown");
    }
    
    /**
     * Get permission identifier for checking
     */
    public String getPermissionIdentifier() {
        return permission != null ? permission.getPermissionIdentifier() : "Unknown";
    }
    
    /**
     * Check if this permission matches a resource and action
     */
    public boolean matches(String resource, String action) {
        return permission != null && permission.matches(resource, action);
    }
    
    /**
     * Check if this permission can be inherited by child groups at the specified depth
     */
    public boolean canInheritAtDepth(int depth) {
        if (!Boolean.TRUE.equals(allowInheritance)) {
            return false;
        }
        
        if (maxInheritanceDepth == null) {
            return true; // Inherit to all levels
        }
        
        return depth <= maxInheritanceDepth;
    }
    
    /**
     * Get inheritance description
     */
    public String getInheritanceDescription() {
        if (!Boolean.TRUE.equals(allowInheritance)) {
            return "No inheritance";
        }
        
        if (maxInheritanceDepth == null) {
            return "Inherit to all descendant levels";
        }
        
        if (maxInheritanceDepth == 0) {
            return "No inheritance";
        }
        
        return "Inherit up to " + maxInheritanceDepth + " level(s) down";
    }
}