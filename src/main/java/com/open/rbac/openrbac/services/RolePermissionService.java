package com.open.rbac.openrbac.services;

import com.open.rbac.openrbac.annotations.RequireAnyRole;
import com.open.rbac.openrbac.dtos.PagedResponse;
import com.open.rbac.openrbac.models.Permission;
import com.open.rbac.openrbac.models.Role;
import com.open.rbac.openrbac.models.User;
import com.open.rbac.openrbac.repositories.PermissionRepository;
import com.open.rbac.openrbac.repositories.RealmRepository;
import com.open.rbac.openrbac.repositories.RoleRepository;
import com.open.rbac.openrbac.repositories.UserRepository;
import com.open.rbac.openrbac.requests.AddRolePermissionsRequest;
import com.open.rbac.openrbac.requests.RemoveRolePermissionsRequest;
import com.open.rbac.openrbac.requests.UpdateRolePermissionsExpiryRequest;
import com.open.rbac.openrbac.requestParams.CheckPermissionRequest;
import com.open.rbac.openrbac.requestParams.RolePermissionFilterRequest;
import com.open.rbac.openrbac.dtos.RolePermissionDTO;
import com.open.rbac.openrbac.models.RolePermission;
import com.open.rbac.openrbac.repositories.RolePermissionRepository;
import com.open.rbac.openrbac.specifications.RolePermissionSpecification;
import com.open.rbac.openrbac.specifications.RoleSpecification;
import com.open.rbac.openrbac.utils.SecurityUtils;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class RolePermissionService {

    private final RealmRepository realmRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final UserRepository userRepository;

    @Transactional
    @RequireAnyRole(value = { "realm-admin", "group-admin" })
    public void addPermissionsToRole(String realmIdentifier, Long roleId, AddRolePermissionsRequest request) {
        Long realmId = resolveRealmId(realmIdentifier);
        Role role = roleRepository.findOne(Specification.allOf(
                RoleSpecification.hasId(roleId),
                RoleSpecification.hasRealm(realmId)))
                .orElseThrow(() -> new EntityNotFoundException("Role not found"));

        final List<Permission> validPermissions = permissionRepository
                .findAllByIdInAndRealm_Id(java.util.Objects.requireNonNull(request.getPermissionIds()), realmId);

        // Identify IDs that are either completely missing or belong to another realm
        List<Long> validPermissionIds = validPermissions.stream().map(Permission::getId).toList();
        List<Long> notFoundPermissionIds = request.getPermissionIds().stream()
                .filter(id -> !validPermissionIds.contains(id))
                .toList();

        if (!notFoundPermissionIds.isEmpty()) {
            throw new EntityNotFoundException("Permissions not found: " + notFoundPermissionIds);
        }
        List<Long> existingPermissionIds = rolePermissionRepository
                .findExistingPermissionIds(roleId, request.getPermissionIds());

        List<Permission> permissionsToAssign = validPermissions.stream()
                .filter(p -> !existingPermissionIds.contains(p.getId()))
                .toList();

        if (permissionsToAssign.isEmpty()) {
            return;
        }

        // Get current user (assignedBy)
        final User assignedBy = SecurityUtils.getAuthenticatedUser(jwt -> {
            String keycloakUserId = jwt.getSubject();
            if (keycloakUserId != null) {
                return userRepository.findByKeycloakUserId(keycloakUserId).orElse(null);
            }
            return null;
        });
        List<RolePermission> rolePermissions = permissionsToAssign.stream().map(p -> RolePermission.builder()
                .role(role)
                .permission(p)
                .assignedBy(assignedBy)
                .expiryDate(request.getExpiryDate())
                .build()).toList();
        rolePermissionRepository.saveAll(rolePermissions);

    }

    @Transactional
    @RequireAnyRole(value = { "realm-admin", "group-admin" })
    public void removePermissionsFromRole(String realmIdentifier, Long roleId, RemoveRolePermissionsRequest request) {
        Long realmId = resolveRealmId(realmIdentifier);
        boolean roleExist = roleRepository.existsByIdAndRealm_id(roleId, realmId);
        if (!roleExist) {
            throw new EntityNotFoundException("Role not found");
        }
        List<Long> existPermissionIds = rolePermissionRepository.findExistingPermissionIds(roleId,
                request.getPermissionIds());

        if (!existPermissionIds.isEmpty()) {
            rolePermissionRepository.deleteByRoleIdAndPermissionIdIn(roleId, existPermissionIds);
        }
    }

    @Transactional
    @RequireAnyRole(value = { "realm-admin", "group-admin" })
    public void updatePermissionsExpiry(String realmIdentifier, Long roleId,
            UpdateRolePermissionsExpiryRequest request) {
        Long realmId = resolveRealmId(realmIdentifier);
        boolean roleExist = roleRepository.existsByIdAndRealm_id(roleId, realmId);
        if (!roleExist) {
            throw new EntityNotFoundException("Role not found");
        }

        List<RolePermission> existingAssignments = rolePermissionRepository.findByRoleIdAndPermissionIdIn(roleId,
                request.getPermissionIds());

        if (existingAssignments.isEmpty()) {
            throw new EntityNotFoundException("No existing permissions found to update");
        }

        existingAssignments.forEach(assignment -> assignment.setExpiryDate(request.getExpiryDate()));
        rolePermissionRepository.saveAll(existingAssignments);
    }

    @Transactional(readOnly = true)
    public PagedResponse<RolePermissionDTO> getRolePermissions(String realmIdentifier, Long roleId,
            RolePermissionFilterRequest filter) {
        Long realmId = resolveRealmId(realmIdentifier);
        if (!roleRepository.existsByIdAndRealm_id(roleId, realmId)) {
            throw new EntityNotFoundException("Role not found");
        }

        Specification<RolePermission> spec = Specification.allOf(
                RolePermissionSpecification.ofRole(roleId, realmId),
                RolePermissionSpecification.hasPermissionName(filter.getPermissionName()),
                RolePermissionSpecification.hasPermissionDescription(filter.getDescription()),
                RolePermissionSpecification.hasPermissionStatus(filter.getStatus()),
                RolePermissionSpecification.hasResource(filter.getResource()),
                RolePermissionSpecification.hasAction(filter.getAction()),
                RolePermissionSpecification.assignedBy(filter.getAssignedBy()),
                RolePermissionSpecification.hasRoleStatus(filter.getRoleStatus()),
                RolePermissionSpecification.assignedAtBefore(filter.getAssignedAtBefore()),
                RolePermissionSpecification.assignedAtAfter(filter.getAssignedAtAfter()));

        return PagedResponse.fromPage(rolePermissionRepository.findAll(spec, filter.toPageable()),
                RolePermissionDTO::from);
    }

    @Transactional(readOnly = true)
    public boolean checkRolePermission(String realmIdentifier, Long roleId,
            CheckPermissionRequest request) {
        Long realmId = resolveRealmId(realmIdentifier);
        if (!roleRepository.existsByIdAndRealm_id(roleId, realmId)) {
            throw new EntityNotFoundException("Role not found");
        }
        if (request.getResource() == null && request.getAction() == null
                && request.getPermissionName() == null) {
            return false;
        }
        return rolePermissionRepository.checkPermission(realmId, roleId, request.getResource(), request.getAction(),
                request.getPermissionName());
    }

    @Transactional(readOnly = true)
    public PagedResponse<String> getRoleResources(String realmIdentifier, Long roleId,
            Pageable pageable) {
        Long realmId = resolveRealmId(realmIdentifier);
        if (!roleRepository.existsByIdAndRealm_id(roleId, realmId)) {
            throw new EntityNotFoundException("Role not found");
        }
        return PagedResponse.fromPage(rolePermissionRepository.findDistinctResourcesByRole(realmId, roleId, pageable),
                s -> s);
    }

    @Transactional(readOnly = true)
    public PagedResponse<String> getRoleActions(String realmIdentifier, Long roleId,
            Pageable pageable) {
        Long realmId = resolveRealmId(realmIdentifier);
        if (!roleRepository.existsByIdAndRealm_id(roleId, realmId)) {
            throw new EntityNotFoundException("Role not found");
        }
        return PagedResponse.fromPage(rolePermissionRepository.findDistinctActionsByRole(realmId, roleId, pageable),
                s -> s);
    }

    private Long resolveRealmId(String realmIdentifier) {
        return realmRepository
                .findOne(com.open.rbac.openrbac.specifications.RealmSpecification.hasIdOrName(realmIdentifier))
                .orElseThrow(() -> new EntityNotFoundException("Realm " + realmIdentifier + " not found"))
                .getId();
    }
}
