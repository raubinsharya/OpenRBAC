package com.open.rbac.openrbac.requestParams;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class GroupPermissionFilterRequest extends BaseFilter {
    private String permissionName;
    private com.open.rbac.openrbac.enums.EntityStatus permissionStatus;
    private com.open.rbac.openrbac.enums.EntityStatus groupStatus;
    private String assignedBy;
    private Boolean isActive;
    private Boolean isInherited;
    private LocalDateTime assignedAtBefore;
    private LocalDateTime assignedAtAfter;
    private LocalDateTime expiryDateBefore;
    private LocalDateTime expiryDateAfter;
    private Boolean fromRole;
}
