package com.open.rbac.openrbac.controllers;

import com.open.rbac.openrbac.requestParams.UserGroupFilterRequest;
import com.open.rbac.openrbac.requests.AddGroupMembersRequest;
import com.open.rbac.openrbac.services.UserGroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/realms/{realmId}/groups/{id}")
@RequiredArgsConstructor
public class GroupMemberController {

    private final UserGroupService userGroupService;

    @GetMapping("/members")
    public ResponseEntity<?> getGroupMembers(@PathVariable String realmId,
            @PathVariable Long id,
            @ModelAttribute @Valid UserGroupFilterRequest userGroupFilterRequest) {
        return ResponseEntity.ok(userGroupService.getGroupMembers(realmId, id, userGroupFilterRequest));
    }

    @PostMapping("/members")
    public ResponseEntity<?> addMembers(@PathVariable String realmId,
            @PathVariable Long id,
            @Valid @RequestBody AddGroupMembersRequest request) {
        return ResponseEntity.ok(userGroupService.addMembersToGroup(realmId, id, request));
    }

    @DeleteMapping("/members")
    public ResponseEntity<?> removeMembers(@PathVariable String realmId,
            @PathVariable Long id,
            @Valid @RequestBody com.open.rbac.openrbac.requests.RemoveGroupMembersRequest request) {
        userGroupService.removeMembersFromGroup(realmId, id, request);
        return ResponseEntity.ok(java.util.Map.of("message", "Members removed successfully"));
    }

    @PatchMapping("/members")
    public ResponseEntity<?> updateMemberExpiry(@PathVariable String realmId,
            @PathVariable Long id,
            @Valid @RequestBody com.open.rbac.openrbac.requests.UpdateGroupMembersExpiryRequest request) {
        userGroupService.updateMembersExpiry(realmId, id, request);
        return ResponseEntity.ok(Map.of("message", "Members expiry updated successfully"));
    }

    @GetMapping("/members/{userId}/check")
    public ResponseEntity<Map<String, Boolean>> checkGroupMember(@PathVariable String realmId,
            @PathVariable Long id,
            @PathVariable Long userId) {
        return ResponseEntity.ok(Map.of("isMember",
                userGroupService.checkUserGroupMembership(realmId, id, userId)));
    }
}