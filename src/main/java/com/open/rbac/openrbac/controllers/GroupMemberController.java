package com.open.rbac.openrbac.controllers;

import com.open.rbac.openrbac.RequestParams.UserGroupFilterRequest;
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


}