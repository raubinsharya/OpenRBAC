package com.open.rbac.openrbac.requests;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class RemoveGroupMembersRequest {

    @NotEmpty(message = "User IDs cannot be empty")
    private List<Long> userId;
}
