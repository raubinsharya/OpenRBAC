package com.open.rbac.openrbac.services;

import com.open.rbac.openrbac.requestParams.RoleFilterRequest;
import com.open.rbac.openrbac.annotations.RequireAnyRole;
import com.open.rbac.openrbac.dtos.PagedResponse;
import com.open.rbac.openrbac.dtos.RoleDTO;
import com.open.rbac.openrbac.models.Role;
import com.open.rbac.openrbac.repositories.RealmRepository;
import com.open.rbac.openrbac.repositories.RoleRepository;
import com.open.rbac.openrbac.models.User;
import com.open.rbac.openrbac.repositories.UserRepository;
import com.open.rbac.openrbac.specifications.BaseSpecification;
import com.open.rbac.openrbac.specifications.RoleSpecification;
import com.open.rbac.openrbac.utils.SecurityUtils;

import lombok.RequiredArgsConstructor;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.ConnectException;
import java.util.concurrent.TimeoutException;

@Service
@RequiredArgsConstructor
@Transactional
public class RoleService {

    private final RoleRepository roleRepository;
    private final RealmRepository realmRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public PagedResponse<RoleDTO> getAllRoles(Long realmId, RoleFilterRequest roleFilterRequest) {
        Specification<Role> spec = Specification.allOf(RoleSpecification.hasRealm(realmId))
                .and(RoleSpecification.searchByNameIgnoreCase(roleFilterRequest.getName()))
                .and(RoleSpecification.hasStatus(roleFilterRequest.getStatus()))
                .and(RoleSpecification.hasCreatedBy(roleFilterRequest.getCreatedBy()))
                .and(BaseSpecification.withBaseFilters(roleFilterRequest))
                .and(RoleSpecification.isSystemRole(roleFilterRequest.getIsSystemRole()))
                .and(RoleSpecification.fetchWithCreatedBy());
        return PagedResponse.fromPage(roleRepository.findAll(spec, roleFilterRequest.toPageable()), RoleDTO::from);
    }

    @Transactional(readOnly = true)
    public RoleDTO getRoleById(Long id, Long realmId) {
        Specification<Role> specification = Specification.allOf(RoleSpecification.hasRealm(realmId))
                .and(RoleSpecification.hasId(id));
        return RoleDTO.from(roleRepository.findOne(specification).orElse(null));
    }

    @Retryable(retryFor = { ConnectException.class,
            TimeoutException.class }, maxAttemptsExpression = "${retry.tenant.max-attempts}", backoff = @Backoff(delayExpression = "${retry.tenant.delay}", multiplierExpression = "${retry.tenant.multiplier}"))
    @RequireAnyRole(value = { "realm-admin" })
    public Role createRole(long realmId, Role role) {
        var realm = realmRepository.findById(realmId)
                .orElseThrow(() -> new IllegalArgumentException("Realm id " + realmId + " not found"));
        role.setRealm(realm);

        User creator = SecurityUtils.getAuthenticatedUser(jwt -> {
            String sub = jwt.getSubject();
            if (sub != null) {
                return userRepository.findByKeycloakUserId(sub).orElse(null);
            }
            return null;
        });
        role.setCreatedBy(creator);

        return roleRepository.save(role);
    }
}