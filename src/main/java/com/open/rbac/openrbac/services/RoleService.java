package com.open.rbac.openrbac.services;

import com.open.rbac.openrbac.RequestParams.RoleFilterRequest;
import com.open.rbac.openrbac.annotations.RequireAnyRole;
import com.open.rbac.openrbac.dtos.PagedResponse;
import com.open.rbac.openrbac.dtos.RoleDTO;
import com.open.rbac.openrbac.models.Realm;
import com.open.rbac.openrbac.models.Role;
import com.open.rbac.openrbac.repositories.RealmRepository;
import com.open.rbac.openrbac.repositories.RoleRepository;
import com.open.rbac.openrbac.specifications.RoleSpecification;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class RoleService {

    private final RoleRepository roleRepository;
    private final RealmRepository realmRepository;

    @Transactional(readOnly = true)
    public PagedResponse<RoleDTO> getAllRoles(String status, Boolean isSystemRole, int page, int size) {
        Specification<Role> spec = Specification
                .allOf(RoleSpecification.hasStatus(status))
                .and(RoleSpecification.isSystemRole(isSystemRole));
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.max(size, 0));
        return PagedResponse.fromPage(roleRepository.findAll(spec, pageable), RoleDTO::from);
    }

    @Transactional(readOnly = true)
    public RoleDTO getRoleById(Long id) {
        return roleRepository.findById(id).map(RoleDTO::from).orElse(null);
    }

    public Role createRole(Long realmId, Role role) {
        Realm realm = realmRepository.findById(realmId).orElseThrow(() -> new IllegalArgumentException("Realm not found"));
        role.setRealm(realm);
        return roleRepository.save(role);
    }

    @RequireAnyRole(value = {"realm-admin"})
    @Transactional(readOnly = true)
    public PagedResponse<RoleDTO> searchRoles(Long realmId, RoleFilterRequest filter) {
        Specification<Role> spec = Specification.allOf(RoleSpecification.hasRealm(realmId))
                .and(RoleSpecification.searchByNameIgnoreCase(filter.getName()))
                .and(RoleSpecification.hasStatus(filter.getStatus()))
                .and(RoleSpecification.hasCreatedAfter(filter.getCreatedAfter()))
                .and(RoleSpecification.hasCreatedBefore(filter.getCreatedBefore()))
                .and(RoleSpecification.hasUpdatedBefore(filter.getUpdatedBefore()))
                .and(RoleSpecification.hasUpdatedAfter(filter.getUpdatedAfter()))
                .and(RoleSpecification.isSystemRole(filter.getIsSystemRole()));
        return PagedResponse.fromPage(roleRepository.findAll(spec, filter.toPageable()), RoleDTO::from);
    }
}