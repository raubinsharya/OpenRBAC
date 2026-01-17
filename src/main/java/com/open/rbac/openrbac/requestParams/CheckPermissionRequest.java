package com.open.rbac.openrbac.requestParams;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
public class CheckPermissionRequest {
    private String resource;
    private String action;
    private String assignmentType;
    private String permissionName;
}
