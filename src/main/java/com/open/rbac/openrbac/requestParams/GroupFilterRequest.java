package com.open.rbac.openrbac.requestParams;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class GroupFilterRequest extends BaseFilter {
    private String name;
    private String description;
    private String status;
    private String createdBy;
}
