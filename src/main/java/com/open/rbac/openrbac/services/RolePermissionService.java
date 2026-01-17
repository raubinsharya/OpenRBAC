package com.open.rbac.openrbac.services;

import com.open.rbac.openrbac.annotations.RequireAnyRole;
import com.open.rbac.openrbac.dtos.PagedResponse;
import com.open.rbac.openrbac.models.Permission;
import com.open.rbac.openrbac.models.Role;
import com.open.rbac.openrbac.models.User;
import com.open.rbac.openrbac.repositories.PermissionRepository;
import com.open.rbac.openrbac.repositories.RoleRepository;
import com.open.rbac.openrbac.repositories.UserRepository;
import com.open.rbac.openrbac.requests.AddRolePermissionsRequest;
import com.open.rbac.openrbac.requests.RemoveRolePermissionsRequest;
import com.open.rbac.openrbac.requestParams.RolePermissionFilterRequest;
import com.open.rbac.openrbac.dtos.RolePermissionDTO;
import com.open.rbac.openrbac.models.RolePermission;
import com.open.rbac.openrbac.repositories.RolePermissionRepository;
import com.open.rbac.openrbac.specifications.RolePermissionSpecification;
import com.open.rbac.openrbac.specifications.RoleSpecification;
import com.open.rbac.openrbac.utils.SecurityUtils;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class RolePermissionService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final UserRepository userRepository;

    @Transactional
    @RequireAnyRole(value = { "realm-admin", "group-admin" })
    public void addPermissionsToRole(Long realmId, Long roleId, AddRolePermissionsRequest request) {
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
        List<Long> rolePermissionAssociation = rolePermissionRepository
                .findExistingPermissionIds(roleId, request.getPermissionIds());
        if (!rolePermissionAssociation.isEmpty()) {
            throw new IllegalArgumentException("Role has already permissions " + rolePermissionAssociation);
        }

        // Get current user (assignedBy)
        final User assignedBy = SecurityUtils.getAuthenticatedUser(jwt -> {
            String username = jwt.getClaimAsString("preferred_username");
            if (username != null) {
                return userRepository.findByUsername(username).orElse(null);
            }
            return null;
        });
        List<RolePermission> rolePermissions = validPermissions.stream().map(p -> RolePermission.builder()
                .role(role)
                .permission(p)
                .assignedBy(assignedBy)
                .expiryDate(request.getExpiryDate())
                .build()).toList();
        rolePermissionRepository.saveAll(rolePermissions);

    }

    @Transactional
    @RequireAnyRole(value = { "realm-admin", "group-admin" })
    public void removePermissionsFromRole(Long realmId, Long roleId, RemoveRolePermissionsRequest request) {
        boolean roleExist = roleRepository.existsByIdAndRealm_id(roleId, realmId);
        if (!roleExist) {
            throw new EntityNotFoundException("Role not found");
        }
        List<Long> existPermissions = rolePermissionRepository.findExistingPermissionIds(roleId,
                request.getPermissionIds());
        if (existPermissions.size() != request.getPermissionIds().size()) {
            request.getPermissionIds().removeAll(new HashSet<>(existPermissions));
            throw new EntityNotFoundException("Permissions are not part this role : " + request.getPermissionIds());
        }
        rolePermissionRepository.deleteByRoleIdAndPermissionIdIn(roleId, existPermissions);
    }

    @Transactional(readOnly = true)
    public PagedResponse<RolePermissionDTO> getRolePermissions(Long realmId, Long roleId,
            RolePermissionFilterRequest filter) {
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
                RolePermissionSpecification.assignedAtAfter(filter.getAssignedAtAfter()),
                RolePermissionSpecification.isNotExpired());

        return PagedResponse.fromPage(rolePermissionRepository.findAll(spec, filter.toPageable()),
                RolePermissionDTO::from);
    }

    @Transactional(readOnly = true)
    public boolean checkRolePermission(Long realmId, Long roleId, String resource, String action) {
        if (!roleRepository.existsByIdAndRealm_id(roleId, realmId)) {
            throw new EntityNotFoundException("Role not found");
        }
        return rolePermissionRepository.checkPermission(realmId, roleId, resource, action);
    }

    @Transactional(readOnly = true)
    public PagedResponse<String> getRoleResources(Long realmId, Long roleId,
            org.springframework.data.domain.Pageable pageable) {
        if (!roleRepository.existsByIdAndRealm_id(roleId, realmId)) {
            throw new EntityNotFoundException("Role not found");
        }
        return PagedResponse.fromPage(rolePermissionRepository.findDistinctResourcesByRole(realmId, roleId, pageable),
                s -> s);
    }

    @Transactional(readOnly = true)
    public PagedResponse<String> getRoleActions(Long realmId, Long roleId,
            org.springframework.data.domain.Pageable pageable) {
        if (!roleRepository.existsByIdAndRealm_id(roleId, realmId)) {
            throw new EntityNotFoundException("Role not found");
        }
        return PagedResponse.fromPage(rolePermissionRepository.findDistinctActionsByRole(realmId, roleId, pageable),
                s -> s);
    }
}
