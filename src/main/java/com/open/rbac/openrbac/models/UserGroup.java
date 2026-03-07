package com.open.rbac.openrbac.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Association entity for User-Group membership with expiry support
 * Supports temporary group memberships for guest users and contractors
 */
@Entity
@Table(name = "user_groups", indexes = {
        @Index(name = "idx_user_group_user", columnList = "user_id"),
        @Index(name = "idx_user_group_group", columnList = "group_id"),
        @Index(name = "idx_user_group_expiry", columnList = "expiry_date"),
        @Index(name = "idx_user_group_active", columnList = "is_active")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_group", columnNames = { "user_id", "group_id" })
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull(message = "User is required")
    private User user;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    @NotNull(message = "Group is required")
    private Group group;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by")
    private User assignedBy;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * Expiry date for temporary group membership
     * NULL means permanent membership
     */
    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;

    @Column(name = "is_active", nullable = false)
    @NotNull(message = "Activation status is required")
    @Builder.Default
    private Boolean isActive = true;

    /**
     * Check if membership is currently valid (not expired and active)
     */
    public boolean isValid() {
        return Boolean.TRUE.equals(isActive) &&
                (expiryDate == null || expiryDate.isAfter(LocalDateTime.now()));
    }

    /**
     * Check if membership has expired
     */
    public boolean isExpired() {
        return expiryDate != null && expiryDate.isBefore(LocalDateTime.now());
    }
}