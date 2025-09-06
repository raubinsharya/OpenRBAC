package com.open.rbac.openrbac.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    private String message;
    private int status;
    private Date timestamp;
    private Map<String, String> errors;  // For validation errors (backward compatibility)
    private Map<String, Object> details; // For additional error details
    private String path;                 // Request path where error occurred
}
