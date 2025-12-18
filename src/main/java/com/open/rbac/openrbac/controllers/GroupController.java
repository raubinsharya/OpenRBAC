package com.open.rbac.openrbac.controllers;

import com.open.rbac.openrbac.RequestParams.GroupFilterRequest;
import com.open.rbac.openrbac.models.Group;
import com.open.rbac.openrbac.models.Role;
import com.open.rbac.openrbac.requests.CreateGroupRequest;
import com.open.rbac.openrbac.services.GroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/realms/{realmId}/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    @GetMapping
    public ResponseEntity<?> getAllGroups(
            @PathVariable Long realmId,
            @ModelAttribute GroupFilterRequest groupFilterRequest) {

        var groups = groupService.getAllGroups(realmId, groupFilterRequest);
        return ResponseEntity.ok(groups);
    }

    @GetMapping("{id}")
    public ResponseEntity<?> getGroupById(@PathVariable Long id,
            @PathVariable Long realmId) {

        var group = groupService.getGroupById(realmId, id);
        return ResponseEntity.ok(group);
    }

    @PostMapping
    public ResponseEntity<?> createRole(
            @PathVariable Long realmId,
            @Valid @RequestBody CreateGroupRequest createGroupRequest) {
        return ResponseEntity.ok(groupService.createGroup(realmId, createGroupRequest));
    }
}