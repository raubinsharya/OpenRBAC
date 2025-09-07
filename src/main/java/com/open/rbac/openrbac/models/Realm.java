package com.open.rbac.openrbac.models;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.open.rbac.openrbac.enums.EntityStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "realms", indexes = {
        @Index(name = "idx_realm_name", columnList = "name"),
        @Index(name = "idx_realm_id", columnList = "realm_id"),
        @Index(name = "idx_realm_status", columnList = "status")
})
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id"
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Realm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 100)
    @NotBlank(message = "Realm name is required")
    @Size(min = 2, max = 100, message = "Realm name must be between 2 and 100 characters")
    private String name;

    @Column(name = "realm_id", unique = true, nullable = false, length = 100)
    @NotBlank(message = "Keycloak realm ID is required")
    private String realmId;

    @Column(columnDefinition = "TEXT")
    private String description;

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

    @Column(name = "created_by")
    private Long createdBy;

    // === BIDIRECTIONAL RELATIONSHIP ===
    @OneToMany(mappedBy = "realm", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnore // Prevent infinite recursion and lazy loading issues
    private List<User> users;

    @OneToMany(mappedBy = "realm", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnore // Prevent infinite recursion and lazy loading issues
    private List<Role> roles;

    @OneToMany(mappedBy = "realm", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnore // Prevent infinite recursion and lazy loading issues
    private List<Permission> permissions;

    @OneToMany(mappedBy = "realm", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnore // Prevent infinite recursion and lazy loading issues
    private List<Group> groups;

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}