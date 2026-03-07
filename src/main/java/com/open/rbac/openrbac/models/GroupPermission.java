package com.open.rbac.openrbac.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Association entity for Group-Permission assignments with inheritance tracking
 * and expiry support
 * Enables direct permission assignment to groups (bypassing roles)
 */
@Entity
@Table(name = "group_permissions", indexes = {
                @Index(name = "idx_group_permission_group", columnList = "group_id"),
                @Index(name = "idx_group_permission_permission", columnList = "permission_id"),
                @Index(name = "idx_group_permission_inherited", columnList = "is_inherited"),
                @Index(name = "idx_group_permission_source", columnList = "source_group_id"),
                @Index(name = "idx_group_permission_expiry", columnList = "expiry_date"),
                @Index(name = "idx_group_permission_active", columnList = "is_active")
}, uniqueConstraints = {
                @UniqueConstraint(name = "uk_group_permission", columnNames = { "group_id", "permission_id" })
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
        @NotNull(message = "Group is required")
        private Group group;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "permission_id", nullable = false)
        @NotNull(message = "Permission is required")
        private Permission permission;

        @Column(name = "created_at", nullable = false, updatable = false)
        @Builder.Default
        private LocalDateTime createdAt = LocalDateTime.now();

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
        @NotNull(message = "Inheritance status is required")
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
        @NotNull(message = "Activation status is required")
        @Builder.Default
        private Boolean isActive = true;

        /**
         * Controls whether this permission can be inherited by child groups
         * Default: false (no inheritance)
         */
        @Column(name = "allow_inheritance", nullable = false)
        @NotNull(message = "Allow inheritance setting is required")
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