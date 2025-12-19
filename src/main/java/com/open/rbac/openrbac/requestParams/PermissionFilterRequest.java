package com.open.rbac.openrbac.requestParams;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class PermissionFilterRequest extends BaseFilter {
    private String name;
    private String description;
    private String status;
    private String resource;
    private String action;
}