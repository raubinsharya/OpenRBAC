package com.open.rbac.openrbac.services;

import com.open.rbac.openrbac.requestParams.GroupFilterRequest;
import com.open.rbac.openrbac.annotations.RequireAllRoles;
import com.open.rbac.openrbac.annotations.RequireAnyRole;
import com.open.rbac.openrbac.dtos.GroupDTO;
import com.open.rbac.openrbac.dtos.PagedResponse;
import com.open.rbac.openrbac.models.Group;
import com.open.rbac.openrbac.repositories.GroupRepository;
import com.open.rbac.openrbac.repositories.RealmRepository;
import com.open.rbac.openrbac.requests.CreateGroupRequest;
import com.open.rbac.openrbac.specifications.BaseSpecification;
import com.open.rbac.openrbac.specifications.GroupSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class GroupService {

    private final GroupRepository groupRepository;
    private final RealmRepository realmRepository;

    @Transactional(readOnly = true)
    public PagedResponse<GroupDTO> getAllGroups(Long realmId, GroupFilterRequest groupFilterRequest) {
        Specification<Group> specification = Specification.allOf(GroupSpecification.hasRealm(realmId))
                .and(GroupSpecification.searchByNameIgnoreCase(groupFilterRequest.getName()))
                .and(GroupSpecification.hasStatus(groupFilterRequest.getStatus()))
                .and(BaseSpecification.withBaseFilters(groupFilterRequest));

        var groups = groupRepository.findAll(specification, groupFilterRequest.toPageable());
        return PagedResponse.fromPage(groups, GroupDTO::from);
    }

    @RequireAllRoles(value = { "realm-admin", "developer" })
    @Transactional(rollbackFor = Exception.class)
    public Group createGroup(long realmId, CreateGroupRequest createGroupRequest) {
        var realm = realmRepository.findById(realmId)
                .orElseThrow(() -> new IllegalArgumentException("Realm id " + realmId + " not found"));

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

        Group group = Group.builder()
                .realm(realm)
                .name(createGroupRequest.name())
                .parentGroup(parentGroup)
                .description(createGroupRequest.description())
                .status(createGroupRequest.status())
                .level(level)
                .path(path)
                .build();
        return groupRepository.save(group);
    }

    public GroupDTO getGroupById(Long realmId, Long id) {
        Specification<Group> specification = GroupSpecification.hasRealm(realmId).and(GroupSpecification.hasId(id));
        return groupRepository.findOne(specification).stream().map(GroupDTO::from).findFirst().orElse(null);
    }

    public GroupDTO getHierarchy(Long realmId, Long groupId) {
        // Fetch specific group, its ancestors, and all its descendants in one query
        List<Group> hierarchy = groupRepository.findGroupHierarchy(realmId, groupId);

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
                parentDTO, currentDTO.ancestors());
    }
}