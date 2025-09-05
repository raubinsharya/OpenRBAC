package com.open.rbac.openrbac.models;

import com.open.rbac.openrbac.enums.EntityStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * User entity for RBAC system
 * Authentication is handled by Keycloak - this entity only stores
 * authorization-related data
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "_user", indexes = {
        @Index(name = "idx_user_keycloak_id", columnList = "keycloak_user_id"),
        @Index(name = "idx_user_realm_email", columnList = "realm_id, email"),
        @Index(name = "idx_user_status", columnList = "status"),
        @Index(name = "idx_user_expiry", columnList = "account_expiry_date")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_realm_email", columnNames = { "realm_id", "email" }),
        @UniqueConstraint(name = "uk_user_keycloak_id", columnNames = { "keycloak_user_id" })
})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // === KEYCLOAK INTEGRATION ===
    @Column(name = "keycloak_user_id", nullable = false, unique = true)
    private String keycloakUserId;

    // === RBAC REALM ===
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "realm_id", nullable = false)
    private Realm realm;

    // === USER INFORMATION (synced from Keycloak) ===
    @Column(nullable = false)
    private String firstName;

    private String lastName;

    @Column(nullable = true)
    @Email(message = "Invalid email format")
    private String email;

    private String username;

    // === STATUS ===
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EntityStatus status = EntityStatus.ACTIVE;

    // === ACCOUNT MANAGEMENT ===
    @Column(name = "account_expiry_date")
    private LocalDateTime accountExpiryDate;

    // === AUDIT FIELDS ===
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // === LIFECYCLE CALLBACKS ===
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // === UTILITY METHODS ===

    /**
     * Get display name for UI purposes
     */
    public String getDisplayName() {
        // Full name if both parts exist
        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        }
        // Fallback chain
        return Optional.ofNullable(firstName)
            .or(() -> Optional.ofNullable(username))
            .or(() -> Optional.ofNullable(email))
            .orElse("");
    }
    

    /**
     * Get username for display purposes
     */
    public String getDisplayUsername() {
        return Optional.ofNullable(this.username).orElse(this.email);
    }

    /**
     * Check if account is not expired
     */
    public boolean isAccountNonExpired() {
        return accountExpiryDate == null || accountExpiryDate.isAfter(LocalDateTime.now());
    }

    /**
     * Check if account is not locked (for RBAC purposes)
     */
    public boolean isAccountNonLocked() {
        return status != EntityStatus.BLOCKED && status != EntityStatus.DISABLED;
    }

    /**
     * Check if user is enabled for RBAC operations
     */
    public boolean isEnabled() {
        return status == EntityStatus.ACTIVE && isAccountNonExpired() &&
                (realm == null || realm.isActive());
    }

    /**
     * Check if user is active in their realm
     */
    public boolean isActiveInRealm() {
        return status == EntityStatus.ACTIVE &&
                realm != null && realm.isActive() &&
                isAccountNonExpired();
    }

}
