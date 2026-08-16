package com.open.rbac.openrbac.models;

import jakarta.persistence.*;
import org.hibernate.envers.Audited;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Association entity for direct User-Permission assignments with expiry support
 * Allows granting specific permissions directly to users without roles
 */
@Entity
@Audited
@Table(name = "user_permissions", indexes = {
                @Index(name = "idx_user_permission_user", columnList = "user_id"),
                @Index(name = "idx_user_permission_permission", columnList = "permission_id"),
                @Index(name = "idx_user_permission_expiry", columnList = "expiry_date"),
                @Index(name = "idx_user_permission_active", columnList = "is_active")
}, uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_permission", columnNames = { "user_id", "permission_id" })
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPermission {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "user_id", nullable = false)
        @NotNull(message = "User is required")
        private User user;

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
         * Expiry date for temporary permission grant
         * NULL means permanent permission
         */
        @Column(name = "expiry_date")
        private LocalDateTime expiryDate;

        @Column(name = "is_active", nullable = false)
        @NotNull(message = "Activation status is required")
        @Builder.Default
        private Boolean isActive = true;

}