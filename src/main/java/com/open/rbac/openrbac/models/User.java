package com.open.rbac.openrbac.models;

import com.fasterxml.jackson.annotation.*;
import com.open.rbac.openrbac.enums.EntityStatus;
import jakarta.persistence.*;
import org.hibernate.envers.Audited;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * User entity for RBAC system
 * Authentication is handled by Keycloak - this entity only stores
 * authorization-related data
 */
@Entity
@Audited
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

@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    // === KEYCLOAK INTEGRATION ===
    @Column(name = "keycloak_user_id", nullable = false, unique = true)
    private String keycloakUserId;

    // === USER INFORMATION (synced from Keycloak) ===
    @Column(nullable = false, length = 100)
    @NotBlank(message = "First name is required")
    @Size(min = 1, max = 100, message = "First name must be between 1 and 100 characters")
    private String firstName;

    @Column(length = 100)
    @Size(max = 100, message = "Last name cannot exceed 100 characters")
    private String lastName;

    @Column(length = 150)
    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    @Size(max = 150, message = "Email cannot exceed 150 characters")
    private String email;

    @Column(nullable = false, length = 100)
    @NotBlank(message = "Username is required")
    @Size(min = 2, max = 100, message = "Username must be between 2 and 100 characters")
    private String username;

    // === RBAC STATUS ===
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

    // === RBAC REALM ===
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "realm_id", nullable = false)
    @JsonIgnore
    private Realm realm;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @ToString.Exclude
    @Builder.Default
    private List<UserRole> userRoles = new java.util.ArrayList<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @ToString.Exclude
    @Builder.Default
    private List<UserPermission> userPermissions = new java.util.ArrayList<>();

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

    public String getDisplayName() {
        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        } else if (firstName != null) {
            return firstName;
        } else if (username != null) {
            return username;
        } else {
            return email;
        }
    }

    public boolean isAccountExpired() {
        if (this.accountExpiryDate == null)
            return false;
        return this.accountExpiryDate.isBefore(LocalDateTime.now());
    }
}
