package com.open.rbac.openrbac.services;

import com.open.rbac.openrbac.requestParams.UserGroupFilterRequest;
import com.open.rbac.openrbac.dtos.PagedResponse;
import com.open.rbac.openrbac.dtos.UserGroupDTO;
import com.open.rbac.openrbac.models.UserGroup;
import com.open.rbac.openrbac.repositories.UserGroupRepository;
import com.open.rbac.openrbac.requests.RemoveGroupMembersRequest;
import com.open.rbac.openrbac.requests.UpdateGroupMembersExpiryRequest;
import com.open.rbac.openrbac.specifications.BaseSpecification;
import com.open.rbac.openrbac.specifications.GroupMemberSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import com.open.rbac.openrbac.repositories.GroupRepository;
import com.open.rbac.openrbac.repositories.UserRepository;
import com.open.rbac.openrbac.requests.AddGroupMembersRequest;
import jakarta.persistence.EntityNotFoundException;
import com.open.rbac.openrbac.models.Group;
import com.open.rbac.openrbac.models.User;
import jakarta.transaction.Transactional;

import java.util.List;

import java.util.stream.Collectors;

import com.open.rbac.openrbac.utils.SecurityUtils;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserGroupService {

    private final UserGroupRepository userGroupRepository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;

    public PagedResponse<UserGroupDTO> getGroupMembers(Long realmId, Long id, UserGroupFilterRequest filter) {
        Specification<UserGroup> spec = GroupMemberSpecification.ofGroup(id, realmId)
                .and(BaseSpecification.withBaseFilters(filter))
                .and(GroupMemberSpecification.hasKeycloakUserId(filter.getKeycloakUserId()))
                .and(GroupMemberSpecification.hasDisplayName(filter.getDisplayName()))
                .and(GroupMemberSpecification.hasEmail(filter.getEmail()))
                .and(GroupMemberSpecification.assignedBy(filter.getAssignedBy()))
                .and(GroupMemberSpecification.hasStatus(filter.getStatus()))
                .and(GroupMemberSpecification.hasGroupStatus(filter.getGroupStatus()))
                .and(GroupMemberSpecification.assignedAtBefore(filter.getAssignedAtBefore()))
                .and(GroupMemberSpecification.assignedAtAfter(filter.getAssignedAtAfter()))
                .and(GroupMemberSpecification.groupMemberExpiryBefore(filter.getGroupMemberExpiryBefore()))
                .and(GroupMemberSpecification.groupMemberExpiryAfter(filter.getGroupMemberExpiryAfter()))
                .and(GroupMemberSpecification.isGroupMembershipExpired(filter.getIsGroupMembershipExpired()))
                .and(GroupMemberSpecification.isGroupMembershipValid(filter.getIsGroupMembershipValid()));

        return PagedResponse.fromPage(userGroupRepository.findAll(spec, filter.toPageable()), UserGroupDTO::from);
    }

    @Transactional
    public List<UserGroupDTO> addMembersToGroup(Long realmId, Long groupId, AddGroupMembersRequest request) {
        Group group = groupRepository.findByIdAndRealm_Id(groupId, realmId)
                .orElseThrow(() -> new EntityNotFoundException("Group not found"));

        List<User> users = userRepository.findAllByIdInAndRealm_Id(request.getUserId(), realmId).orElse(List.of());
        var requestedUserIds = users.stream().map(User::getId).toList();
        if (users.size() != request.getUserId().size()) {
            var notFoundUsers = request.getUserId().stream().filter(userId -> !requestedUserIds.contains(userId))
                    .toList();
            throw new EntityNotFoundException("User with ids " + notFoundUsers + " not found");
        }

        List<Long> existingMemberIds = userGroupRepository.findExistingMemberIds(groupId, requestedUserIds);
        if (!existingMemberIds.isEmpty()) {
            throw new EntityNotFoundException("User with ids " + existingMemberIds + " already have membership");
        }

        // Get current user (assignedBy)
        final User assignedBy = SecurityUtils.getAuthenticatedUser(jwt -> {
            String username = jwt.getClaimAsString("preferred_username");
            if (username != null) {
                return userRepository.findByUsername(username).orElse(null);
            }
            return null;
        });

        List<UserGroup> userGroups = users.stream()
                .map(user -> UserGroup.builder()
                        .user(user)
                        .group(group)
                        .assignedBy(assignedBy)
                        .expiryDate(request.getExpiryDate())
                        .isActive(true)
                        .build())
                .toList();

        return userGroupRepository.saveAll(userGroups).stream()
                .map(UserGroupDTO::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public void removeMembersFromGroup(Long realmId, Long groupId, RemoveGroupMembersRequest request) {
        if (!groupRepository.existsByIdAndRealm_Id(groupId, realmId)) {
            throw new EntityNotFoundException("Group not found");
        }

        List<Long> actualMemberIds = userGroupRepository.findExistingMemberIds(groupId, request.getUserId());
        if (actualMemberIds.size() != request.getUserId().size()) {
            List<Long> notFoundMembers = request.getUserId().stream()
                    .filter(id -> !actualMemberIds.contains(id))
                    .toList();
            throw new EntityNotFoundException("Following users are not members of this group: " + notFoundMembers);
        }

        userGroupRepository.removeMembers(groupId, request.getUserId());
    }

    @Transactional
    public void updateMembersExpiry(Long realmId, Long groupId, UpdateGroupMembersExpiryRequest request) {
        if (!groupRepository.existsByIdAndRealm_Id(groupId, realmId)) {
            throw new EntityNotFoundException("Group not found");
        }
        var userGroups = userGroupRepository.findAllByIdIn(request.getGroupMemberIds());
        if (userGroups.size() != request.getGroupMemberIds().size()) {
            var foundMembers = userGroups.stream().map(UserGroup::getId).collect(Collectors.toSet());
            request.getGroupMemberIds().removeAll(foundMembers);
            throw new EntityNotFoundException(
                    "Following users are not members of this group: " + request.getGroupMemberIds());
        }
        userGroupRepository.updateExpiryDate(groupId, request.getGroupMemberIds(), request.getExpiryDate());
    }

    public boolean checkUserGroupMembership(Long realmId, Long groupId, Long userId) {
        return userGroupRepository.existsByUserIdAndGroup_IdAndGroup_Realm_IdAndUser_Realm_Id(userId, groupId, realmId,
                realmId);
    }
}
