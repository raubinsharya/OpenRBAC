package com.open.rbac.openrbac.services;

import com.open.rbac.openrbac.annotations.RequireAnyRole;
import com.open.rbac.openrbac.dtos.GroupPermissionDTO;
import com.open.rbac.openrbac.dtos.PagedResponse;
import com.open.rbac.openrbac.models.Group;
import com.open.rbac.openrbac.models.GroupEffectivePermission;
import com.open.rbac.openrbac.models.GroupPermission;
import com.open.rbac.openrbac.models.Permission;
import com.open.rbac.openrbac.models.User;
import com.open.rbac.openrbac.repositories.GroupEffectivePermissionRepository;
import com.open.rbac.openrbac.repositories.GroupPermissionRepository;
import com.open.rbac.openrbac.repositories.GroupRepository;
import com.open.rbac.openrbac.repositories.PermissionRepository;
import com.open.rbac.openrbac.repositories.UserRepository;
import com.open.rbac.openrbac.requestParams.GroupPermissionFilterRequest;
import com.open.rbac.openrbac.requests.AddGroupPermissionsRequest;
import com.open.rbac.openrbac.requests.RemoveGroupPermissionsRequest;
import com.open.rbac.openrbac.specifications.GroupEffectivePermissionSpecification;
import com.open.rbac.openrbac.specifications.GroupSpecification;
import com.open.rbac.openrbac.utils.SecurityUtils;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class GroupPermissionService {

        private final GroupRepository groupRepository;
        private final PermissionRepository permissionRepository;
        private final GroupPermissionRepository groupPermissionRepository;
        private final GroupEffectivePermissionRepository groupEffectivePermissionRepository;
        private final UserRepository userRepository;

        @Transactional
        @RequireAnyRole(value = { "realm-admin", "group-admin" })
        public void addPermissionsToGroup(String realmIdentifier, Long groupId, AddGroupPermissionsRequest request) {
                Group group = groupRepository.findOne(Specification.allOf(
                                GroupSpecification.hasId(groupId),
                                GroupSpecification.hasRealm(realmIdentifier)))
                                .orElseThrow(() -> new EntityNotFoundException("Group not found"));

                final List<Permission> validPermissions = permissionRepository
                                .findAllByIdInAndRealm_Id(Objects.requireNonNull(request.getPermissionIds()),
                                                group.getRealm().getId());

                List<Long> validPermissionIds = validPermissions.stream().map(Permission::getId).toList();
                List<Long> notFoundPermissionIds = request.getPermissionIds().stream()
                                .filter(id -> !validPermissionIds.contains(id))
                                .toList();

                if (!notFoundPermissionIds.isEmpty()) {
                        throw new EntityNotFoundException("Permissions not found: " + notFoundPermissionIds);
                }

                List<Long> existingPermissionIds = groupPermissionRepository.findExistingPermissionIds(groupId,
                                request.getPermissionIds());
                if (!existingPermissionIds.isEmpty()) {
                        throw new IllegalArgumentException("Group already has permissions " + existingPermissionIds);
                }

                final User assignedBy = SecurityUtils.getAuthenticatedUser(jwt -> {
                        String sub = jwt.getSubject();
                        if (sub != null) {
                                return userRepository.findByKeycloakUserId(sub).orElse(null);
                        }
                        return null;
                });

                List<GroupPermission> groupPermissions = validPermissions.stream().map(p -> GroupPermission.builder()
                                .group(group)
                                .permission(p)
                                .assignedBy(assignedBy)
                                .createdAt(LocalDateTime.now())
                                .expiryDate(request.getExpiryDate())
                                .isActive(true)
                                .isInherited(false)
                                .sourceGroup(null)
                                .allowInheritance(request.getAllowInheritance())
                                .maxInheritanceDepth(request.getMaxInheritanceDepth())
                                .build()).toList();

                groupPermissionRepository.saveAll(groupPermissions);
        }

        @Transactional
        @RequireAnyRole(value = { "realm-admin", "group-admin" })
        public void removePermissionsFromGroup(String realmIdentifier, Long groupId,
                        RemoveGroupPermissionsRequest request) {
                boolean groupExists = groupRepository.exists(Specification.allOf(
                                GroupSpecification.hasId(groupId),
                                GroupSpecification.hasRealm(realmIdentifier)));
                if (!groupExists) {
                        throw new EntityNotFoundException("Group not found");
                }

                List<Long> existingPermissionIds = groupPermissionRepository.findExistingPermissionIds(groupId,
                                request.getPermissionIds());
                if (existingPermissionIds.size() != request.getPermissionIds().size()) {
                        request.getPermissionIds().removeAll(new HashSet<>(existingPermissionIds));
                        throw new EntityNotFoundException(
                                        "Permissions are not assigned to this group: " + request.getPermissionIds());
                }

                groupPermissionRepository.deleteByGroupIdAndPermissionIdIn(groupId, existingPermissionIds);
        }

        @Transactional(readOnly = true)
        public PagedResponse<GroupPermissionDTO> getGroupPermissions(String realmIdentifier, Long groupId,
                        GroupPermissionFilterRequest filter) {
                Group group = groupRepository.findOne(Specification.allOf(
                                GroupSpecification.hasId(groupId),
                                GroupSpecification.hasRealm(realmIdentifier)))
                                .orElseThrow(() -> new EntityNotFoundException("Group not found"));

                List<Long> ancestorIds = parseAncestorIds(group.getPath());

                Specification<GroupEffectivePermission> spec = Specification.allOf(
                                GroupEffectivePermissionSpecification.ofGroup(groupId, ancestorIds, group.getLevel(),
                                                group.getRealm().getId()),
                                GroupEffectivePermissionSpecification.hasPermissionName(filter.getPermissionName()),
                                GroupEffectivePermissionSpecification.hasPermissionStatus(filter.getPermissionStatus()),
                                GroupEffectivePermissionSpecification.hasGroupStatus(filter.getGroupStatus()),
                                GroupEffectivePermissionSpecification.assignedBy(filter.getAssignedBy()),
                                GroupEffectivePermissionSpecification.isActive(filter.getIsActive()),
                                GroupEffectivePermissionSpecification.isInherited(groupId, filter.getIsInherited()),
                                GroupEffectivePermissionSpecification.assignedAtBefore(filter.getAssignedAtBefore()),
                                GroupEffectivePermissionSpecification.assignedAtAfter(filter.getAssignedAtAfter()),
                                GroupEffectivePermissionSpecification.expiryDateBefore(filter.getExpiryDateBefore()),
                                GroupEffectivePermissionSpecification.expiryDateAfter(filter.getExpiryDateAfter()),
                                GroupEffectivePermissionSpecification.assignmentType(filter.getAssignmentType()));

                return PagedResponse.fromPage(groupEffectivePermissionRepository.findAll(spec, filter.toPageable()),
                                gep -> GroupPermissionDTO.from(gep, groupId));
        }

        @Transactional(readOnly = true)
        public boolean hasPermission(String realmIdentifier, Long groupId, Long permissionId, String permissionName,
                        String resource,
                        String action) {
                if (permissionId == null && (permissionName == null || permissionName.isEmpty())
                                && (resource == null || resource.isEmpty()) && (action == null || action.isEmpty())) {
                        throw new IllegalArgumentException(
                                        "Either permissionId, permissionName, or resource/action must be provided");
                }

                Group group = groupRepository.findOne(Specification.allOf(
                                GroupSpecification.hasId(groupId),
                                GroupSpecification.hasRealm(realmIdentifier)))
                                .orElseThrow(() -> new EntityNotFoundException("Group not found"));

                List<Long> ancestorIds = parseAncestorIds(group.getPath());

                Specification<GroupEffectivePermission> spec = Specification.allOf(
                                GroupEffectivePermissionSpecification.ofGroup(groupId, ancestorIds, group.getLevel(),
                                                group.getRealm().getId()),
                                GroupEffectivePermissionSpecification.isNotExpired(),
                                GroupEffectivePermissionSpecification.isActive(true));

                if (permissionId != null) {
                        spec = spec.and((root, query, cb) -> cb.equal(root.get("permission").get("id"), permissionId));
                } else {
                        spec = spec.and(GroupEffectivePermissionSpecification.hasPermissionName(permissionName));
                        spec = spec.and(GroupEffectivePermissionSpecification.hasResource(resource));
                        spec = spec.and(GroupEffectivePermissionSpecification.hasAction(action));
                }

                return groupEffectivePermissionRepository.exists(spec);
        }

        private List<Long> parseAncestorIds(String path) {
                if (path == null || path.equals("/") || path.isEmpty()) {
                        return List.of();
                }
                return Arrays.stream(path.split("/"))
                                .filter(s -> !s.isEmpty())
                                .map(Long::valueOf)
                                .toList();
        }
}
