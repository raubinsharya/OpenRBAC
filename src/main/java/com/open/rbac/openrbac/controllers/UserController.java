package com.open.rbac.openrbac.controllers;

import com.open.rbac.openrbac.dtos.UserDTO;
import com.open.rbac.openrbac.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<?> getAllUsers(@AuthenticationPrincipal Jwt jwt) {
        List<UserDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }
}