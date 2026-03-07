package com.open.rbac.openrbac.services;

import com.open.rbac.openrbac.requestParams.GroupFilterRequest;
import com.open.rbac.openrbac.annotations.RequireAllRoles;
import com.open.rbac.openrbac.dtos.GroupDTO;
import com.open.rbac.openrbac.dtos.PagedResponse;
import com.open.rbac.openrbac.models.Group;
import com.open.rbac.openrbac.models.Realm;
import com.open.rbac.openrbac.repositories.GroupRepository;
import com.open.rbac.openrbac.repositories.RealmRepository;
import com.open.rbac.openrbac.requests.CreateGroupRequest;
import com.open.rbac.openrbac.requests.UpdateGroupRequest;
import com.open.rbac.openrbac.specifications.BaseSpecification;
import com.open.rbac.openrbac.specifications.GroupSpecification;
import com.open.rbac.openrbac.specifications.RealmSpecification;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import com.open.rbac.openrbac.models.User;
import com.open.rbac.openrbac.repositories.UserRepository;
import com.open.rbac.openrbac.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final RealmRepository realmRepository;
    private final UserRepository userRepository;

    public PagedResponse<GroupDTO> getAllGroups(String realmIdentifier, GroupFilterRequest groupFilterRequest) {

        Specification<Group> specification = Specification.allOf(GroupSpecification.hasRealm(realmIdentifier))
                .and(GroupSpecification.searchByNameIgnoreCase(groupFilterRequest.getName()))
                .and(GroupSpecification.hasStatus(groupFilterRequest.getStatus()))
                .and(GroupSpecification.hasCreatedBy(groupFilterRequest.getCreatedBy()))
                .and(GroupSpecification.hasPath(groupFilterRequest.getPath()))
                .and(GroupSpecification.hasPathPrefix(groupFilterRequest.getPathPrefix()))
                .and(GroupSpecification.hasLevel(groupFilterRequest.getLevel()))
                .and(GroupSpecification.isRoot(groupFilterRequest.getIsRoot()))
                .and(GroupSpecification.hasParentGroup(groupFilterRequest.getParentGroupId()))
                .and(BaseSpecification.withBaseFilters(groupFilterRequest))
                .and(GroupSpecification.fetchWithCreatedBy());

        var groups = groupRepository.findAll(specification, groupFilterRequest.toPageable());
        return PagedResponse.fromPage(groups, GroupDTO::from);
    }

    @RequireAllRoles(value = { "realm-admin" })
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = { "groups", "group_hierarchies" }, allEntries = true)
    public Group createGroup(String realmIdentifier, CreateGroupRequest createGroupRequest) {
        Specification<Realm> specification = RealmSpecification.hasIdOrName(realmIdentifier);
        var realm = realmRepository.findOne(specification)
                .orElseThrow(() -> new IllegalArgumentException("Realm id " + realmIdentifier + " not found"));

        Group parentGroup = null;
        String path = "/";
        var level = 0;

        if (createGroupRequest.parentGroupId() != null) {
            parentGroup = groupRepository.findByIdAndRealm(createGroupRequest.parentGroupId(), realm)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Parent group id " + createGroupRequest.parentGroupId() + " not found"));
            level = parentGroup.getLevel() + 1;
            path = parentGroup.generatePathForChild();
        }

        final User createdBy = SecurityUtils.getAuthenticatedUser(jwt -> {
            String sub = jwt.getSubject(); // This is the keycloak_user_id
            if (sub != null) {
                return userRepository.findByKeycloakUserId(sub).orElse(null);
            }
            return null;
        });

        // Ensure createdBy is not null if we want to enforce it, but for now we can let
        // it be null or throw.
        // Usually system actions imply null createdBy, but this is an API call.

        Group group = Group.builder()
                .realm(realm)
                .name(createGroupRequest.name())
                .parentGroup(parentGroup)
                .description(createGroupRequest.description())
                .status(createGroupRequest.status())
                .level(level)
                .path(path)
                .createdBy(createdBy)
                .build();
        return groupRepository.save(group);
    }

    @Cacheable(value = "groups", key = "#realmIdentifier + '-' + #id")
    public GroupDTO getGroupById(String realmIdentifier, Long id) {
        return GroupDTO.from(getGroupOrThrow(realmIdentifier, id));
    }

    @RequireAllRoles(value = { "realm-admin" })
    @Transactional
    @CacheEvict(value = { "groups", "group_hierarchies" }, allEntries = true)
    public GroupDTO updateGroup(String realmIdentifier, Long id, UpdateGroupRequest updateData) {
        Group existing = getGroupOrThrow(realmIdentifier, id);

        existing.setName(updateData.name());
        existing.setDescription(updateData.description());
        if (updateData.status() != null) {
            existing.setStatus(updateData.status());
        }

        Group saved = groupRepository.save(existing);
        return GroupDTO.from(saved);
    }

    @RequireAllRoles(value = { "realm-admin" })
    @Transactional
    @CacheEvict(value = { "groups", "group_hierarchies" }, allEntries = true)
    public GroupDTO patchGroup(String realmIdentifier, Long id, UpdateGroupRequest patchData) {
        Group existing = getGroupOrThrow(realmIdentifier, id);

        Optional.ofNullable(patchData.name()).ifPresent(existing::setName);
        Optional.ofNullable(patchData.description()).ifPresent(existing::setDescription);
        Optional.ofNullable(patchData.status()).ifPresent(existing::setStatus);

        Group saved = groupRepository.save(existing);
        return GroupDTO.from(saved);
    }

    private Group getGroupOrThrow(String realmIdentifier, Long id) {
        Specification<Group> specification = GroupSpecification.hasRealm(realmIdentifier)
                .and(GroupSpecification.hasId(id));
        return groupRepository.findOne(specification)
                .orElseThrow(() -> new EntityNotFoundException("Group not found with id: " + id));
    }

    @Cacheable(value = "group_hierarchies", key = "#realmIdentifier + '-' + #groupId")
    public GroupDTO getHierarchy(String realmIdentifier, Long groupId) {
        // Fetch specific group, its ancestors, and all its descendants in one query
        List<Group> hierarchy = groupRepository.findGroupHierarchy(realmIdentifier, groupId);

        if (hierarchy.isEmpty()) {
            throw new IllegalArgumentException("Group not found");
        }

        // Map of ID -> Fully loaded Group Entity (to avoid using lazy proxies)
        Map<Long, Group> groupMap = hierarchy.stream()
                .collect(Collectors.toMap(Group::getId, g -> g, (a, b) -> a));

        // The requested node
        Group requestedNode = groupMap.get(groupId);
        if (requestedNode == null) {
            throw new IllegalArgumentException("Group not found in hierarchy");
        }

        // MAP: ParentID -> List<Children> (for building descendants tree)
        Map<Long, List<Group>> childrenMap = hierarchy.stream()
                .filter(g -> g.getParentGroup() != null)
                .collect(Collectors.groupingBy(g -> g.getParentGroup().getId()));

        // 1. Build Descendants Tree (Children)
        GroupDTO resultDTO = buildDescendantsTree(requestedNode, childrenMap);

        // 2. Build Ancestor Chain (Parents) and attach to result
        resultDTO = attachAncestors(resultDTO, requestedNode, groupMap);

        return resultDTO;
    }

    private GroupDTO buildDescendantsTree(Group current, Map<Long, List<Group>> childrenMap) {
        var myChildren = childrenMap.getOrDefault(current.getId(), Collections.emptyList());

        var childrenDTOs = myChildren.stream()
                .map(child -> buildDescendantsTree(child, childrenMap))
                .toList();

        return GroupDTO.from(current, childrenDTOs.isEmpty() ? null : childrenDTOs, null);
    }

    private GroupDTO attachAncestors(GroupDTO currentDTO, Group currentNode, Map<Long, Group> groupMap) {
        if (currentNode.getParentGroup() == null) {
            return currentDTO;
        }

        // Find the full entity of the parent from our pre-fetched map
        Group parentGroup = groupMap.get(currentNode.getParentGroup().getId());
        if (parentGroup == null) {
            // Parent exists in DB but wasn't fetched in hierarchy query?
            // This happens if query logic is flawed or root reached.
            return currentDTO;
        }

        // Recursively build parent DTO (without its children, to avoid massive
        // duplication/cycles)
        // We only want the chain upwards.
        GroupDTO parentDTO = GroupDTO.from(parentGroup, null, null);

        // Recursively attach ITS parent
        parentDTO = attachAncestors(parentDTO, parentGroup, groupMap);

        // Return the current DTO with this new parent attached
        return new GroupDTO(
                currentDTO.id(),
                currentDTO.name(),
                currentDTO.description(),
                currentDTO.path(),
                currentDTO.parentGroupId(),
                currentDTO.createdAt(),
                currentDTO.updatedAt(),
                currentDTO.status(),
                currentDTO.children(),
                parentDTO, currentDTO.ancestors(), currentDTO.createdBy());
    }
}