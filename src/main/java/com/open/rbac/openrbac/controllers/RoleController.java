package com.open.rbac.openrbac.controllers;

import com.open.rbac.openrbac.RequestParams.RoleFilterRequest;
import com.open.rbac.openrbac.models.Role;
import com.open.rbac.openrbac.services.RoleService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/realms/{realmId}/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    public ResponseEntity<?> getAllRoles(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Boolean isSystemRole,
            @RequestParam(defaultValue = "0", required = false) int page,
            @RequestParam(defaultValue = "10", required = false) int size,
            @AuthenticationPrincipal Jwt jwt, @PathVariable String realmId) {

        var roles = roleService.getAllRoles(status, isSystemRole, page, size);
        return ResponseEntity.ok(roles);
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchRoles(@AuthenticationPrincipal Jwt jwt,
                                         @PathVariable Long realmId,
                                         @ModelAttribute RoleFilterRequest filter) {
        return ResponseEntity.ok(roleService.searchRoles(realmId, filter));
    }

    @GetMapping("{id}")
    public ResponseEntity<?> getRoleById(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt, @PathVariable String realmId) {

        var role = roleService.getRoleById(id);
        return ResponseEntity.ok(role);
    }

    @PostMapping
    public ResponseEntity<?> createRole(@PathVariable("realmId") Long id, @RequestBody Role role, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(roleService.createRole(id, role));
    }
}