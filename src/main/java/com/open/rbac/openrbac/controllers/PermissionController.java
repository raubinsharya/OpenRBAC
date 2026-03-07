package com.open.rbac.openrbac.controllers;

import com.open.rbac.openrbac.requestParams.PermissionFilterRequest;
import com.open.rbac.openrbac.requestParams.ResourceFilterRequest;
import com.open.rbac.openrbac.models.Permission;
import com.open.rbac.openrbac.requests.StandardPermission;
import com.open.rbac.openrbac.services.PermissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.open.rbac.openrbac.requests.UpdatePermissionRequest;

@RestController
@RequestMapping("/api/v1/realms/{realmId}/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    @GetMapping
    public ResponseEntity<?> getAllPermissions(
            @PathVariable String realmId,
            @ModelAttribute PermissionFilterRequest permissionFilterRequest) {

        var permissions = permissionService.getAllPermissions(realmId, permissionFilterRequest);
        return ResponseEntity.ok(permissions);
    }

    @GetMapping("{id}")
    public ResponseEntity<?> getPermissionById(
            @PathVariable Long id,
            @PathVariable String realmId) {

        var permission = permissionService.getPermissionById(realmId, id);
        return ResponseEntity.ok(permission);
    }

    @GetMapping("/resources")
    public ResponseEntity<?> getResources(@PathVariable String realmId,
            @ModelAttribute ResourceFilterRequest resourceFilterRequest) {
        var resources = permissionService.getResources(realmId, resourceFilterRequest);
        return ResponseEntity.ok(resources);
    }

    @GetMapping("/actions")
    public ResponseEntity<?> getActions(@PathVariable String realmId,
            @ModelAttribute ResourceFilterRequest resourceFilterRequest) {
        var actions = permissionService.getActions(realmId, resourceFilterRequest);
        return ResponseEntity.ok(actions);
    }

    @PostMapping
    public ResponseEntity<?> createPermission(
            @PathVariable String realmId,
            @Valid @RequestBody Permission permission) {
        return ResponseEntity.ok(permissionService.createPermission(realmId, permission));
    }

    @PostMapping("/standard")
    public ResponseEntity<?> createStandardPermission(
            @PathVariable String realmId,
            @Valid @RequestBody StandardPermission standardPermission) {
        return ResponseEntity.ok(permissionService.createStandardPermission(realmId, standardPermission));
    }

    @PutMapping("{id}")
    public ResponseEntity<?> updatePermission(
            @PathVariable String realmId,
            @PathVariable Long id,
            @Valid @RequestBody UpdatePermissionRequest request) {
        return ResponseEntity.ok(permissionService.updatePermission(realmId, id, request));
    }

    @PatchMapping("{id}")
    public ResponseEntity<?> patchPermission(
            @PathVariable String realmId,
            @PathVariable Long id,
            @Valid @RequestBody UpdatePermissionRequest request) {
        return ResponseEntity.ok(permissionService.patchPermission(realmId, id, request));
    }
}