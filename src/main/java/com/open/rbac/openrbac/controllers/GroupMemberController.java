package com.open.rbac.openrbac.controllers;

import com.open.rbac.openrbac.requestParams.UserGroupFilterRequest;
import com.open.rbac.openrbac.requests.AddGroupMembersRequest;
import com.open.rbac.openrbac.services.UserGroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/realms/{realmId}/groups/{id}")
@RequiredArgsConstructor
public class GroupMemberController {

    private final UserGroupService userGroupService;

    @GetMapping("/members")
    public ResponseEntity<?> getGroupMembers(@PathVariable Long realmId,
            @PathVariable Long id,
            @ModelAttribute @Valid UserGroupFilterRequest userGroupFilterRequest) {
        return ResponseEntity.ok(userGroupService.getGroupMembers(realmId, id, userGroupFilterRequest));
    }

    @PostMapping("/members")
    public ResponseEntity<?> addMembers(@PathVariable Long realmId,
            @PathVariable Long id,
            @Valid @RequestBody AddGroupMembersRequest request) {
        return ResponseEntity.ok(userGroupService.addMembersToGroup(realmId, id, request));
    }

}