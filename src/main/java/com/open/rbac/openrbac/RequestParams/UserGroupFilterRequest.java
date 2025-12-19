package com.open.rbac.openrbac.RequestParams;

import com.open.rbac.openrbac.enums.EntityStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.format.annotation.DateTimeFormat;

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

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime assignedAtAfter;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime assignedAtBefore;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime groupMemberExpiryAfter;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime groupMemberExpiryBefore;

}
