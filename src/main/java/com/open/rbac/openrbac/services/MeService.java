package com.open.rbac.openrbac.services;

import com.open.rbac.openrbac.requestParams.PermissionFilterRequest;
import com.open.rbac.openrbac.requestParams.RoleFilterRequest;
import com.open.rbac.openrbac.dtos.PagedResponse;
import com.open.rbac.openrbac.dtos.PermissionDTO;
import com.open.rbac.openrbac.dtos.RoleDTO;
import com.open.rbac.openrbac.dtos.UserDTO;
import com.open.rbac.openrbac.models.User;
import com.open.rbac.openrbac.repositories.UserRepository;
import com.open.rbac.openrbac.specifications.BaseSpecification;
import com.open.rbac.openrbac.dtos.UserPermissionDTO;
import com.open.rbac.openrbac.dtos.UserRoleDTO;
import com.open.rbac.openrbac.models.UserEffectivePermission;
import com.open.rbac.openrbac.models.UserEffectiveRole;
import com.open.rbac.openrbac.repositories.UserEffectivePermissionRepository;
import com.open.rbac.openrbac.repositories.UserEffectiveRoleRepository;
import com.open.rbac.openrbac.specifications.UserEffectivePermissionSpecification;
import com.open.rbac.openrbac.specifications.UserEffectiveRoleSpecification;
import com.open.rbac.openrbac.specifications.UserSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MeService {
    private final UserRepository userRepository;

    private final UserEffectiveRoleRepository userEffectiveRoleRepository;
    private final UserEffectivePermissionRepository userEffectivePermissionRepository;

    public Optional<UserDTO> getUser(String username, boolean includeRealm) {
        Specification<User> userSpecification = Specification.allOf(
                UserSpecification.hasStatus("active"),
                UserSpecification.hasUserName(username),
                UserSpecification.includeRealm(includeRealm));
        return userRepository.findAll(userSpecification).stream().findFirst().map(u -> UserDTO.from(u, includeRealm));
    }

    public List<RoleDTO> getMeRoles(String userName) {
        User user = userRepository.findByUsername(userName)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("User not found"));
        Long realmId = user.getRealm().getId();
        Specification<UserEffectiveRole> spec = Specification.allOf(
                UserEffectiveRoleSpecification.ofUser(user.getId(), realmId),
                UserEffectiveRoleSpecification.isNotExpired(),
                UserEffectiveRoleSpecification.isActive(true));
        return userEffectiveRoleRepository.findAll(spec).stream()
                .map(UserRoleDTO::from)
                .map(role -> new RoleDTO(
                        role.roleId(),
                        role.roleName(),
                        null,
                        role.roleStatus(),
                        false, // System role info not available in effective view easily, defaulting false or
                               // need to fetch
                        role.assignedAt(),
                        null))
                .collect(Collectors.toList());

    }

    public List<PermissionDTO> getMePermissions(String userName) {
        User user = userRepository.findByUsername(userName)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("User not found"));
        Long realmId = user.getRealm().getId();
        Specification<UserEffectivePermission> spec = Specification.allOf(
                UserEffectivePermissionSpecification.ofUser(user.getId(), realmId),
                UserEffectivePermissionSpecification.isNotExpired(),
                UserEffectivePermissionSpecification.isActive(true));
        return userEffectivePermissionRepository.findAll(spec).stream()
                .map(UserPermissionDTO::from)
                .map(perm -> new PermissionDTO(
                        perm.permissionId(),
                        perm.permissionName(),
                        perm.resource(),
                        perm.action(),
                        null,
                        perm.permissionStatus(),
                        perm.assignedAt(),
                        null))
                .collect(Collectors.toList());
    }

    public PagedResponse<UserRoleDTO> getMeRoles(String userName, RoleFilterRequest filter) {
        // Need user ID first
        User user = userRepository.findByUsername(userName)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("User not found"));
        Long realmId = user.getRealm().getId();

        Specification<UserEffectiveRole> spec = Specification.allOf(
                UserEffectiveRoleSpecification.ofUser(user.getId(), realmId),
                UserEffectiveRoleSpecification.isNotExpired(),
                UserEffectiveRoleSpecification.hasRoleName(filter.getName()),
                UserEffectiveRoleSpecification.isActive(true), // Only active for "me"
                UserEffectiveRoleSpecification.assignmentType(filter.getAssignmentType()),
                BaseSpecification.withBaseFilters(filter));

        return PagedResponse.fromPage(userEffectiveRoleRepository.findAll(spec, filter.toPageable()),
                UserRoleDTO::from);
    }

    public PagedResponse<UserPermissionDTO> getMePermissions(String userName, PermissionFilterRequest filter) {
        // Need user ID first
        User user = userRepository.findByUsername(userName)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("User not found"));
        Long realmId = user.getRealm().getId();

        Specification<UserEffectivePermission> spec = Specification.allOf(
                UserEffectivePermissionSpecification.ofUser(user.getId(), realmId),
                UserEffectivePermissionSpecification.isNotExpired(),
                UserEffectivePermissionSpecification.hasPermissionName(filter.getName()),
                UserEffectivePermissionSpecification.hasResource(filter.getResource()),
                UserEffectivePermissionSpecification.hasAction(filter.getAction()),
                UserEffectivePermissionSpecification.isActive(true), // Only active for "me"
                UserEffectivePermissionSpecification.assignmentType(filter.getAssignmentType()),
                BaseSpecification.withBaseFilters(filter));

        return PagedResponse.fromPage(userEffectivePermissionRepository.findAll(spec, filter.toPageable()),
                UserPermissionDTO::from);
    }
}
