package com.open.rbac.openrbac.controllers;

import com.open.rbac.openrbac.requestParams.RolePermissionFilterRequest;
import com.open.rbac.openrbac.requests.AddRolePermissionsRequest;
import com.open.rbac.openrbac.requests.RemoveRolePermissionsRequest;
import com.open.rbac.openrbac.services.RolePermissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/realms/{realmId}/roles/{id}/permissions")
@RequiredArgsConstructor
public class RolePermissionController {

    private final RolePermissionService rolePermissionService;

    @GetMapping
    public ResponseEntity<?> getRolePermissions(
            @PathVariable Long realmId,
            @PathVariable Long id,
            @ModelAttribute @Valid RolePermissionFilterRequest filter) {
        return ResponseEntity.ok(rolePermissionService.getRolePermissions(realmId, id, filter));
    }

    @GetMapping("/check")
    public ResponseEntity<?> checkPermission(
            @PathVariable Long realmId,
            @PathVariable Long id,
            @ModelAttribute com.open.rbac.openrbac.requestParams.CheckPermissionRequest request) {
        boolean hasPermission = rolePermissionService.checkRolePermission(realmId, id, request);
        return ResponseEntity.ok(Map.of("hasPermission", hasPermission));
    }

    @PostMapping
    public ResponseEntity<?> addPermissions(
            @PathVariable Long realmId,
            @PathVariable Long id,
            @RequestBody @Valid AddRolePermissionsRequest request) {
        rolePermissionService.addPermissionsToRole(realmId, id, request);
        return ResponseEntity.ok(Map.of("message", "Permissions added successfully"));
    }

    @DeleteMapping
    public ResponseEntity<?> removePermissions(
            @PathVariable Long realmId,
            @PathVariable Long id,
            @RequestBody @Valid RemoveRolePermissionsRequest request) {
        rolePermissionService.removePermissionsFromRole(realmId, id, request);
        return ResponseEntity.ok(Map.of("message", "Permissions removed successfully"));
    }

    @GetMapping("/resources")
    public ResponseEntity<?> getRoleResources(
            @PathVariable Long realmId,
            @PathVariable Long id,
            org.springframework.data.domain.Pageable pageable) {
        return ResponseEntity.ok(rolePermissionService.getRoleResources(realmId, id, pageable));
    }

    @GetMapping("/actions")
    public ResponseEntity<?> getRoleActions(
            @PathVariable Long realmId,
            @PathVariable Long id,
            org.springframework.data.domain.Pageable pageable) {
        return ResponseEntity.ok(rolePermissionService.getRoleActions(realmId, id, pageable));
    }
}
