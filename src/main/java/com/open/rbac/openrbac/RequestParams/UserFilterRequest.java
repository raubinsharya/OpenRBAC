package com.open.rbac.openrbac.RequestParams;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;


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
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
