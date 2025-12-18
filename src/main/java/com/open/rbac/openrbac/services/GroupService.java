package com.open.rbac.openrbac.services;

import com.open.rbac.openrbac.RequestParams.GroupFilterRequest;
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

    @RequireAnyRole(value = { "realm-admin" })
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
}