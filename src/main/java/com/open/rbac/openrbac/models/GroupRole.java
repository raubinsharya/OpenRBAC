package com.open.rbac.openrbac.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Association entity for Group-Role assignments with inheritance tracking and
 * expiry support
 * Supports both direct role assignments and inherited roles from parent groups
 */
@Entity
@Table(name = "group_roles", indexes = {
        @Index(name = "idx_group_role_group", columnList = "group_id"),
        @Index(name = "idx_group_role_role", columnList = "role_id"),
        @Index(name = "idx_group_role_inherited", columnList = "is_inherited"),
        @Index(name = "idx_group_role_source", columnList = "source_group_id"),
        @Index(name = "idx_group_role_expiry", columnList = "expiry_date"),
        @Index(name = "idx_group_role_active", columnList = "is_active")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_group_role", columnNames = { "group_id", "role_id" })
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

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

    /**
     * True if this role is inherited from a parent group
     * False if directly assigned to this group
     */
    @Column(name = "is_inherited", nullable = false)
    @Builder.Default
    private Boolean isInherited = false;

    /**
     * Source group from which this role is inherited
     * NULL if directly assigned (not inherited)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_group_id")
    private Group sourceGroup;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    /**
     * Controls whether this role can be inherited by child groups
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

}