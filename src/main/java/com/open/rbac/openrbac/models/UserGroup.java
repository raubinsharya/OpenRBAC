package com.open.rbac.openrbac.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Association entity for User-Group membership with expiry support
 * Supports temporary group memberships for guest users and contractors
 */
@Entity
@Table(name = "user_groups",
       indexes = {
           @Index(name = "idx_user_group_user", columnList = "user_id"),
           @Index(name = "idx_user_group_group", columnList = "group_id"),
           @Index(name = "idx_user_group_expiry", columnList = "expiry_date"),
           @Index(name = "idx_user_group_active", columnList = "is_active")
       },
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_user_group", columnNames = {"user_id", "group_id"})
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserGroup {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;
    
    @Column(name = "assigned_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime assignedAt = LocalDateTime.now();
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by")
    private User assignedBy;
    
    /**
     * Expiry date for temporary group membership
     * NULL means permanent membership
     */
    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;
    
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
    
    /**
     * Check if membership is currently valid (not expired and active)
     */
    public boolean isValid() {
        return Boolean.TRUE.equals(isActive) && 
               (expiryDate == null || expiryDate.isAfter(LocalDateTime.now()));
    }
    
    /**
     * Check if membership has expired
     */
    public boolean isExpired() {
        return expiryDate != null && expiryDate.isBefore(LocalDateTime.now());
    }
    
    /**
     * Get remaining time until expiry in minutes
     */
    public Long getRemainingMinutes() {
        if (expiryDate == null) {
            return null; // Permanent membership
        }
        
        LocalDateTime now = LocalDateTime.now();
        if (expiryDate.isBefore(now)) {
            return 0L; // Already expired
        }
        
        return java.time.Duration.between(now, expiryDate).toMinutes();
    }
    
    /**
     * Check if membership is temporary (has expiry date)
     */
    public boolean isTemporary() {
        return expiryDate != null;
    }
}