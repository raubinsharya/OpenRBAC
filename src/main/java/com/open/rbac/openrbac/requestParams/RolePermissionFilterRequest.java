package com.open.rbac.openrbac.requestParams;

import com.open.rbac.openrbac.annotations.DateStrategy;
import com.open.rbac.openrbac.annotations.FlexibleDate;
import com.open.rbac.openrbac.enums.EntityStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class RolePermissionFilterRequest extends BaseFilter {
    private String permissionName;
    private String description;
    private String status;
    private String resource;
    private String action;
    private String assignedBy;
    private EntityStatus roleStatus;

    @FlexibleDate(strategy = DateStrategy.END_OF_DAY)
    private LocalDateTime assignedAtBefore;

    @FlexibleDate(strategy = DateStrategy.START_OF_DAY)
    private LocalDateTime assignedAtAfter;
}