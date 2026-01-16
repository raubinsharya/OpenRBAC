package com.open.rbac.openrbac.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.ToString;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Association entity for Role-Permission assignments
 * Defines which permissions are contained within each role
 */
@Entity
@Table(name = "role_permissions", indexes = {
                @Index(name = "idx_role_permission_role", columnList = "role_id"),
                @Index(name = "idx_role_permission_permission", columnList = "permission_id")
}, uniqueConstraints = {
                @UniqueConstraint(name = "uk_role_permission", columnNames = { "role_id", "permission_id" })
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RolePermission {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "role_id", nullable = false)
        @ToString.Exclude
        private Role role;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "permission_id", nullable = false)
        private Permission permission;

        @Column(name = "created_at", nullable = false, updatable = false)
        @Builder.Default
        private LocalDateTime createdAt = LocalDateTime.now();

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "assigned_by")
        private User assignedBy;
}