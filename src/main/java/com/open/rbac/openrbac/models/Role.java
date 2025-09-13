package com.open.rbac.openrbac.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.open.rbac.openrbac.enums.EntityStatus;

/**
 * RBAC Role entity for the multi-realm system
 * Roles are containers for permissions and are scoped to realms
 */
@Entity
@Table(name = "roles", indexes = {
        @Index(name = "idx_rbac_role_realm_name", columnList = "realm_id, name"),
        @Index(name = "idx_rbac_role_status", columnList = "status"),
        @Index(name = "idx_rbac_role_system", columnList = "is_system_role")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_rbac_role_realm_name", columnNames = {"realm_id", "name"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

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
     * System roles are created automatically (SUPER_ADMIN, REALM_ADMIN,
     * GROUP_ADMIN)
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "realm_id", nullable = false)
    @JsonIgnore
    private Realm realm;

    @ManyToMany(mappedBy = "roles")
    @JsonIgnore
    private Set<User> users;
    /**
     * Permissions associated with this role (many-to-many relationship)
     */
    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "role_permissions", joinColumns = @JoinColumn(name = "role_id"), inverseJoinColumns = @JoinColumn(name = "permission_id"), indexes = {
            @Index(name = "idx_role_permission_role", columnList = "role_id"),
            @Index(name = "idx_role_permission_permission", columnList = "permission_id")
    }, uniqueConstraints = {
            @UniqueConstraint(name = "uk_role_permission", columnNames = {"role_id", "permission_id"})
    })
    @JsonIgnore
    private List<Permission> permissions;

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}