package com.open.rbac.openrbac.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Association entity for direct User-Role assignments with expiry support
 * Allows assigning roles directly to users (in addition to group-based roles)
 */
@Entity
@Table(name = "user_roles",
       indexes = {
           @Index(name = "idx_user_role_user", columnList = "user_id"),
           @Index(name = "idx_user_role_role", columnList = "role_id"),
           @Index(name = "idx_user_role_expiry", columnList = "expiry_date"),
           @Index(name = "idx_user_role_active", columnList = "is_active")
       },
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_user_role", columnNames = {"user_id", "role_id"})
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRole {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;
    
    @Column(name = "assigned_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime assignedAt = LocalDateTime.now();
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by")
    private User assignedBy;
    
    /**
     * Expiry date for temporary role assignment
     * NULL means permanent assignment
     */
    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;
    
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
    
    /**
     * Check if role assignment is currently valid (not expired and active)
     */
    public boolean isValid() {
        return Boolean.TRUE.equals(isActive) && 
               (expiryDate == null || expiryDate.isAfter(LocalDateTime.now())) &&
               (role == null || role.isActive());
    }
    
    /**
     * Check if role assignment has expired
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
     * Check if role assignment is temporary (has expiry date)
     */
    public boolean isTemporary() {
        return expiryDate != null;
    }
    
    /**
     * Get role identifier for this assignment
     */
    public String getRoleIdentifier() {
        return role != null ? role.getRoleIdentifier() : null;
    }
    
    /**
     * Check if user and role belong to the same realm
     */
    public boolean isRealmConsistent() {
        if (user == null || role == null || 
            user.getRealm() == null || role.getRealm() == null) {
            return false;
        }
        return user.getRealm().getId().equals(role.getRealm().getId());
    }
}