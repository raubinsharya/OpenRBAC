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
import com.open.rbac.openrbac.requestParams.CheckPermissionRequest;
import com.open.rbac.openrbac.requestParams.UserPermissionFilterRequest;
import com.open.rbac.openrbac.specifications.BaseSpecification;
import com.open.rbac.openrbac.models.UserEffectivePermission;
import com.open.rbac.openrbac.repositories.UserEffectivePermissionRepository;
import com.open.rbac.openrbac.specifications.UserEffectivePermissionSpecification;
import com.open.rbac.openrbac.specifications.UserSpecification;
import com.open.rbac.openrbac.utils.SecurityUtils;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
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
        // @RequireAnyRole(value = {"realm-admin"})
        public void addPermissionsToUser(String realmIdentifier, Long userId, AddUserPermissionsRequest request) {
                User user = userRepository.findOne(Specification.allOf(
                                UserSpecification.hasUserId(userId, realmIdentifier),
                                UserSpecification.includeRealm(true)))
                                .orElseThrow(() -> new EntityNotFoundException("User not found"));

                final List<Permission> validPermissions = permissionRepository
                                .findAllByIdInAndRealm_Id(Objects.requireNonNull(request.getPermissionIds()),
                                                user.getRealm().getId());

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
                        String keycloakUserId = jwt.getSubject();
                        if (keycloakUserId != null) {
                                return userRepository.findByKeycloakUserId(keycloakUserId).orElse(null);
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
        // @RequireAnyRole(value = {"realm-admin"})
        public void removePermissionsFromUser(String realmIdentifier, Long userId,
                        RemoveUserPermissionsRequest request) {
                boolean userExists = userRepository
                                .exists(Specification.allOf(UserSpecification.hasUserId(userId, realmIdentifier)));
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
        public PagedResponse<UserPermissionDTO> getUserPermissions(String realmIdentifier, Long userId,
                        UserPermissionFilterRequest filter) {
                if (!userRepository.exists(Specification.allOf(UserSpecification.hasUserId(userId, realmIdentifier)))) {
                        throw new EntityNotFoundException("User not found");
                }

                Specification<UserEffectivePermission> spec = Specification.allOf(
                                UserEffectivePermissionSpecification
                                                .ofUser(userId, realmIdentifier),
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
                                                .assignmentType(filter.getAssignmentType()),
                                BaseSpecification.withBaseFilters(filter));

                return PagedResponse.fromPage(userEffectivePermissionRepository.findAll(spec, filter.toPageable()),
                                UserPermissionDTO::from);
        }

        @Transactional(readOnly = true)
        public boolean checkPermission(String realmId, Long userId,
                        CheckPermissionRequest request) {
                Specification<User> userSpecification = Specification.allOf(
                                UserSpecification.hasUserId(userId, realmId),
                                UserSpecification.includeRealm(true));
                var user = userRepository.findOne(userSpecification)
                                .orElseThrow(() -> new EntityNotFoundException("User not found"));

                if (request.getResource() == null && request.getAction() == null
                                && request.getPermissionName() == null) {
                        return false;
                }
                return userEffectivePermissionRepository.checkPermission(user.getRealm().getId(), userId,
                                request.getResource(),
                                request.getAction(), request.getAssignmentType(), request.getPermissionName());
        }

        @Transactional(readOnly = true)
        public PagedResponse<String> getEffectiveUserResources(String realmIdentifier, Long userId,
                        Pageable pageable, String assignmentType) {
                Specification<User> userSpecification = Specification.allOf(
                                UserSpecification.hasUserId(userId, realmIdentifier),
                                UserSpecification.includeRealm(true));
                var user = userRepository.findOne(userSpecification)
                                .orElseThrow(() -> new EntityNotFoundException("User not found"));

                return PagedResponse.fromPage(
                                userEffectivePermissionRepository.findDistinctResourcesByUser(user.getRealm().getId(),
                                                userId, assignmentType, pageable),
                                s -> s);
        }

        @Transactional(readOnly = true)
        public PagedResponse<String> getEffectiveUserActions(String realmId, Long userId,
                        Pageable pageable, String assignmentType) {
                Specification<User> userSpecification = Specification.allOf(
                                UserSpecification.hasUserId(userId, realmId),
                                UserSpecification.includeRealm(true));
                var user = userRepository.findOne(userSpecification)
                                .orElseThrow(() -> new EntityNotFoundException("User not found"));

                return PagedResponse.fromPage(
                                userEffectivePermissionRepository.findDistinctActionsByUser(user.getRealm().getId(),
                                                userId, assignmentType, pageable),
                                s -> s);
        }
}
