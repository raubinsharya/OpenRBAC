package com.open.rbac.openrbac.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import com.open.rbac.openrbac.enums.EntityStatus;

/**
 * RBAC Role entity for the multi-realm system
 * Roles are containers for permissions and are scoped to realms
 */
@Entity
@Table(name = "roles", 
       indexes = {
           @Index(name = "idx_rbac_role_realm_name", columnList = "realm_id, name"),
           @Index(name = "idx_rbac_role_status", columnList = "status"),
           @Index(name = "idx_rbac_role_system", columnList = "is_system_role")
       },
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_rbac_role_realm_name", columnNames = {"realm_id", "name"})
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "realm_id", nullable = false)
    private Realm realm;
    
    @Column(nullable = false, length = 100)
    @NotBlank(message = "Role name is required")
    @Size(min = 2, max = 100, message = "Role name must be between 2 and 100 characters")
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EntityStatus status = EntityStatus.ACTIVE;
    
    /**
     * System roles are created automatically (SUPER_ADMIN, REALM_ADMIN, GROUP_ADMIN)
     * and have special privileges
     */
    @Column(name = "is_system_role", nullable = false)
    @Builder.Default
    private Boolean isSystemRole = false;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Check if role is active and can be used
     */
    public boolean isActive() {
        return EntityStatus.ACTIVE.equals(this.status);
    }
    
    /**
     * Check if role is blocked (temporarily disabled)
     */
    public boolean isBlocked() {
        return EntityStatus.BLOCKED.equals(this.status);
    }
    
    /**
     * Check if role is disabled (requires admin intervention)
     */
    public boolean isDisabled() {
        return EntityStatus.DISABLED.equals(this.status);
    }
    
    /**
     * Check if this is a system-defined role
     */
    public boolean isSystem() {
        return Boolean.TRUE.equals(this.isSystemRole);
    }
    
    /**
     * Get role identifier for permission checking
     */
    public String getRoleIdentifier() {
        return realm.getName() + ":" + name;
    }
}