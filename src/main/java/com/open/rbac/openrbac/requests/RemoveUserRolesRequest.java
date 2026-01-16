package com.open.rbac.openrbac.requests;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.Set;

@Data
public class RemoveUserRolesRequest {
    @NotEmpty(message = "Role IDs cannot be empty")
    private Set<Long> roleIds;
}
