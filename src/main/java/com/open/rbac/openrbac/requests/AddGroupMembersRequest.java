package com.open.rbac.openrbac.requests;

import java.time.LocalDateTime;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.open.rbac.openrbac.annotations.DateStrategy;
import com.open.rbac.openrbac.annotations.FlexibleDate;
import com.open.rbac.openrbac.utils.FlexibleDateDeserializer;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class AddGroupMembersRequest {

    @NotEmpty(message = "User IDs cannot be empty")
    private java.util.List<Long> userIds;

    @FlexibleDate(strategy = DateStrategy.END_OF_DAY)
    @JsonDeserialize(using = FlexibleDateDeserializer.class)
    private LocalDateTime expiryDate;
}
