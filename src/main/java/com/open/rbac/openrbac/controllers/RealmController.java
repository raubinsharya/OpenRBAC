package com.open.rbac.openrbac.controllers;

import com.open.rbac.openrbac.services.RealmService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/realms")
@RequiredArgsConstructor
public class RealmController {

    private final RealmService realmService;

    @GetMapping
    public ResponseEntity<?> getAllRealms(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0", required = false) int page,
            @RequestParam(defaultValue = "10", required = false) int size,
            @AuthenticationPrincipal Jwt jwt) {
        var realms = realmService.getAllRealms(status, page, size);
        return ResponseEntity.ok(realms);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getRealmById(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id,
            @RequestParam(required = false) boolean includeUsers,
            @RequestParam(required = false) boolean includeRoles,
            @RequestParam(required = false) boolean includePermissions) {
        return ResponseEntity.ok(realmService.getRealmById(id, includeUsers, includeRoles, includePermissions));
    }

    @GetMapping("/realm-id/{realmId}")
    public ResponseEntity<?> getRealmByRealmId(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String realmId,
            @RequestParam(required = false) boolean includeUsers,
            @RequestParam(required = false) boolean includeRoles,
            @RequestParam(required = false) boolean includePermissions) {
        return ResponseEntity.ok(realmService.getRealmByRealmId(realmId, includeUsers, includeRoles, includePermissions));
    }

}