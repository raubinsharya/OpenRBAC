package com.open.rbac.openrbac.controllers;

import com.open.rbac.openrbac.requestParams.RoleFilterRequest;
import com.open.rbac.openrbac.requestParams.UserFilterRequest;
import com.open.rbac.openrbac.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/realm/{realmId}/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

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

    @GetMapping("{id}/roles")
    public ResponseEntity<?> getUserRoles(
            @PathVariable Long id,
            @PathVariable Long realmId,
            @ModelAttribute RoleFilterRequest filter) {
        var roles = userService.getUserRoles(id, realmId, filter);
        return ResponseEntity.ok(roles);
    }
}