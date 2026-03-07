package com.open.rbac.openrbac.controllers;

import com.open.rbac.openrbac.requestParams.RoleFilterRequest;
import com.open.rbac.openrbac.models.Role;
import com.open.rbac.openrbac.services.RoleService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.open.rbac.openrbac.requests.UpdateRoleRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/realms/{realmId}/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    public ResponseEntity<?> getAllRoles(
            @PathVariable String realmId,
            @ModelAttribute RoleFilterRequest roleFilterRequest) {

        var roles = roleService.getAllRoles(realmId, roleFilterRequest);
        return ResponseEntity.ok(roles);
    }

    @GetMapping("{id}")
    public ResponseEntity<?> getRoleById(
            @PathVariable Long id,
            @PathVariable String realmId) {

        var role = roleService.getRoleById(id, realmId);
        return ResponseEntity.ok(role);
    }

    @PostMapping
    public ResponseEntity<?> createRole(
            @PathVariable String realmId,
            @RequestBody Role role) {
        return ResponseEntity.ok(roleService.createRole(realmId, role));
    }

    @PutMapping("{id}")
    public ResponseEntity<?> updateRole(
            @PathVariable String realmId,
            @PathVariable Long id,
            @Valid @RequestBody UpdateRoleRequest request) {
        return ResponseEntity.ok(roleService.updateRole(realmId, id, request));
    }

    @PatchMapping("{id}")
    public ResponseEntity<?> patchRole(
            @PathVariable String realmId,
            @PathVariable Long id,
            @RequestBody UpdateRoleRequest request) {
        return ResponseEntity.ok(roleService.patchRole(realmId, id, request));
    }
}