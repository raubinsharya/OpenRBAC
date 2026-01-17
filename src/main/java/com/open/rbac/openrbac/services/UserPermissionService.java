package com.open.rbac.openrbac.services;

import com.open.rbac.openrbac.annotations.RequireAnyRole;
import com.open.rbac.openrbac.dtos.PagedResponse;
import com.open.rbac.openrbac.dtos.UserPermissionDTO;
import com.open.rbac.openrbac.models.Permission;
import com.open.rbac.openrbac.models.User;
import com.open.rbac.openrbac.models.UserPermission;
import com.open.rbac.openrbac.repositories.PermissionRepository;
import com.open.rbac.openrbac.repositories.UserPermissionRepository;
import com.open.rbac.openrbac.repositories.UserRepository;
import com.open.rbac.openrbac.requests.AddUserPermissionsRequest;
import com.open.rbac.openrbac.requests.RemoveUserPermissionsRequest;
import com.open.rbac.openrbac.requestParams.UserPermissionFilterRequest;
import com.open.rbac.openrbac.specifications.BaseSpecification;
import com.open.rbac.openrbac.models.UserEffectivePermission;
import com.open.rbac.openrbac.repositories.UserEffectivePermissionRepository;
import com.open.rbac.openrbac.specifications.UserEffectivePermissionSpecification;
import com.open.rbac.openrbac.specifications.UserSpecification;
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
public class UserPermissionService {

        private final UserRepository userRepository;
        private final PermissionRepository permissionRepository;
        private final UserPermissionRepository userPermissionRepository;
        private final UserEffectivePermissionRepository userEffectivePermissionRepository;

        @Transactional
        @RequireAnyRole(value = { "realm-admin" })
        public void addPermissionsToUser(Long realmId, Long userId, AddUserPermissionsRequest request) {
                User user = userRepository.findOne(Specification.allOf(
                                UserSpecification.hasUserId(userId, realmId)))
                                .orElseThrow(() -> new EntityNotFoundException("User not found"));

                final List<Permission> validPermissions = permissionRepository
                                .findAllByIdInAndRealm_Id(Objects.requireNonNull(request.getPermissionIds()), realmId);

                List<Long> validPermissionIds = validPermissions.stream().map(Permission::getId).toList();
                List<Long> notFoundPermissionIds = request.getPermissionIds().stream()
                                .filter(id -> !validPermissionIds.contains(id))
                                .toList();

                if (!notFoundPermissionIds.isEmpty()) {
                        throw new EntityNotFoundException("Permissions not found: " + notFoundPermissionIds);
                }

                List<Long> existingPermissionIds = userPermissionRepository.findExistingPermissionIds(userId,
                                request.getPermissionIds());
                if (!existingPermissionIds.isEmpty()) {
                        throw new IllegalArgumentException("User already has permissions " + existingPermissionIds);
                }

                final User assignedBy = SecurityUtils.getAuthenticatedUser(jwt -> {
                        String username = jwt.getClaimAsString("preferred_username");
                        if (username != null) {
                                return userRepository.findByUsername(username).orElse(null);
                        }
                        return null;
                });

                List<UserPermission> userPermissions = validPermissions.stream().map(p -> UserPermission.builder()
                                .user(user)
                                .permission(p)
                                .assignedBy(assignedBy)
                                .expiryDate(request.getExpiryDate())
                                .isActive(true)
                                .build()).toList();

                userPermissionRepository.saveAll(userPermissions);
        }

        @Transactional
        @RequireAnyRole(value = { "realm-admin" })
        public void removePermissionsFromUser(Long realmId, Long userId, RemoveUserPermissionsRequest request) {
                boolean userExists = userRepository
                                .exists(Specification.allOf(UserSpecification.hasUserId(userId, realmId)));
                if (!userExists) {
                        throw new EntityNotFoundException("User not found");
                }

                List<Long> existingPermissionIds = userPermissionRepository.findExistingPermissionIds(userId,
                                request.getPermissionIds());
                if (existingPermissionIds.size() != request.getPermissionIds().size()) {
                        request.getPermissionIds().removeAll(new HashSet<>(existingPermissionIds));
                        throw new EntityNotFoundException(
                                        "Permissions are not assigned to this user: " + request.getPermissionIds());
                }

                userPermissionRepository.deleteByUserIdAndPermissionIdIn(userId, existingPermissionIds);
        }

