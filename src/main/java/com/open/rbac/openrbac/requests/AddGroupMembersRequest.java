package com.open.rbac.openrbac.requests;

import java.time.LocalDateTime;
import java.util.List;

import com.open.rbac.openrbac.annotations.DateStrategy;
import com.open.rbac.openrbac.annotations.FlexibleDate;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class AddGroupMembersRequest {

    @NotEmpty(message = "User IDs cannot be empty")
    private List<Long> userId;

    @FlexibleDate(strategy = DateStrategy.END_OF_DAY)
    private LocalDateTime expiryDate;
}
