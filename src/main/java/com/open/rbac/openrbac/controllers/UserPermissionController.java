package com.open.rbac.openrbac.controllers;

import com.open.rbac.openrbac.requestParams.CheckPermissionRequest;
import com.open.rbac.openrbac.requestParams.UserPermissionFilterRequest;
import com.open.rbac.openrbac.requests.AddUserPermissionsRequest;
import com.open.rbac.openrbac.requests.RemoveUserPermissionsRequest;
import com.open.rbac.openrbac.services.UserPermissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/realms/{realmId}/users/{userId}/permissions")
@RequiredArgsConstructor
public class UserPermissionController {

    private final UserPermissionService userPermissionService;

    @GetMapping
    public ResponseEntity<?> getUserPermissions(
            @PathVariable Long realmId,
            @PathVariable Long userId,
            @ModelAttribute @Valid UserPermissionFilterRequest filter) {
        return ResponseEntity.ok(userPermissionService.getUserPermissions(realmId, userId, filter));
    }

    @GetMapping("/check")
    public ResponseEntity<?> checkPermission(
            @PathVariable Long realmId,
            @PathVariable Long userId,
            @ModelAttribute CheckPermissionRequest request) {
        boolean hasPermission = userPermissionService.checkPermission(realmId, userId, request);
        return ResponseEntity.ok(Map.of("hasPermission", hasPermission));
    }

    @PostMapping
    public ResponseEntity<?> addPermissions(
            @PathVariable Long realmId,
            @PathVariable Long userId,
            @RequestBody @Valid AddUserPermissionsRequest request) {
        userPermissionService.addPermissionsToUser(realmId, userId, request);
        return ResponseEntity.ok(Map.of("message", "Permissions added successfully"));
    }

    @DeleteMapping
    public ResponseEntity<?> removePermissions(
            @PathVariable Long realmId,
            @PathVariable Long userId,
            @RequestBody @Valid RemoveUserPermissionsRequest request) {
        userPermissionService.removePermissionsFromUser(realmId, userId, request);
        return ResponseEntity.ok(Map.of("message", "Permissions removed successfully"));
    }

    @GetMapping("/resources")
    public ResponseEntity<?> getEffectiveUserResources(
            @PathVariable Long realmId,
            @PathVariable Long userId,
            @RequestParam(required = false) String assignmentType,
            Pageable pageable) {
        return ResponseEntity
                .ok(userPermissionService.getEffectiveUserResources(realmId, userId, pageable, assignmentType));
    }

    @GetMapping("/actions")
    public ResponseEntity<?> getEffectiveUserActions(
            @PathVariable Long realmId,
            @PathVariable Long userId,
            @RequestParam(required = false) String assignmentType,
            Pageable pageable) {
        return ResponseEntity
                .ok(userPermissionService.getEffectiveUserActions(realmId, userId, pageable, assignmentType));
    }
}
