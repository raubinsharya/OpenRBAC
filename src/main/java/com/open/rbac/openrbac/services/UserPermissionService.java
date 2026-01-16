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
import com.open.rbac.openrbac.specifications.UserPermissionSpecification;
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
        boolean userExists = userRepository.exists(Specification.allOf(UserSpecification.hasUserId(userId, realmId)));
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
        boolean userExists = userRepository.exists(Specification.allOf(UserSpecification.hasUserId(userId, realmId)));
        if (!userExists) {
            throw new EntityNotFoundException("User not found");
        }

        Specification<UserPermission> spec = Specification.allOf(
                UserPermissionSpecification.ofUser(userId, realmId),
                UserPermissionSpecification.hasPermissionName(filter.getPermissionName()),
                UserPermissionSpecification.hasResource(filter.getResource()),
                UserPermissionSpecification.hasAction(filter.getAction()),
                UserPermissionSpecification.hasPermissionStatus(filter.getPermissionStatus()),
                UserPermissionSpecification.hasUserStatus(filter.getUserStatus()),
                UserPermissionSpecification.assignedBy(filter.getAssignedBy()),
                UserPermissionSpecification.isActive(filter.getIsActive()),
                UserPermissionSpecification.assignedAtBefore(filter.getAssignedAtBefore()),
                UserPermissionSpecification.assignedAtAfter(filter.getAssignedAtAfter()),
                UserPermissionSpecification.expiryDateBefore(filter.getExpiryDateBefore()),
                UserPermissionSpecification.expiryDateAfter(filter.getExpiryDateAfter()));

        return PagedResponse.fromPage(userPermissionRepository.findAll(spec, filter.toPageable()),
                UserPermissionDTO::from);
    }
}
