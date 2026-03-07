package com.open.rbac.openrbac.controllers;

import com.open.rbac.openrbac.requestParams.CheckPermissionRequest;
import com.open.rbac.openrbac.requestParams.RolePermissionFilterRequest;
import com.open.rbac.openrbac.requests.AddRolePermissionsRequest;
import com.open.rbac.openrbac.requests.RemoveRolePermissionsRequest;
import com.open.rbac.openrbac.requests.UpdateRolePermissionsExpiryRequest;
import com.open.rbac.openrbac.services.RolePermissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
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
            @PathVariable String realmId,
            @PathVariable Long id,
            @ModelAttribute @Valid RolePermissionFilterRequest filter) {
        return ResponseEntity.ok(rolePermissionService.getRolePermissions(realmId, id, filter));
    }

    @GetMapping("/check")
    public ResponseEntity<?> checkPermission(
            @PathVariable String realmId,
            @PathVariable Long id,
            @ModelAttribute CheckPermissionRequest request) {
        boolean hasPermission = rolePermissionService.checkRolePermission(realmId, id, request);
        return ResponseEntity.ok(Map.of("hasPermission", hasPermission));
    }

    @PostMapping
    public ResponseEntity<?> addPermissions(
            @PathVariable String realmId,
            @PathVariable Long id,
            @RequestBody @Valid AddRolePermissionsRequest request) {
        rolePermissionService.addPermissionsToRole(realmId, id, request);
        return ResponseEntity.ok(Map.of("message", "Permissions added successfully"));
    }

    @DeleteMapping
    public ResponseEntity<?> removePermissions(
            @PathVariable String realmId,
            @PathVariable Long id,
            @RequestBody @Valid RemoveRolePermissionsRequest request) {
        rolePermissionService.removePermissionsFromRole(realmId, id, request);
        return ResponseEntity.ok(Map.of("message", "Permissions removed successfully"));
    }

    @PatchMapping
    public ResponseEntity<?> updatePermissionsExpiry(
            @PathVariable String realmId,
            @PathVariable Long id,
            @RequestBody @Valid UpdateRolePermissionsExpiryRequest request) {
        rolePermissionService.updatePermissionsExpiry(realmId, id, request);
        return ResponseEntity.ok(Map.of("message", "Permissions expiry updated successfully"));
    }

    @GetMapping("/resources")
    public ResponseEntity<?> getRoleResources(
            @PathVariable String realmId,
            @PathVariable Long id,
            Pageable pageable) {
        return ResponseEntity.ok(rolePermissionService.getRoleResources(realmId, id, pageable));
    }

    @GetMapping("/actions")
    public ResponseEntity<?> getRoleActions(
            @PathVariable String realmId,
            @PathVariable Long id,
            Pageable pageable) {
        return ResponseEntity.ok(rolePermissionService.getRoleActions(realmId, id, pageable));
    }
}
