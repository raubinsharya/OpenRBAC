package com.open.rbac.openrbac.controllers;

import com.open.rbac.openrbac.requestParams.ResourceFilterRequest;
import com.open.rbac.openrbac.requestParams.UserFilterRequest;
import com.open.rbac.openrbac.services.PermissionService;
import com.open.rbac.openrbac.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/realms/{realmId}/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final PermissionService permissionService;

    @GetMapping
    public ResponseEntity<?> getAllUsers(
            @ModelAttribute @Valid UserFilterRequest userFilterRequest,
            @PathVariable Long realmId) {
        var users = userService.getAllUsers(userFilterRequest, realmId);
        return ResponseEntity.ok(users);
    }

    @GetMapping("{id}")
    public ResponseEntity<?> getUserById(
            @PathVariable Long id,
            @PathVariable Long realmId) {
        var user = userService.getUserById(id, realmId);
        return ResponseEntity.ok(user);
    }

    @GetMapping("{id}/resources")
    public ResponseEntity<?> getUserResources(
            @PathVariable Long realmId,
            @PathVariable("id") Long userId,
            @ModelAttribute @Valid ResourceFilterRequest filter) {
        var resources = permissionService.getUserResources(realmId, userId, filter);
        return ResponseEntity.ok(resources);
    }
}