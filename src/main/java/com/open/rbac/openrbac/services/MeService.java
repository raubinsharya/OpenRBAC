package com.open.rbac.openrbac.services;

import com.open.rbac.openrbac.requestParams.PermissionFilterRequest;
import com.open.rbac.openrbac.requestParams.RoleFilterRequest;
import com.open.rbac.openrbac.dtos.PagedResponse;
import com.open.rbac.openrbac.dtos.PermissionDTO;
import com.open.rbac.openrbac.dtos.RoleDTO;
import com.open.rbac.openrbac.dtos.UserDTO;
import com.open.rbac.openrbac.models.Permission;
import com.open.rbac.openrbac.models.Role;
import com.open.rbac.openrbac.models.User;
import com.open.rbac.openrbac.repositories.PermissionRepository;
import com.open.rbac.openrbac.repositories.RoleRepository;
import com.open.rbac.openrbac.repositories.UserRepository;
import com.open.rbac.openrbac.specifications.BaseSpecification;
import com.open.rbac.openrbac.specifications.PermissionSpecification;
import com.open.rbac.openrbac.specifications.RoleSpecification;
import com.open.rbac.openrbac.specifications.UserSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MeService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public Optional<UserDTO> getUser(String username, boolean includeRealm) {
        Specification<User> userSpecification = Specification.allOf(
                UserSpecification.hasStatus("active"),
                UserSpecification.includeRealm(includeRealm));
        return userRepository.findAll(userSpecification).stream().findFirst().map(u -> UserDTO.from(u, includeRealm));
    }

    public PagedResponse<RoleDTO> getMeRoles(String userName, RoleFilterRequest filter) {
        Specification<Role> spec = Specification.allOf(RoleSpecification.ofUser(userName))
                .and(RoleSpecification.searchByNameIgnoreCase(filter.getName()))
                .and(RoleSpecification.hasStatus(filter.getStatus()))
                .and(RoleSpecification.isSystemRole(filter.getIsSystemRole())
                        .and(BaseSpecification.withBaseFilters(filter)));
        Pageable pageable = filter.toPageable();
        return PagedResponse.fromPage(roleRepository.findAll(spec, pageable), RoleDTO::from);
    }

    public PagedResponse<PermissionDTO> getMePermissions(String userName, PermissionFilterRequest filter) {
        Specification<Permission> spec = Specification.allOf(PermissionSpecification.ofUser(userName))
                .and(PermissionSpecification.searchByNameIgnoreCase(filter.getName()))
                .and(PermissionSpecification.hasStatus(filter.getStatus()))
                .and(PermissionSpecification.hasResource(filter.getResource()))
                .and(PermissionSpecification.hasAction(filter.getAction()))
                .and(BaseSpecification.withBaseFilters(filter));
        Pageable pageable = filter.toPageable();
        return PagedResponse.fromPage(permissionRepository.findAll(spec, pageable), PermissionDTO::from);
    }

    public List<RoleDTO> getMeRoles(String userName) {
        Specification<Role> spec = Specification.allOf(RoleSpecification.ofUser(userName));
        return roleRepository.findAll(spec).stream().map(RoleDTO::from).collect(Collectors.toList());
    }

    public List<PermissionDTO> getMePermissions(String userName) {
        Specification<Permission> spec = Specification.allOf(PermissionSpecification.ofUser(userName));
        return permissionRepository.findAll(spec).stream().map(PermissionDTO::from).collect(Collectors.toList());
    }
}