        @Transactional(readOnly = true)
        public PagedResponse<UserPermissionDTO> getUserPermissions(Long realmId, Long userId,
                        UserPermissionFilterRequest filter) {
                if (!userRepository.exists(Specification.allOf(UserSpecification.hasUserId(userId, realmId)))) {
                        throw new EntityNotFoundException("User not found");
                }

                Specification<UserEffectivePermission> spec = Specification.allOf(
                                UserEffectivePermissionSpecification
                                                .ofUser(userId, realmId),
                                UserEffectivePermissionSpecification
                                                .isNotExpired(),
                                UserEffectivePermissionSpecification
                                                .hasPermissionName(filter.getPermissionName()),
                                UserEffectivePermissionSpecification
                                                .hasResource(filter.getResource()),
                                UserEffectivePermissionSpecification
                                                .hasAction(filter.getAction()),
                                UserEffectivePermissionSpecification
                                                .hasPermissionStatus(filter.getPermissionStatus()),
                                UserEffectivePermissionSpecification
                                                .hasUserStatus(filter.getUserStatus()),
                                UserEffectivePermissionSpecification
                                                .assignedBy(filter.getAssignedBy()),
                                UserEffectivePermissionSpecification
                                                .isActive(filter.getIsActive()),
                                UserEffectivePermissionSpecification
                                                .assignedAtBefore(filter.getAssignedAtBefore()),
                                UserEffectivePermissionSpecification
                                                .assignedAtAfter(filter.getAssignedAtAfter()),
                                UserEffectivePermissionSpecification
                                                .expiryDateBefore(filter.getExpiryDateBefore()),
                                UserEffectivePermissionSpecification
                                                .expiryDateAfter(filter.getExpiryDateAfter()),
                                UserEffectivePermissionSpecification
                                                .fromRole(filter.isFromRole()),
                                BaseSpecification.withBaseFilters(filter));

                return PagedResponse.fromPage(userEffectivePermissionRepository.findAll(spec, filter.toPageable()),
                                p -> UserPermissionDTO.from(p));
        }

        @Transactional(readOnly = true)
        public boolean checkPermission(Long realmId, Long userId,
                        com.open.rbac.openrbac.requestParams.CheckPermissionRequest request) {
                if (!userRepository.exists(Specification.allOf(UserSpecification.hasUserId(userId, realmId)))) {
                        throw new EntityNotFoundException("User not found");
                }
                if (request.getResource() == null && request.getAction() == null
                                && request.getPermissionName() == null) {
                        return false;
                }
                return userEffectivePermissionRepository.checkPermission(realmId, userId, request.getResource(),
                                request.getAction(), request.getAssignmentType(), request.getPermissionName());
        }

        @Transactional(readOnly = true)
        public PagedResponse<String> getEffectiveUserResources(Long realmId, Long userId,
                        org.springframework.data.domain.Pageable pageable, boolean fromRole) {
                if (!userRepository.exists(Specification.allOf(UserSpecification.hasUserId(userId, realmId)))) {
                        throw new EntityNotFoundException("User not found");
                }
                return PagedResponse.fromPage(userEffectivePermissionRepository.findDistinctResourcesByUser(realmId,
                                userId, fromRole, pageable), s -> s);
        }

        @Transactional(readOnly = true)
        public PagedResponse<String> getEffectiveUserActions(Long realmId, Long userId,
                        org.springframework.data.domain.Pageable pageable, boolean fromRole) {
                if (!userRepository.exists(Specification.allOf(UserSpecification.hasUserId(userId, realmId)))) {
                        throw new EntityNotFoundException("User not found");
                }
                return PagedResponse.fromPage(userEffectivePermissionRepository.findDistinctActionsByUser(realmId,
                                userId, fromRole, pageable), s -> s);
        }
}
