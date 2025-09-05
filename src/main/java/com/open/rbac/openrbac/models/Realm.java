package com.open.rbac.openrbac.models;

import com.open.rbac.openrbac.enums.EntityStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Realm entity for multi-tenant RBAC system
 * Provides complete isolation between different organizations/tenants
 */
@Entity
@Table(name = "realms", indexes = {
        @Index(name = "idx_realm_name", columnList = "name"),
        @Index(name = "idx_realm_status", columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Realm {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false, length = 100)
    @NotBlank(message = "Realm name is required")
    @Size(min = 2, max = 100, message = "Realm name must be between 2 and 100 characters")
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EntityStatus status = EntityStatus.ACTIVE;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @Column(name = "created_by")
    private Long createdBy;
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Check if realm is active and can be used
     */
    public boolean isActive() {
        return EntityStatus.ACTIVE.equals(this.status);
    }
    
    /**
     * Check if realm is blocked (temporarily disabled)
     */
    public boolean isBlocked() {
        return EntityStatus.BLOCKED.equals(this.status);
    }
    
    /**
     * Check if realm is disabled (requires admin intervention)
     */
    public boolean isDisabled() {
        return EntityStatus.DISABLED.equals(this.status);
    }
}