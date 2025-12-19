package com.open.rbac.openrbac.services;

import com.open.rbac.openrbac.RequestParams.UserGroupFilterRequest;
import com.open.rbac.openrbac.dtos.PagedResponse;
import com.open.rbac.openrbac.dtos.UserGroupDTO;
import com.open.rbac.openrbac.models.UserGroup;
import com.open.rbac.openrbac.repositories.UserGroupRepository;
import com.open.rbac.openrbac.specifications.BaseSpecification;
import com.open.rbac.openrbac.specifications.GroupMemberSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserGroupService {

    private final UserGroupRepository userGroupRepository;

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
                .and(GroupMemberSpecification.groupMemberExpiryAfter(filter.getGroupMemberExpiryAfter()));

        return PagedResponse.fromPage(userGroupRepository.findAll(spec, filter.toPageable()), UserGroupDTO::from);
    }
}
