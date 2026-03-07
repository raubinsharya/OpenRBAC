package com.open.rbac.openrbac.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
                @UniqueConstraint(name = "uk_rbac_role_realm_name", columnNames = { "realm_id", "name" })
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

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "created_by")
        @JsonIgnore
        private User createdBy;

        @OneToMany(mappedBy = "role", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
        @JsonIgnore
        @ToString.Exclude
        @Builder.Default
        private List<UserRole> userRoles = new java.util.ArrayList<>();

        @OneToMany(mappedBy = "role", fetch = FetchType.LAZY)
        @JsonIgnore
        @ToString.Exclude
        @Builder.Default
        private List<GroupRole> groupRoles = new java.util.ArrayList<>();

        /**
         * Permissions associated with this role (many-to-many relationship)
         */
        @JsonIgnore
        @OneToMany(fetch = FetchType.LAZY, mappedBy = "role", orphanRemoval = true, cascade = CascadeType.ALL)
        @ToString.Exclude
        private List<RolePermission> rolePermissions;

        @PreUpdate
        protected void onUpdate() {
                this.updatedAt = LocalDateTime.now();
        }
}