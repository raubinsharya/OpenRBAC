package com.open.rbac.openrbac.services;

import com.open.rbac.openrbac.dtos.PagedResponse;
import com.open.rbac.openrbac.dtos.PagedResponseMapper;
import com.open.rbac.openrbac.dtos.RoleDTO;
import com.open.rbac.openrbac.models.Role;
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

    @Transactional(readOnly = true)
    public PagedResponse<RoleDTO> getAllRoles(String status, Boolean isSystemRole, int page, int size) {
        Specification<Role> spec = Specification
                .allOf(RoleSpecification.hasStatus(status))
                .and(RoleSpecification.isSystemRole(isSystemRole));
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.max(size, 0));
        return PagedResponseMapper.fromPage(roleRepository.findAll(spec, pageable), RoleDTO::from);
    }

    @Transactional(readOnly = true)
    public RoleDTO getRoleById(Long id) {
        return roleRepository.findById(id).map(RoleDTO::from).orElse(null);
    }

}