package com.open.rbac.openrbac.controllers;

import com.open.rbac.openrbac.requestParams.RoleFilterRequest;
import com.open.rbac.openrbac.models.Role;
import com.open.rbac.openrbac.services.RoleService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/realms/{realmId}/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    public ResponseEntity<?> getAllRoles(
            @PathVariable Long realmId,
            @ModelAttribute RoleFilterRequest roleFilterRequest) {

        var roles = roleService.getAllRoles(realmId, roleFilterRequest);
        return ResponseEntity.ok(roles);
    }

    @GetMapping("{id}")
    public ResponseEntity<?> getRoleById(
            @PathVariable Long id,
            @PathVariable Long realmId) {

        var role = roleService.getRoleById(id, realmId);
        return ResponseEntity.ok(role);
    }

    @PostMapping
    public ResponseEntity<?> createRole(
            @PathVariable Long realmId,
            @RequestBody Role role) {
        return ResponseEntity.ok(roleService.createRole(realmId, role));
    }
}