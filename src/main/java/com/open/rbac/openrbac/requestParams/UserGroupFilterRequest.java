package com.open.rbac.openrbac.requestParams;

import com.open.rbac.openrbac.enums.EntityStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import com.open.rbac.openrbac.annotations.DateStrategy;
import com.open.rbac.openrbac.annotations.FlexibleDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class UserGroupFilterRequest extends BaseFilter {
    private Long id;
    private String keycloakUserId;
    private String displayName;
    private String email;
    private String assignedBy;
    private EntityStatus status;
    private EntityStatus groupStatus;
    private Boolean isGroupMembershipExpired;
    private Boolean isGroupMembershipValid;

    @FlexibleDate(strategy = DateStrategy.START_OF_DAY)
    private LocalDateTime assignedAtAfter;

    @FlexibleDate(strategy = DateStrategy.END_OF_DAY)
    private LocalDateTime assignedAtBefore;

    @FlexibleDate(strategy = DateStrategy.START_OF_DAY)
    private LocalDateTime groupMemberExpiryAfter;

    @FlexibleDate(strategy = DateStrategy.END_OF_DAY)
    private LocalDateTime groupMemberExpiryBefore;

}
