package com.open.rbac.openrbac.requestParams;

import lombok.*;
import lombok.experimental.SuperBuilder;


@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class UserFilterRequest extends BaseFilter {
    private String name;
    private String description;
    private String status;
    private String keycloakUserId;
    private String firstName;
    private String lastName;
    private String email;
    private String username;
    private String displayName;
}
