package com.open.rbac.openrbac.controllers;

import com.open.rbac.openrbac.requestParams.PermissionFilterRequest;
import com.open.rbac.openrbac.requestParams.RoleFilterRequest;
import com.open.rbac.openrbac.services.MeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/me")
public class MeController {
    private final MeService meService;

    @GetMapping
    public ResponseEntity<?> getMe(@RequestParam(required = false) boolean includeRealm,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(meService.getUser(jwt.getSubject(), includeRealm));
    }

    @GetMapping("/roles")
    public ResponseEntity<?> getRoles(@AuthenticationPrincipal Jwt jwt,
            @ModelAttribute RoleFilterRequest filterRequest) {
        return ResponseEntity.ok(meService.getMeRoles(jwt.getSubject(), filterRequest));
    }

    @GetMapping("/permissions")
    public ResponseEntity<?> getPermissions(@AuthenticationPrincipal Jwt jwt,
            @ModelAttribute PermissionFilterRequest filterRequest) {
        return ResponseEntity.ok(meService.getMePermissions(jwt.getSubject(), filterRequest));
    }
}
