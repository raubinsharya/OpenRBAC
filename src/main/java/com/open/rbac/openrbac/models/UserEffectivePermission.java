package com.open.rbac.openrbac.models;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Subselect;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Entity
@Immutable
@Subselect("""
        SELECT
            concat('D', up.id) as id,
            up.user_id,
            up.permission_id,
            up.assigned_by,
            up.created_at,
            up.expiry_date,
            up.is_active,
            'DIRECT' as assignment_type
        FROM user_permissions up
        UNION ALL
        SELECT
            concat('R', ur.id, 'P', rp.id) as id,
            ur.user_id,
            rp.permission_id,
            ur.assigned_by,
            ur.created_at,
            CASE
               WHEN ur.expiry_date IS NULL THEN rp.expiry_date
               WHEN rp.expiry_date IS NULL THEN ur.expiry_date
               WHEN ur.expiry_date < rp.expiry_date THEN ur.expiry_date
               ELSE rp.expiry_date
            END as expiry_date,
            (CASE WHEN ur.is_active IS TRUE THEN true ELSE false END) as is_active,
            'ROLE' as assignment_type
        FROM user_roles ur
        JOIN role_permissions rp ON ur.role_id = rp.role_id
        """)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEffectivePermission {
    @Id
    private String id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "permission_id")
    private Permission permission;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by")
    private User assignedBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "assignment_type")
    private String assignmentType;
}
