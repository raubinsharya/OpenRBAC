package com.open.rbac.openrbac.models;

import jakarta.persistence.*;
import org.hibernate.envers.Audited;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Subselect;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Entity
@Audited
@Immutable
@Subselect("""
        SELECT
            concat('D', gp.id) as id,
            gp.group_id,
            gp.permission_id,
            gp.assigned_by,
            gp.created_at,
            gp.expiry_date,
            'DIRECT' as assignment_type,
            gp.allow_inheritance,
            gp.max_inheritance_depth,
            (CASE WHEN gp.is_active IS TRUE THEN true ELSE false END) as is_active
        FROM group_permissions gp
        UNION ALL
        SELECT
            concat('R', gr.id, 'P', rp.id) as id,
            gr.group_id,
            rp.permission_id,
            gr.assigned_by,
            gr.created_at,
            CASE
               WHEN gr.expiry_date IS NULL THEN rp.expiry_date
               WHEN rp.expiry_date IS NULL THEN gr.expiry_date
               WHEN gr.expiry_date < rp.expiry_date THEN gr.expiry_date
               ELSE rp.expiry_date
            END as expiry_date,
            'ROLE' as assignment_type,
            gr.allow_inheritance,
            gr.max_inheritance_depth,
            (CASE WHEN gr.is_active IS TRUE AND (rp.expiry_date IS NULL OR rp.expiry_date > NOW()) THEN true ELSE false END) as is_active
        FROM group_roles gr
        JOIN role_permissions rp ON gr.role_id = rp.role_id
        """)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupEffectivePermission {
    @Id
    private String id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "group_id")
    private Group group;

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

    @Column(name = "assignment_type")
    private String assignmentType;

    @Column(name = "allow_inheritance")
    private Boolean allowInheritance;

    @Column(name = "max_inheritance_depth")
    private Integer maxInheritanceDepth;

    @Column(name = "is_active")
    private Boolean isActive;
}
