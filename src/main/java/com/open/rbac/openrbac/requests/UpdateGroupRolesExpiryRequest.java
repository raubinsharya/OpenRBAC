package com.open.rbac.openrbac.requests;

import java.time.LocalDateTime;
import java.util.Set;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.open.rbac.openrbac.annotations.DateStrategy;
import com.open.rbac.openrbac.annotations.FlexibleDate;

import com.open.rbac.openrbac.utils.FlexibleDateDeserializer;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class UpdateGroupRolesExpiryRequest {

    @NotEmpty(message = "Role IDs cannot be empty")
    private Set<Long> roleIds;

    @JsonDeserialize(using = FlexibleDateDeserializer.class)
    @FlexibleDate(strategy = DateStrategy.END_OF_DAY)
    private LocalDateTime expiryDate;

    private Boolean allowInheritance;

    private Integer maxInheritanceDepth;
}
