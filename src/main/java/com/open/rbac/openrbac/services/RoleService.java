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
import com.open.rbac.openrbac.requests.UpdateRoleRequest;
import com.open.rbac.openrbac.specifications.BaseSpecification;
import com.open.rbac.openrbac.specifications.RoleSpecification;
import com.open.rbac.openrbac.utils.SecurityUtils;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import lombok.RequiredArgsConstructor;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.ConnectException;
import java.util.Optional;
import java.util.concurrent.TimeoutException;

@Service
@RequiredArgsConstructor
@Transactional
public class RoleService {

    private final RoleRepository roleRepository;
    private final RealmRepository realmRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public PagedResponse<RoleDTO> getAllRoles(String realmIdentifier, RoleFilterRequest roleFilterRequest) {
        Specification<Role> spec = Specification.allOf(RoleSpecification.hasRealm(realmIdentifier))
                .and(RoleSpecification.searchByNameIgnoreCase(roleFilterRequest.getName()))
                .and(RoleSpecification.hasStatus(roleFilterRequest.getStatus()))
                .and(RoleSpecification.hasCreatedBy(roleFilterRequest.getCreatedBy()))
                .and(BaseSpecification.withBaseFilters(roleFilterRequest))
                .and(RoleSpecification.isSystemRole(roleFilterRequest.getIsSystemRole()))
                .and(RoleSpecification.fetchWithCreatedBy());
        return PagedResponse.fromPage(roleRepository.findAll(spec, roleFilterRequest.toPageable()), RoleDTO::from);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "roles", key = "#realmIdentifier + '-' + #id")
    public RoleDTO getRoleById(Long id, String realmIdentifier) {
        Specification<Role> specification = Specification.allOf(RoleSpecification.hasRealm(realmIdentifier))
                .and(RoleSpecification.hasId(id))
                .and(RoleSpecification.fetchWithCreatedBy());
        return RoleDTO.from(roleRepository.findOne(specification).orElse(null));
    }

    @Retryable(retryFor = { ConnectException.class,
            TimeoutException.class }, maxAttemptsExpression = "${retry.tenant.max-attempts}", backoff = @Backoff(delayExpression = "${retry.tenant.delay}", multiplierExpression = "${retry.tenant.multiplier}"))
    @RequireAnyRole(value = { "realm-admin" })
    public Role createRole(String realmIdentifier, Role role) {
        Specification<com.open.rbac.openrbac.models.Realm> spec = com.open.rbac.openrbac.specifications.RealmSpecification
                .hasIdOrName(realmIdentifier);
        var realm = realmRepository.findOne(spec)
                .orElseThrow(() -> new IllegalArgumentException("Realm " + realmIdentifier + " not found"));
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

    @Retryable(retryFor = { ConnectException.class,
            TimeoutException.class }, maxAttemptsExpression = "${retry.tenant.max-attempts}", backoff = @Backoff(delayExpression = "${retry.tenant.delay}", multiplierExpression = "${retry.tenant.multiplier}"))
    @RequireAnyRole(value = { "realm-admin" })
    @Transactional
    @CacheEvict(value = "roles", key = "#realmIdentifier + '-' + #id")
    public RoleDTO updateRole(String realmIdentifier, Long id, UpdateRoleRequest updateData) {
        Role existing = getRoleOrThrow(realmIdentifier, id);

        // Usually prevent updates to system roles via basic endpoints
        if (existing.getIsSystemRole()) {
            throw new IllegalStateException("System roles cannot be modified directly");
        }

        existing.setName(updateData.name());
        existing.setDescription(updateData.description());
        if (updateData.status() != null) {
            existing.setStatus(updateData.status());
        }

        Role saved = roleRepository.save(existing);
        return RoleDTO.from(saved);
    }

    @Retryable(retryFor = { ConnectException.class,
            TimeoutException.class }, maxAttemptsExpression = "${retry.tenant.max-attempts}", backoff = @Backoff(delayExpression = "${retry.tenant.delay}", multiplierExpression = "${retry.tenant.multiplier}"))
    @RequireAnyRole(value = { "realm-admin" })
    @Transactional
    @CacheEvict(value = "roles", key = "#realmIdentifier + '-' + #id")
    public RoleDTO patchRole(String realmIdentifier, Long id, UpdateRoleRequest patchData) {
        Role existing = getRoleOrThrow(realmIdentifier, id);

        if (existing.getIsSystemRole()) {
            throw new IllegalStateException("System roles cannot be modified directly");
        }

        Optional.ofNullable(patchData.name()).ifPresent(existing::setName);
        Optional.ofNullable(patchData.description()).ifPresent(existing::setDescription);
        Optional.ofNullable(patchData.status()).ifPresent(existing::setStatus);

        Role saved = roleRepository.save(existing);
        return RoleDTO.from(saved);
    }

    private Role getRoleOrThrow(String realmIdentifier, Long id) {
        Specification<Role> spec = Specification.allOf(RoleSpecification.hasRealm(realmIdentifier))
                .and(RoleSpecification.hasId(id));
        return roleRepository.findOne(spec)
                .orElseThrow(() -> new EntityNotFoundException("Role not found with id: " + id));
    }
}