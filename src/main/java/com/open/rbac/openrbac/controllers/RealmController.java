package com.open.rbac.openrbac.controllers;

import com.open.rbac.openrbac.dtos.RealmDTO;
import com.open.rbac.openrbac.services.RealmService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/realms")
@RequiredArgsConstructor
public class RealmController {

    private final RealmService realmService;

    @GetMapping
    public ResponseEntity<?> getAllRealms(@RequestParam(required = false) String status,
            @AuthenticationPrincipal Jwt jwt) {
        List<RealmDTO> realms = realmService.getAllRealms(status);
        return ResponseEntity.ok(realms);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getRealmById(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(realmService.getRealmById(id).orElse(null));
    }

    @GetMapping("/realm-id/{realmId}")
    public ResponseEntity<?> getRealmByRealmId(@PathVariable String realmId,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(realmService.getRealmByRealmId(realmId).orElse(null));
    }

}