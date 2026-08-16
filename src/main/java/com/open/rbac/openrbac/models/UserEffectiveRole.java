package com.open.rbac.openrbac.models;

import org.hibernate.envers.Audited;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Subselect;

import java.time.LocalDateTime;

@Entity
@Audited
@Immutable
@Subselect("""
            SELECT
               concat('D', ur.id) as id,
               ur.user_id,
               ur.role_id,
               'DIRECT' as assignment_type,
               NULL as source_group_id,
               ur.created_at,
               ur.expiry_date,
               ur.is_active,
               ur.assigned_by as assigned_by_id
            FROM user_roles ur
            JOIN roles r ON ur.role_id = r.id
            WHERE r.status = 'ACTIVE'
            UNION ALL
            SELECT
               concat('G', gr.id, 'U', ug.id) as id,
               ug.user_id,
               gr.role_id,
               'GROUP' as assignment_type,
               gr.group_id as source_group_id,
               ug.created_at,
               CASE
                   WHEN ug.expiry_date IS NULL THEN gr.expiry_date
                   WHEN gr.expiry_date IS NULL THEN ug.expiry_date
                   WHEN ug.expiry_date < gr.expiry_date THEN ug.expiry_date
                   ELSE gr.expiry_date
               END as expiry_date,
               (CASE WHEN ug.is_active = true AND gr.is_active = true THEN true ELSE false END) as is_active,
               gr.assigned_by as assigned_by_id
            FROM user_groups ug
            JOIN groups desc_g ON ug.group_id = desc_g.id
            JOIN groups anc_g ON desc_g.path LIKE concat(anc_g.path, '%')
            JOIN group_roles gr ON gr.group_id = anc_g.id
            JOIN roles r ON gr.role_id = r.id
            WHERE r.status = 'ACTIVE'
               AND desc_g.status = 'ACTIVE'
               AND anc_g.status = 'ACTIVE'
               AND ((gr.group_id = ug.group_id)
                   OR (gr.allow_inheritance = true
                       AND (gr.max_inheritance_depth IS NULL OR (desc_g.level - anc_g.level) <= gr.max_inheritance_depth)))
        """)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class UserEffectiveRole {

    @Id
    private String id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id")
    private Role role;

    @Column(name = "assignment_type")
    private String assignmentType;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "source_group_id")
    private Group sourceGroup;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;

    @Column(name = "is_active")
    private Boolean isActive;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assigned_by_id")
    private User assignedBy;
}
