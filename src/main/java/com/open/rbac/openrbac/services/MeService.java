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
import jakarta.persistence.EntityNotFoundException;
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

        public Optional<UserDTO> getUser(String keycloakUserId, boolean includeRealm) {
                Specification<User> userSpecification = Specification.allOf(
                                UserSpecification.hasStatus("active"),
                                UserSpecification.hasKeycloakUserId(keycloakUserId),
                                UserSpecification.includeRealm(includeRealm));
                return userRepository.findAll(userSpecification).stream().findFirst()
                                .map(u -> UserDTO.from(u, includeRealm));
        }

        public List<RoleDTO> getMeRoles(String keycloakUserId) {
                User user = userRepository.findByKeycloakUserId(keycloakUserId)
                                .orElseThrow(() -> new EntityNotFoundException("User not found"));
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
                                                false, // System role info not available in effective view easily,
                                                       // defaulting false or
                                                       // need to fetch
                                                role.assignedAt(),
                                                null))
                                .collect(Collectors.toList());

        }

        public boolean hasAnyRole(String keycloakUserId, List<String> roleNames) {
                User user = userRepository.findByKeycloakUserId(keycloakUserId)
                                .orElseThrow(() -> new EntityNotFoundException("User not found"));
                Long realmId = user.getRealm().getId();
                Specification<UserEffectiveRole> spec = Specification.allOf(
                                UserEffectiveRoleSpecification.ofUser(user.getId(), realmId),
                                UserEffectiveRoleSpecification.isNotExpired(),
                                UserEffectiveRoleSpecification.isActive(true),
                                UserEffectiveRoleSpecification.hasRoleNameIn(roleNames));
                return userEffectiveRoleRepository.exists(spec);
        }

        public boolean hasAllRoles(String keycloakUserId, List<String> roleNames) {
                User user = userRepository.findByKeycloakUserId(keycloakUserId)
                                .orElseThrow(() -> new EntityNotFoundException("User not found"));
                Long realmId = user.getRealm().getId();
                Specification<UserEffectiveRole> spec = Specification.allOf(
                                UserEffectiveRoleSpecification.ofUser(user.getId(), realmId),
                                UserEffectiveRoleSpecification.isNotExpired(),
                                UserEffectiveRoleSpecification.isActive(true),
                                UserEffectiveRoleSpecification.hasRoleNameIn(roleNames));
                // For "All roles", the count of unique matching roles must equal the requested
                // list size
                // Note: accurate "hasAll" check usually requires checking count(distinct
                // role_id)
                // This is a simplified check that assumes unique role names in the input list

                // Ideally we should group by role.name and count, but simple count might be
                // strictly sufficient if roles are unique per user-effective-view which they
                // might not be (inheritance)
                // A better approach for ALL check via DB is: find roles WHERE name IN (...) ->
                // get names -> compare sets in Java
                // But to fully optimize avoiding fetching all roles, we can fetch ONLY the
                // names
                // For now, let's implement a "fetch only matching names" strategy for "ALL"
                // checks to keep it robust and efficient.
                List<UserEffectiveRole> matchingRoles = userEffectiveRoleRepository.findAll(spec);
                long uniqueMatchingRoles = matchingRoles.stream()
                                .map(r -> r.getRole().getName())
                                .distinct()
                                .count();
                return uniqueMatchingRoles >= roleNames.size();
        }

        public boolean hasAnyPermission(String keycloakUserId, List<String> permissions) {
                // Permissions are "resource:action" strings
                User user = userRepository.findByKeycloakUserId(keycloakUserId)
                                .orElseThrow(() -> new EntityNotFoundException("User not found"));
                Long realmId = user.getRealm().getId();

                for (String permString : permissions) {
                        String[] parts = permString.split(":");
                        if (parts.length != 2)
                                continue;
                        String resource = parts[0];
                        String action = parts[1];

                        Specification<UserEffectivePermission> spec = Specification.allOf(
                                        UserEffectivePermissionSpecification.ofUser(user.getId(), realmId),
                                        UserEffectivePermissionSpecification.isNotExpired(),
                                        UserEffectivePermissionSpecification.isActive(true),
                                        UserEffectivePermissionSpecification.hasResource(resource),
                                        UserEffectivePermissionSpecification.hasAction(action));
                        if (userEffectivePermissionRepository.exists(spec)) {
                                return true;
                        }
                }
                return false;
        }

        public boolean hasAllPermissions(String keycloakUserId, List<String> permissions) {
                User user = userRepository.findByKeycloakUserId(keycloakUserId)
                                .orElseThrow(() -> new EntityNotFoundException("User not found"));
                Long realmId = user.getRealm().getId();

                // Using stream to check all, but could be slow if many permissions.
                // However, usually required permissions are few (1-3).
                for (String permString : permissions) {
                        String[] parts = permString.split(":");
                        if (parts.length != 2)
                                return false;
                        String resource = parts[0];
                        String action = parts[1];

                        Specification<UserEffectivePermission> spec = Specification.allOf(
                                        UserEffectivePermissionSpecification.ofUser(user.getId(), realmId),
                                        UserEffectivePermissionSpecification.isNotExpired(),
                                        UserEffectivePermissionSpecification.isActive(true),
                                        UserEffectivePermissionSpecification.hasResource(resource),
                                        UserEffectivePermissionSpecification.hasAction(action));

                        // If any permission is missing, return false
                        if (!userEffectivePermissionRepository.exists(spec)) {
                                return false;
                        }
                }
                return true;
        }

        public List<PermissionDTO> getMePermissions(String keycloakUserId) {
                User user = userRepository.findByKeycloakUserId(keycloakUserId)
                                .orElseThrow(() -> new EntityNotFoundException("User not found"));
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

        public PagedResponse<UserRoleDTO> getMeRoles(String keycloakUserId, RoleFilterRequest filter) {
                // Need user ID first
                User user = userRepository.findByKeycloakUserId(keycloakUserId)
                                .orElseThrow(() -> new EntityNotFoundException("User not found"));
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

        public PagedResponse<UserPermissionDTO> getMePermissions(String keycloakUserId,
                        PermissionFilterRequest filter) {
                // Need user ID first
                User user = userRepository.findByKeycloakUserId(keycloakUserId)
                                .orElseThrow(() -> new EntityNotFoundException("User not found"));
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
