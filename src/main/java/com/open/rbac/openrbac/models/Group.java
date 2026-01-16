package com.open.rbac.openrbac.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.open.rbac.openrbac.enums.EntityStatus;

/**
 * Group entity with materialized path hierarchy support
 * Enables efficient hierarchical queries and inheritance calculations
 */
@Entity
@Table(name = "groups", indexes = {
        @Index(name = "idx_group_realm_name", columnList = "realm_id, name"),
        @Index(name = "idx_group_path", columnList = "path"),
        @Index(name = "idx_group_realm_path", columnList = "realm_id, path"),
        @Index(name = "idx_group_parent", columnList = "parent_group_id"),
        @Index(name = "idx_group_level", columnList = "level"),
        @Index(name = "idx_group_status", columnList = "status")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_group_realm_parent_name", columnNames = { "realm_id", "path", "name" })
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "realm_id", nullable = false)
    @JsonIgnore
    private Realm realm;

    @Column(nullable = false, length = 100)
    @NotBlank(message = "Group name is required")
    @Size(min = 2, max = 100, message = "Group name must be between 2 and 100 characters")
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_group_id")
    @JsonIgnore
    private Group parentGroup;

    /**
     * Materialized path of ancestors
     * Format: /1/5/ where numbers are ancestor group IDs
     * Root groups have path /
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String path;

    /**
     * Hierarchy level (0 for root groups, 1 for first level children, etc.)
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer level = 0;

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

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @OneToMany(mappedBy = "group", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<GroupRole> groupRoles;

    @OneToMany(mappedBy = "group", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<UserGroup> userGroups;

    /**
     * Get list of ancestor group IDs from the materialized path
     * 
     * @return List of ancestor IDs in order from root to immediate parent
     */
    public List<Long> getAncestorIds() {
        if (path == null || path.equals("/")) {
            return List.of();
        }

        return Arrays.stream(path.split("/"))
                .filter(s -> !s.isEmpty())
                .map(Long::valueOf)
                .collect(Collectors.toList());
    }

    /**
     * Check if this group is a descendant of another group
     * 
     * @param other The potential ancestor group
     * @return true if this group is a descendant of the other group
     */
    public boolean isDescendantOf(Group other) {
        if (other == null || this.path == null || other.path == null) {
            return false;
        }
        String otherPathIdentifier = other.path + other.getId() + "/";
        return this.path.startsWith(otherPathIdentifier);
    }

    /**
     * Check if this group is an ancestor of another group
     * 
     * @param other The potential descendant group
     * @return true if this group is an ancestor of the other group
     */
    public boolean isAncestorOf(Group other) {
        if (other == null) {
            return false;
        }
        return other.isDescendantOf(this);
    }

    /**
     * Generate path for a child group
     * 
     * @return The materialized path for the child
     */
    public String generatePathForChild() {
        if (this.path == null || this.path.equals("/")) {
            return "/" + this.getId() + "/";
        }
        return this.path + this.getId() + "/";
    }

    /**
     * Check if group is active and can be used
     */
    public boolean isActive() {
        return EntityStatus.ACTIVE.equals(this.status);
    }

    /**
     * Check if group is blocked (temporarily disabled)
     */
    public boolean isBlocked() {
        return EntityStatus.BLOCKED.equals(this.status);
    }

    /**
     * Check if group is disabled (requires admin intervention)
     */
    public boolean isDisabled() {
        return EntityStatus.DISABLED.equals(this.status);
    }

    /**
     * Check if this group is a root group (no parent)
     */
    public boolean isRoot() {
        return parentGroup == null;
    }

    /**
     * Get the depth of this group in the hierarchy
     */
    public int getDepth() {
        return level;
    }
}