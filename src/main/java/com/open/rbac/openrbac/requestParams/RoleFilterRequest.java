package com.open.rbac.openrbac.requestParams;

import lombok.*;
import lombok.experimental.SuperBuilder;


@Data

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class RoleFilterRequest extends BaseFilter {
    private String name;
    private String description;
    private String status;
    private Boolean isSystemRole;
}
