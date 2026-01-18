package com.open.rbac.openrbac.controllers;

import com.open.rbac.openrbac.dtos.GroupPermissionDTO;
import com.open.rbac.openrbac.dtos.PagedResponse;
import com.open.rbac.openrbac.requestParams.GroupPermissionFilterRequest;
import com.open.rbac.openrbac.requests.AddGroupPermissionsRequest;
import com.open.rbac.openrbac.requests.RemoveGroupPermissionsRequest;
import com.open.rbac.openrbac.services.GroupPermissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/realms/{realmId}/groups/{groupId}/permissions")
@RequiredArgsConstructor
public class GroupPermissionController {

    private final GroupPermissionService groupPermissionService;

    @PostMapping
    public ResponseEntity<?> addPermissions(
            @PathVariable Long realmId,
            @PathVariable Long groupId,
            @Valid @RequestBody AddGroupPermissionsRequest request) {
        groupPermissionService.addPermissionsToGroup(realmId, groupId, request);
        return ResponseEntity.ok(java.util.Map.of("message", "Permissions added successfully"));
    }

    @DeleteMapping
    public ResponseEntity<?> removePermissions(
            @PathVariable Long realmId,
            @PathVariable Long groupId,
            @Valid @RequestBody RemoveGroupPermissionsRequest request) {
        groupPermissionService.removePermissionsFromGroup(realmId, groupId, request);
        return ResponseEntity.ok(java.util.Map.of("message", "Permissions removed successfully"));
    }

    @GetMapping
    public ResponseEntity<?> getGroupPermissions(
            @PathVariable Long realmId,
            @PathVariable Long groupId,
            @ModelAttribute @Valid GroupPermissionFilterRequest filter) {
        return ResponseEntity.ok(groupPermissionService.getGroupPermissions(realmId, groupId, filter));
    }

    @GetMapping("/check")
    public ResponseEntity<Map<String, Boolean>> checkGroupPermission(
            @PathVariable Long realmId,
            @PathVariable Long groupId,
            @RequestParam(required = false) Long permissionId,
            @RequestParam(required = false) String permissionName) {
        boolean hasPermission = groupPermissionService.hasPermission(realmId, groupId, permissionId, permissionName);
        return ResponseEntity.ok(Map.of("hasPermission", hasPermission));
    }
}
