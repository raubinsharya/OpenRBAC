package com.open.rbac.openrbac.controllers;

import com.open.rbac.openrbac.RequestParams.UserFilterRequest;
import com.open.rbac.openrbac.dtos.UserDTO;
import com.open.rbac.openrbac.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/realm/{realmId}/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<?> getAllUsers(
            @ModelAttribute @Valid UserFilterRequest userFilterRequest,
            @AuthenticationPrincipal Jwt jwt, @PathVariable String realmId) {
        var users = userService.getAllUsers(userFilterRequest);
        return ResponseEntity.ok(users);
    }

    @GetMapping("{id}")
    public ResponseEntity<?> getUserById(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt, @PathVariable String realmId) {
        var user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }
}