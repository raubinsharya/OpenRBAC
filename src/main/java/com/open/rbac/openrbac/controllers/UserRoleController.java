package com.open.rbac.openrbac.controllers;

import com.open.rbac.openrbac.requestParams.UserRoleFilterRequest;
import com.open.rbac.openrbac.requests.AddUserRolesRequest;
import com.open.rbac.openrbac.requests.RemoveUserRolesRequest;
import com.open.rbac.openrbac.services.UserRoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/realms/{realmId}/users/{userId}/roles")
@RequiredArgsConstructor
public class UserRoleController {

    private final UserRoleService userRoleService;

    @GetMapping
    public ResponseEntity<?> getUserRoles(
            @PathVariable Long realmId,
            @PathVariable Long userId,
            @ModelAttribute @Valid UserRoleFilterRequest filter) {
        return ResponseEntity.ok(userRoleService.getUserRoles(realmId, userId, filter));
    }

    @PostMapping
    public ResponseEntity<?> addRoles(
            @PathVariable Long realmId,
            @PathVariable Long userId,
            @RequestBody @Valid AddUserRolesRequest request) {
        userRoleService.addRolesToUser(realmId, userId, request);
        return ResponseEntity.ok(Map.of("message", "Roles added successfully"));
    }

    @DeleteMapping
    public ResponseEntity<?> removeRoles(
            @PathVariable Long realmId,
            @PathVariable Long userId,
            @RequestBody @Valid RemoveUserRolesRequest request) {
        userRoleService.removeRolesFromUser(realmId, userId, request);
        return ResponseEntity.ok(Map.of("message", "Roles removed successfully"));
    }
}
