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
          SELECT DISTINCT
             cp.id,
             cp.user_id,
             cp.permission_id,
             cp.assigned_by,
             cp.created_at,
             cp.expiry_date,
             cp.is_active,
             cp.assignment_type
          FROM (
              SELECT
                 concat('D', up.id) as id,
                 up.user_id,
                 up.permission_id,
                 up.assigned_by,
                 up.created_at,
                 up.expiry_date,
                 (CASE WHEN up.is_active = true THEN true ELSE false END) as is_active,
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
                 (CASE WHEN ur.is_active = true THEN true ELSE false END) as is_active,
                 'ROLE' as assignment_type
              FROM user_roles ur
              JOIN role_permissions rp ON ur.role_id = rp.role_id
              JOIN roles r ON ur.role_id = r.id
              WHERE r.status = 'ACTIVE'
              UNION ALL
              SELECT
                 concat('G', gp.id, 'U', ug.id) as id,
                 ug.user_id,
                 gp.permission_id,
                 gp.assigned_by,
                 ug.created_at,
                 CASE
                     WHEN ug.expiry_date IS NULL THEN gp.expiry_date
                     WHEN gp.expiry_date IS NULL THEN ug.expiry_date
                     WHEN ug.expiry_date < gp.expiry_date THEN ug.expiry_date
                     ELSE gp.expiry_date
                 END as expiry_date,
                 (CASE WHEN ug.is_active = true AND gp.is_active = true THEN true ELSE false END) as is_active,
                 'GROUP' as assignment_type
              FROM user_groups ug
              JOIN groups desc_g ON ug.group_id = desc_g.id
              JOIN groups anc_g ON desc_g.path LIKE concat(anc_g.path, '%')
              JOIN group_permissions gp ON gp.group_id = anc_g.id
              WHERE desc_g.status = 'ACTIVE'
                AND anc_g.status = 'ACTIVE'
                AND ((gp.group_id = ug.group_id)
                 OR (gp.allow_inheritance = true AND (gp.max_inheritance_depth IS NULL OR (desc_g.level - anc_g.level) <= gp.max_inheritance_depth)))
              UNION ALL
              SELECT
                 concat('GR', gr.id, 'RP', rp.id, 'U', ug.id) as id,
                 ug.user_id,
                 rp.permission_id,
                 gr.assigned_by,
                 ug.created_at,
                 CASE
                     WHEN ug.expiry_date IS NULL AND gr.expiry_date IS NULL THEN rp.expiry_date
                     WHEN ug.expiry_date IS NULL AND rp.expiry_date IS NULL THEN gr.expiry_date
                     WHEN gr.expiry_date IS NULL AND rp.expiry_date IS NULL THEN ug.expiry_date
                     WHEN ug.expiry_date IS NULL THEN (CASE WHEN gr.expiry_date < rp.expiry_date THEN gr.expiry_date ELSE rp.expiry_date END)
                     WHEN gr.expiry_date IS NULL THEN (CASE WHEN ug.expiry_date < rp.expiry_date THEN ug.expiry_date ELSE rp.expiry_date END)
                     WHEN rp.expiry_date IS NULL THEN (CASE WHEN ug.expiry_date < gr.expiry_date THEN ug.expiry_date ELSE gr.expiry_date END)
                     ELSE (CASE
                         WHEN ug.expiry_date < gr.expiry_date AND ug.expiry_date < rp.expiry_date THEN ug.expiry_date
                         WHEN gr.expiry_date < rp.expiry_date THEN gr.expiry_date
                         ELSE rp.expiry_date
                     END)
                 END as expiry_date,
                 (CASE WHEN ug.is_active = true AND gr.is_active = true THEN true ELSE false END) as is_active,
                 'GROUP_ROLE' as assignment_type
              FROM user_groups ug
              JOIN groups desc_g ON ug.group_id = desc_g.id
              JOIN groups anc_g ON desc_g.path LIKE concat(anc_g.path, '%')
              JOIN group_roles gr ON gr.group_id = anc_g.id
              JOIN roles r ON gr.role_id = r.id
              JOIN role_permissions rp ON gr.role_id = rp.role_id
              WHERE r.status = 'ACTIVE'
                AND desc_g.status = 'ACTIVE'
                AND anc_g.status = 'ACTIVE'
                AND ((gr.group_id = ug.group_id)
                 OR (gr.allow_inheritance = true AND (gr.max_inheritance_depth IS NULL OR (desc_g.level - anc_g.level) <= gr.max_inheritance_depth)))
          ) cp
          JOIN permissions p ON cp.permission_id = p.id
          WHERE p.status = 'ACTIVE'
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
