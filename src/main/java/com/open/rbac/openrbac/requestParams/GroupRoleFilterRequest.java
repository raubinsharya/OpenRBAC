package com.open.rbac.openrbac.requestParams;

import com.open.rbac.openrbac.annotations.FlexibleDate;
import com.open.rbac.openrbac.annotations.DateStrategy;
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
public class GroupRoleFilterRequest extends BaseFilter {
    private String roleName;
    private EntityStatus roleStatus;
    private String assignedBy;
    private EntityStatus groupStatus;
    private Boolean isActive;
    private Boolean isInherited;

    @FlexibleDate(strategy = DateStrategy.END_OF_DAY)
    private LocalDateTime assignedAtBefore;

    @FlexibleDate(strategy = DateStrategy.START_OF_DAY)
    private LocalDateTime assignedAtAfter;

    @FlexibleDate(strategy = DateStrategy.END_OF_DAY)
    private LocalDateTime expiryDateBefore;

    @FlexibleDate(strategy = DateStrategy.START_OF_DAY)
    private LocalDateTime expiryDateAfter;
}
