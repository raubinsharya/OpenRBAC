package com.open.rbac.openrbac.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditRevisionDto<T> {
    private Number revisionId;
    private Date revisionDate;
    private String revisionType;
    private T entity;
}
