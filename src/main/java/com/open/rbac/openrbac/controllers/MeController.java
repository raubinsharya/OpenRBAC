package com.open.rbac.openrbac.controllers;

import com.open.rbac.openrbac.RequestParams.PermissionFilterRequest;
import com.open.rbac.openrbac.RequestParams.RoleFilterRequest;
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
    public ResponseEntity<?> getMe(@RequestParam(required = false) boolean includeRealm, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(meService.getUser(jwt.getClaim("preferred_username"), includeRealm));
    }

    @GetMapping("/roles")
    public ResponseEntity<?> getRoles(@AuthenticationPrincipal Jwt jwt, @ModelAttribute RoleFilterRequest filterRequest) {
        return ResponseEntity.ok(meService.getMeRoles(jwt.getClaim("preferred_username"), filterRequest));
    }

    @GetMapping("/permissions")
    public ResponseEntity<?> getPermissions(@AuthenticationPrincipal Jwt jwt, @ModelAttribute PermissionFilterRequest filterRequest) {
        return ResponseEntity.ok(meService.getMePermissions(jwt.getClaim("preferred_username"), filterRequest));
    }
}
