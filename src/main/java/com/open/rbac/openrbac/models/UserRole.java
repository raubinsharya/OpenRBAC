package com.open.rbac.openrbac.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Association entity for direct User-Role assignments with expiry support
 * Allows assigning roles directly to users (in addition to group-based roles)
 */
@Entity
@Table(name = "user_roles", indexes = {
                @Index(name = "idx_user_role_user", columnList = "user_id"),
                @Index(name = "idx_user_role_role", columnList = "role_id"),
                @Index(name = "idx_user_role_expiry", columnList = "expiry_date"),
                @Index(name = "idx_user_role_active", columnList = "is_active")
}, uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_role", columnNames = { "user_id", "role_id" })
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRole {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "user_id", nullable = false)
        private User user;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "role_id", nullable = false)
        private Role role;

        @Column(name = "created_at", nullable = false, updatable = false)
        @Builder.Default
        private LocalDateTime createdAt = LocalDateTime.now();

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "assigned_by")
        private User assignedBy;

        /**
         * Expiry date for temporary role assignment
         * NULL means permanent assignment
         */
        @Column(name = "expiry_date")
        private LocalDateTime expiryDate;

        @Column(name = "is_active", nullable = false)
        @Builder.Default
        private Boolean isActive = true;
}