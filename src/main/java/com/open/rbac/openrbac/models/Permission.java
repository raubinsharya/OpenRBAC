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
import java.util.Set;

import com.open.rbac.openrbac.enums.EntityStatus;

/**
 * Permission entity for fine-grained access control
 * Permissions follow the RESOURCE_ACTION naming convention (e.g., BOOK_CREATE,
 * USER_EDIT)
 */
@Entity
@Table(name = "permissions", indexes = {
        @Index(name = "idx_permission_realm_name", columnList = "realm_id, name"),
        @Index(name = "idx_permission_resource", columnList = "resource"),
        @Index(name = "idx_permission_action", columnList = "action"),
        @Index(name = "idx_permission_resource_action", columnList = "resource, action"),
        @Index(name = "idx_permission_status", columnList = "status")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_permission_realm_name", columnNames = { "realm_id", "name" })
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    /**
     * Permission name following RESOURCE_ACTION convention
     * Examples: BOOK_CREATE, USER_EDIT, REPORT_VIEW, ADMIN_MANAGE
     */
    @Column(nullable = false, length = 100)
    @NotBlank(message = "Name is required")
    @Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters")
    private String name;

    /**
     * Resource that this permission applies to
     * Examples: BOOK, USER, REPORT, ADMIN
     */
    @Column(nullable = false, length = 50)
    @NotBlank(message = "Resource is required")
    @Size(min = 2, max = 50, message = "Resource must be between 2 and 50 characters")
    private String resource;

    /**
     * Action that can be performed on the resource
     * Examples: CREATE, READ, UPDATE, DELETE, MANAGE, VIEW, EDIT
     */
    @Column(nullable = false, length = 50)
    @NotBlank(message = "Action is required")
    @Size(min = 2, max = 50, message = "Action must be between 2 and 50 characters")
    private String action;

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

    @ManyToMany(mappedBy = "permissions")
    @JsonIgnore
    private Set<User> users;

    @OneToMany(mappedBy = "permission", fetch = FetchType.LAZY)
    @JsonIgnore
    @ToString.Exclude
    private Set<RolePermission> rolePermissions;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "realm_id", nullable = false)
    @JsonIgnore
    private Realm realm;

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}