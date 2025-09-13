package com.open.rbac.openrbac.models;

import com.fasterxml.jackson.annotation.*;
import com.open.rbac.openrbac.enums.EntityStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;


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
        @UniqueConstraint(name = "uk_user_realm_email", columnNames = {"realm_id", "email"}),
        @UniqueConstraint(name = "uk_user_keycloak_id", columnNames = {"keycloak_user_id"})
})

@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id"
)
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
    @Column(nullable = false)
    private String firstName;

    @Column
    private String lastName;

    @Column()
    @Email(message = "Invalid email format")
    private String email;

    @Column(nullable = false)
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
    private Realm realm;

    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"), indexes = {
            @Index(name = "idx_user_role", columnList = "user_id"),
            @Index(name = "idx_role_id", columnList = "role_id")
    }, uniqueConstraints = {
            @UniqueConstraint(name = "uk_user_role", columnNames = {"user_id", "role_id"})
    })
    @JsonIgnore
    private Set<Role> roles;

    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "user_permissions", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "permission_id"), indexes = {
            @Index(name = "idx_user_permission", columnList = "user_id"),
            @Index(name = "idx_permission_id", columnList = "permission_id")
    }, uniqueConstraints = {
            @UniqueConstraint(name = "uk_user_permission", columnNames = {"user_id", "permission_id"})
    })
    @JsonIgnore
    private Set<Permission> permissions;


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
}
