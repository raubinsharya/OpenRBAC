package com.open.rbac.openrbac.controllers;

import com.open.rbac.openrbac.dtos.AuditRevisionDto;
import com.open.rbac.openrbac.services.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;

    @GetMapping("/{entityType}/{entityId}")
    public ResponseEntity<List<AuditRevisionDto<Object>>> getEntityHistory(
            @PathVariable String entityType,
            @PathVariable String entityId) {
        
        try {
            List<AuditRevisionDto<Object>> history = auditService.getEntityRevisions(entityType, entityId);
            return ResponseEntity.ok(history);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
