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
    public ResponseEntity<?> getRealmById(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(realmService.getRealmById(id));
    }

    @GetMapping("/realm-id/{realmId}")
    public ResponseEntity<?> getRealmByRealmId(@PathVariable String realmId,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(realmService.getRealmByRealmId(realmId));
    }

}