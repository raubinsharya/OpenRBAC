package com.open.rbac.openrbac.services;

import com.open.rbac.openrbac.annotations.RequireAnyRole;
import com.open.rbac.openrbac.dtos.PagedResponse;
import com.open.rbac.openrbac.dtos.UserRoleDTO;
import com.open.rbac.openrbac.models.Role;
import com.open.rbac.openrbac.models.User;
import com.open.rbac.openrbac.models.UserRole;
import com.open.rbac.openrbac.repositories.RoleRepository;
import com.open.rbac.openrbac.repositories.UserRoleRepository;
import com.open.rbac.openrbac.repositories.UserRepository;
import com.open.rbac.openrbac.requests.AddUserRolesRequest;
import com.open.rbac.openrbac.requests.RemoveUserRolesRequest;
import com.open.rbac.openrbac.requestParams.UserRoleFilterRequest;
import com.open.rbac.openrbac.specifications.UserRoleSpecification;
import com.open.rbac.openrbac.specifications.UserSpecification;
import com.open.rbac.openrbac.utils.SecurityUtils;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserRoleService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;

    @Transactional
    @RequireAnyRole(value = { "realm-admin", "group-admin" })
    public void addRolesToUser(Long realmId, Long userId, AddUserRolesRequest request) {
        User user = userRepository.findOne(Specification.allOf(
                UserSpecification.hasUserId(userId, realmId)))
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        final List<Role> requestedRoles = roleRepository.findAllById(request.getRoleIds());
        // Verify all roles belong to the same realm
        List<Role> validRoles = requestedRoles.stream()
                .filter(r -> r.getRealm().getId().equals(realmId))
                .toList();

        if (validRoles.size() != request.getRoleIds().size()) {
            throw new EntityNotFoundException("Roles not found or invalid for this realm");
        }

        List<Long> existingRoleIds = userRoleRepository.findExistingRoleIds(userId, request.getRoleIds());
        if (!existingRoleIds.isEmpty()) {
            throw new IllegalArgumentException("User already has roles " + existingRoleIds);
        }

        // Get current user (assignedBy)
        final User assignedBy = SecurityUtils.getAuthenticatedUser(jwt -> {
            String username = jwt.getClaimAsString("preferred_username");
            if (username != null) {
                return userRepository.findByUsername(username).orElse(null);
            }
            return null;
        });

        List<UserRole> userRoles = validRoles.stream().map(r -> UserRole.builder()
                .user(user)
                .role(r)
                .assignedBy(assignedBy)
                .isActive(true)
                .build()).toList();

        userRoleRepository.saveAll(userRoles);
    }

    @Transactional
    @RequireAnyRole(value = { "realm-admin", "group-admin" })
    public void removeRolesFromUser(Long realmId, Long userId, RemoveUserRolesRequest request) {
        // Verify user exists in realm
        boolean userExists = userRepository.exists(Specification.allOf(UserSpecification.hasUserId(userId, realmId)));
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
    public PagedResponse<UserRoleDTO> getUserRoles(Long realmId, Long userId, UserRoleFilterRequest filter) {
        boolean userExists = userRepository.exists(Specification.allOf(UserSpecification.hasUserId(userId, realmId)));
        if (!userExists) {
            throw new EntityNotFoundException("User not found");
        }

        Specification<UserRole> spec = Specification.allOf(
                UserRoleSpecification.ofUser(userId, realmId),
                UserRoleSpecification.hasRoleName(filter.getRoleName()),
                UserRoleSpecification.hasRoleStatus(filter.getRoleStatus()),
                UserRoleSpecification.hasUserStatus(filter.getUserStatus()),
                UserRoleSpecification.assignedBy(filter.getAssignedBy()),
                UserRoleSpecification.isActive(filter.getIsActive()),
                UserRoleSpecification.assignedAtBefore(filter.getAssignedAtBefore()),
                UserRoleSpecification.assignedAtAfter(filter.getAssignedAtAfter()),
                UserRoleSpecification.expiryDateBefore(filter.getExpiryDateBefore()),
                UserRoleSpecification.expiryDateAfter(filter.getExpiryDateAfter()));

        return PagedResponse.fromPage(userRoleRepository.findAll(spec, filter.toPageable()), UserRoleDTO::from);
    }
}
