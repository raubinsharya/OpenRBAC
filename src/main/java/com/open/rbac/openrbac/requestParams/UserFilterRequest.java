package com.open.rbac.openrbac.requestParams;

import java.time.LocalDateTime;

import com.open.rbac.openrbac.annotations.DateStrategy;
import com.open.rbac.openrbac.annotations.FlexibleDate;
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
    private Boolean isAccountExpired;

    @FlexibleDate(strategy = DateStrategy.START_OF_DAY)
    private LocalDateTime accountExpiryDateAfter;

    @FlexibleDate(strategy = DateStrategy.END_OF_DAY)
    private LocalDateTime accountExpiryDateBefore;
}
