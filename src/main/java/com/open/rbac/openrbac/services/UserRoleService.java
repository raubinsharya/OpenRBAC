package com.open.rbac.openrbac.services;

import com.open.rbac.openrbac.annotations.RequireAnyRole;
import com.open.rbac.openrbac.dtos.PagedResponse;
import com.open.rbac.openrbac.dtos.UserRoleDTO;
import com.open.rbac.openrbac.models.Role;
import com.open.rbac.openrbac.models.User;
import com.open.rbac.openrbac.models.UserEffectiveRole;
import com.open.rbac.openrbac.models.UserRole;
import com.open.rbac.openrbac.repositories.RoleRepository;
import com.open.rbac.openrbac.repositories.UserEffectiveRoleRepository;
import com.open.rbac.openrbac.repositories.UserRoleRepository;
import com.open.rbac.openrbac.repositories.UserRepository;
import com.open.rbac.openrbac.requests.AddUserRolesRequest;
import com.open.rbac.openrbac.requests.RemoveUserRolesRequest;
import com.open.rbac.openrbac.requestParams.UserRoleFilterRequest;
import com.open.rbac.openrbac.specifications.UserEffectiveRoleSpecification;
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
public class UserRoleService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final UserEffectiveRoleRepository userEffectiveRoleRepository;

    @Transactional
    // @RequireAnyRole(value = {"realm-admin", "group-admin"})
    public void addRolesToUser(String realmIdentifier, Long userId, AddUserRolesRequest request) {
        User user = userRepository.findOne(
                Specification.allOf(
                        UserSpecification.hasUserId(userId, realmIdentifier),
                        UserSpecification.includeRealm(true)))
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        final List<Role> validRoles = roleRepository
                .findAllByIdInAndRealm_Id(Objects.requireNonNull(request.getRoleIds()), user.getRealm().getId());

        // Identify IDs that are either completely missing or belong to another realm
        List<Long> validRoleIds = validRoles.stream().map(Role::getId).toList();
        List<Long> notFoundRoleIds = request.getRoleIds().stream()
                .filter(id -> !validRoleIds.contains(id))
                .toList();

        if (!notFoundRoleIds.isEmpty()) {
            throw new EntityNotFoundException("Roles not found: " + notFoundRoleIds);
        }

        List<Long> existingRoleIds = userRoleRepository.findExistingRoleIds(userId, request.getRoleIds());
        if (!existingRoleIds.isEmpty()) {
            throw new IllegalArgumentException("User already has roles " + existingRoleIds);
        }

        // Get current user (assignedBy)
        final User assignedBy = SecurityUtils.getAuthenticatedUser(jwt -> {
            String keyCloakUserId = jwt.getSubject();
            if (keyCloakUserId != null) {
                return userRepository.findByKeycloakUserId(keyCloakUserId).orElse(null);
            }
            return null;
        });

        List<UserRole> userRoles = validRoles.stream().map(r -> UserRole.builder()
                .user(user)
                .role(r)
                .assignedBy(assignedBy)
                .expiryDate(request.getExpiryDate())
                .isActive(true)
                .build()).toList();

        userRoleRepository.saveAll(userRoles);
    }

    @Transactional
    // @RequireAnyRole(value = {"realm-admin", "group-admin"})
    public void removeRolesFromUser(String realmId, Long userId, RemoveUserRolesRequest request) {
        // Verify user exists in realm
        boolean userExists = userRepository.exists(
                Specification.allOf(UserSpecification.hasUserId(userId, realmId)));
        if (!userExists) {
            throw new EntityNotFoundException("User not found");
        }

        List<Long> existingRoleIds = userRoleRepository.findExistingRoleIds(userId, request.getRoleIds());
        if (existingRoleIds.size() != request.getRoleIds().size()) {
            request.getRoleIds().removeAll(new HashSet<>(existingRoleIds));
            throw new EntityNotFoundException("Roles are not assigned to this user: " + request.getRoleIds());
        }

        userRoleRepository.deleteByUserIdAndRoleIdIn(userId, existingRoleIds);
    }

    @Transactional(readOnly = true)
    public PagedResponse<UserRoleDTO> getUserRoles(String realmIdentifier, Long userId, UserRoleFilterRequest filter) {
        // 1. Check User Existence
        Specification<User> userSpecification = Specification
                .allOf(UserSpecification.hasUserId(userId, realmIdentifier));
        boolean userExists = userRepository.exists(userSpecification);
        if (!userExists) {
            throw new EntityNotFoundException("User not found");
        }

        // 2. Fetch Effective User Roles (Direct + Group)
        Specification<UserEffectiveRole> spec = Specification.allOf(
                UserEffectiveRoleSpecification.ofUser(userId, realmIdentifier),
                UserEffectiveRoleSpecification.hasRoleName(filter.getRoleName()),
                UserEffectiveRoleSpecification.hasRoleStatus(filter.getRoleStatus()),
                UserEffectiveRoleSpecification.hasUserStatus(filter.getUserStatus()),
                UserEffectiveRoleSpecification.assignedBy(filter.getAssignedBy()),
                UserEffectiveRoleSpecification.isActive(filter.getIsActive()),
                UserEffectiveRoleSpecification.assignedAtBefore(filter.getAssignedAtBefore()),
                UserEffectiveRoleSpecification.assignedAtAfter(filter.getAssignedAtAfter()),
                UserEffectiveRoleSpecification.expiryDateBefore(filter.getExpiryDateBefore()),
                UserEffectiveRoleSpecification.expiryDateAfter(filter.getExpiryDateAfter()),
                UserEffectiveRoleSpecification.assignmentType(filter.getAssignmentType()));

        return PagedResponse.fromPage(userEffectiveRoleRepository.findAll(spec, filter.toPageable()),
                UserRoleDTO::from);
    }

    @Transactional(readOnly = true)
    public boolean hasRole(String realmIdentifier, Long userId, Long roleId, String roleName) {
        if (roleId == null && (roleName == null || roleName.isEmpty())) {
            throw new IllegalArgumentException("Either roleId or roleName must be provided");
        }

        // Verify user exists
        userRepository.findOne(Specification.allOf(UserSpecification.hasUserId(userId, realmIdentifier)))
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // Check against Effective Roles
        Specification<UserEffectiveRole> spec = Specification.allOf(
                UserEffectiveRoleSpecification.ofUser(userId, realmIdentifier),
                UserEffectiveRoleSpecification.isNotExpired(),
                UserEffectiveRoleSpecification.isActive(true));

        if (roleId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("role").get("id"), roleId));
        } else {
            spec = spec.and(UserEffectiveRoleSpecification.hasRoleName(roleName));
        }

        return userEffectiveRoleRepository.exists(spec);
    }
}
