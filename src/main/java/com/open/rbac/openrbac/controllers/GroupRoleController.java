package com.open.rbac.openrbac.controllers;

import com.open.rbac.openrbac.requestParams.GroupRoleFilterRequest;
import com.open.rbac.openrbac.requests.AddGroupRolesRequest;
import com.open.rbac.openrbac.requests.RemoveGroupRolesRequest;
import com.open.rbac.openrbac.services.GroupRoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/realms/{realmId}/groups/{groupId}/roles")
@RequiredArgsConstructor
public class GroupRoleController {

    private final GroupRoleService groupRoleService;

    @GetMapping
    public ResponseEntity<?> getGroupRoles(
            @PathVariable String realmId,
            @PathVariable Long groupId,
            @ModelAttribute @Valid GroupRoleFilterRequest filter) {
        return ResponseEntity.ok(groupRoleService.getGroupRoles(realmId, groupId, filter));
    }

    @GetMapping("/check")
    public ResponseEntity<Map<String, Boolean>> checkGroupRole(
            @PathVariable String realmId,
            @PathVariable Long groupId,
            @RequestParam(required = false) Long roleId,
            @RequestParam(required = false) String roleName) {
        boolean hasRole = groupRoleService.hasRole(realmId, groupId, roleId, roleName);
        return ResponseEntity.ok(Map.of("hasRole", hasRole));
    }

    @PostMapping
    public ResponseEntity<?> addRoles(
            @PathVariable String realmId,
            @PathVariable Long groupId,
            @RequestBody @Valid AddGroupRolesRequest request) {
        groupRoleService.addRolesToGroup(realmId, groupId, request);
        return ResponseEntity.ok(Map.of("message", "Roles added successfully"));
    }

    @DeleteMapping
    public ResponseEntity<?> removeRoles(
            @PathVariable String realmId,
            @PathVariable Long groupId,
            @RequestBody @Valid RemoveGroupRolesRequest request) {
        groupRoleService.removeRolesFromGroup(realmId, groupId, request);
        return ResponseEntity.ok(Map.of("message", "Roles removed successfully"));
    }
}
