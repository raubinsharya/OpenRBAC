package com.open.rbac.openrbac.requests;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.open.rbac.openrbac.annotations.DateStrategy;
import com.open.rbac.openrbac.annotations.FlexibleDate;
import com.open.rbac.openrbac.utils.FlexibleDateDeserializer;

@Data
public class AddUserRolesRequest {
    @NotEmpty(message = "Role IDs cannot be empty")
    private Set<Long> roleIds;

    @FlexibleDate(strategy = DateStrategy.END_OF_DAY)
    @JsonDeserialize(using = FlexibleDateDeserializer.class)
    private LocalDateTime expiryDate;

}
