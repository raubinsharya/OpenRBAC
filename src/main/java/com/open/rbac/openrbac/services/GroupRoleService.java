package com.open.rbac.openrbac.services;

import com.open.rbac.openrbac.annotations.RequireAnyRole;
import com.open.rbac.openrbac.dtos.GroupRoleDTO;
import com.open.rbac.openrbac.dtos.PagedResponse;
import com.open.rbac.openrbac.models.Group;
import com.open.rbac.openrbac.models.GroupRole;
import com.open.rbac.openrbac.models.Role;
import com.open.rbac.openrbac.models.User;
import com.open.rbac.openrbac.repositories.GroupRepository;
import com.open.rbac.openrbac.repositories.GroupRoleRepository;
import com.open.rbac.openrbac.repositories.RoleRepository;
import com.open.rbac.openrbac.repositories.UserRepository;
import com.open.rbac.openrbac.requests.AddGroupRolesRequest;
import com.open.rbac.openrbac.requests.RemoveGroupRolesRequest;
import com.open.rbac.openrbac.requestParams.GroupRoleFilterRequest;
import com.open.rbac.openrbac.specifications.GroupRoleSpecification;
import com.open.rbac.openrbac.specifications.GroupSpecification;
import com.open.rbac.openrbac.utils.SecurityUtils;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class GroupRoleService {

    private final GroupRepository groupRepository;
    private final RoleRepository roleRepository;
    private final GroupRoleRepository groupRoleRepository;
    private final UserRepository userRepository;

    @Transactional
    @RequireAnyRole(value = { "realm-admin", "group-admin" })
    public void addRolesToGroup(Long realmId, Long groupId, AddGroupRolesRequest request) {
        Group group = groupRepository.findOne(Specification.allOf(
                GroupSpecification.hasId(groupId),
                GroupSpecification.hasRealm(realmId)))
                .orElseThrow(() -> new EntityNotFoundException("Group not found"));

        final List<Role> validRoles = roleRepository
                .findAllByIdInAndRealm_Id(Objects.requireNonNull(request.getRoleIds()), realmId);

        List<Long> validRoleIds = validRoles.stream().map(Role::getId).toList();
        List<Long> notFoundRoleIds = request.getRoleIds().stream()
                .filter(id -> !validRoleIds.contains(id))
                .toList();

        if (!notFoundRoleIds.isEmpty()) {
            throw new EntityNotFoundException("Roles not found: " + notFoundRoleIds);
        }

        List<Long> existingRoleIds = groupRoleRepository.findExistingRoleIds(groupId, request.getRoleIds());
        if (!existingRoleIds.isEmpty()) {
            throw new IllegalArgumentException("Group already has roles " + existingRoleIds);
        }

        final User assignedBy = SecurityUtils.getAuthenticatedUser(jwt -> {
            String username = jwt.getClaimAsString("preferred_username");
            if (username != null) {
                return userRepository.findByUsername(username).orElse(null);
            }
            return null;
        });

        List<GroupRole> groupRoles = validRoles.stream().map(r -> GroupRole.builder()
                .group(group)
                .role(r)
                .assignedBy(assignedBy)
                .createdAt(java.time.LocalDateTime.now())
                .expiryDate(request.getExpiryDate())
                .isActive(true)
                .isInherited(false)
                .sourceGroup(null)
                .allowInheritance(request.getAllowInheritance())
                .maxInheritanceDepth(request.getMaxInheritanceDepth() != null ? request.getMaxInheritanceDepth() : 0)
                .build()).toList();

        groupRoleRepository.saveAll(groupRoles);
    }

    @Transactional
    @RequireAnyRole(value = { "realm-admin", "group-admin" })
    public void removeRolesFromGroup(Long realmId, Long groupId, RemoveGroupRolesRequest request) {
        boolean groupExists = groupRepository.exists(Specification.allOf(
                GroupSpecification.hasId(groupId),
                GroupSpecification.hasRealm(realmId)));
        if (!groupExists) {
            throw new EntityNotFoundException("Group not found");
        }

        List<Long> existingRoleIds = groupRoleRepository.findExistingRoleIds(groupId, request.getRoleIds());
        if (existingRoleIds.size() != request.getRoleIds().size()) {
            request.getRoleIds().removeAll(new HashSet<>(existingRoleIds));
            throw new EntityNotFoundException("Roles are not assigned to this group: " + request.getRoleIds());
        }

        // Potential issue: Deleting inherited roles? For now assume valid deletion of
        // whatever is found matching roleId
        groupRoleRepository.deleteByGroupIdAndRoleIdIn(groupId, existingRoleIds);
    }

    @Transactional(readOnly = true)
    public PagedResponse<GroupRoleDTO> getGroupRoles(Long realmId, Long groupId, GroupRoleFilterRequest filter) {
        boolean groupExists = groupRepository.exists(Specification.allOf(
                GroupSpecification.hasId(groupId),
                GroupSpecification.hasRealm(realmId)));
        if (!groupExists) {
            throw new EntityNotFoundException("Group not found");
        }

        Specification<GroupRole> spec = Specification.allOf(
                GroupRoleSpecification.ofGroup(groupId, realmId),
                GroupRoleSpecification.isNotExpired(),
                GroupRoleSpecification.hasRoleName(filter.getRoleName()),
                GroupRoleSpecification.hasRoleStatus(filter.getRoleStatus()),
                GroupRoleSpecification.hasGroupStatus(filter.getGroupStatus()),
                GroupRoleSpecification.assignedBy(filter.getAssignedBy()),
                GroupRoleSpecification.isActive(filter.getIsActive()),
                GroupRoleSpecification.isInherited(filter.getIsInherited()),
                GroupRoleSpecification.assignedAtBefore(filter.getAssignedAtBefore()),
                GroupRoleSpecification.assignedAtAfter(filter.getAssignedAtAfter()),
                GroupRoleSpecification.expiryDateBefore(filter.getExpiryDateBefore()),
                GroupRoleSpecification.expiryDateAfter(filter.getExpiryDateAfter()));

        return PagedResponse.fromPage(groupRoleRepository.findAll(spec, filter.toPageable()), GroupRoleDTO::from);
    }

    @Transactional(readOnly = true)
    public boolean hasRole(Long realmId, Long groupId, Long roleId, String roleName) {
        if (roleId == null && (roleName == null || roleName.isEmpty())) {
            throw new IllegalArgumentException("Either roleId or roleName must be provided");
        }

        boolean groupExists = groupRepository.exists(Specification.allOf(
                GroupSpecification.hasId(groupId),
                GroupSpecification.hasRealm(realmId)));
        if (!groupExists) {
            throw new EntityNotFoundException("Group not found");
        }

        if (roleId != null) {
            return groupRoleRepository.existsByGroupIdAndRoleIdAndRole_Realm_Id(groupId, roleId, realmId);
        } else {
            return groupRoleRepository.existsByGroupIdAndRole_NameAndRole_Realm_Id(groupId, roleName, realmId);
        }
    }
}
