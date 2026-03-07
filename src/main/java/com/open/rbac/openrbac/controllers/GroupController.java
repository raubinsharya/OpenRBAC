package com.open.rbac.openrbac.controllers;

import com.open.rbac.openrbac.requestParams.GroupFilterRequest;
import com.open.rbac.openrbac.requests.CreateGroupRequest;
import com.open.rbac.openrbac.requests.UpdateGroupRequest;
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
            @PathVariable String realmId,
            @ModelAttribute GroupFilterRequest groupFilterRequest) {

        var groups = groupService.getAllGroups(realmId, groupFilterRequest);
        return ResponseEntity.ok(groups);
    }

    @GetMapping("{id}")
    public ResponseEntity<?> getGroupById(@PathVariable Long id,
            @PathVariable String realmId) {

        var group = groupService.getGroupById(realmId, id);
        return ResponseEntity.ok(group);
    }

    @PostMapping
    public ResponseEntity<?> createGroup(
            @PathVariable String realmId,
            @Valid @RequestBody CreateGroupRequest createGroupRequest) {
        return ResponseEntity.ok(groupService.createGroup(realmId, createGroupRequest));
    }

    @GetMapping("/{id}/hierarchy")
    public ResponseEntity<?> getHierarchy(
            @PathVariable String realmId,
            @PathVariable Long id) {
        return ResponseEntity.ok(groupService.getHierarchy(realmId, id));
    }

    @PutMapping("{id}")
    public ResponseEntity<?> updateGroup(
            @PathVariable String realmId,
            @PathVariable Long id,
            @Valid @RequestBody UpdateGroupRequest request) {
        return ResponseEntity.ok(groupService.updateGroup(realmId, id, request));
    }

    @PatchMapping("{id}")
    public ResponseEntity<?> patchGroup(
            @PathVariable String realmId,
            @PathVariable Long id,
            @RequestBody UpdateGroupRequest request) {
        return ResponseEntity.ok(groupService.patchGroup(realmId, id, request));
    }
}