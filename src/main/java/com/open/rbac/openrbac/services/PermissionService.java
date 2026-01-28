package com.open.rbac.openrbac.services;

import com.open.rbac.openrbac.requestParams.PermissionFilterRequest;
import com.open.rbac.openrbac.requestParams.ResourceFilterRequest;
import com.open.rbac.openrbac.annotations.RequireAnyRole;
import com.open.rbac.openrbac.dtos.PagedResponse;
import com.open.rbac.openrbac.dtos.PermissionDTO;
import com.open.rbac.openrbac.enums.StandardAction;
import com.open.rbac.openrbac.models.Permission;
import com.open.rbac.openrbac.models.Realm;
import com.open.rbac.openrbac.repositories.PermissionRepository;
import com.open.rbac.openrbac.repositories.RealmRepository;
import com.open.rbac.openrbac.requests.StandardPermission;
import com.open.rbac.openrbac.specifications.BaseSpecification;
import com.open.rbac.openrbac.specifications.PermissionSpecification;
import jakarta.validation.Valid;
import com.open.rbac.openrbac.models.User;
import com.open.rbac.openrbac.repositories.UserRepository;
import com.open.rbac.openrbac.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PermissionService {

        private final PermissionRepository permissionRepository;
        private final RealmRepository realmRepository;
        private final UserRepository userRepository;

        @Transactional(readOnly = true)
        public PagedResponse<PermissionDTO> getAllPermissions(String realmIdentifier,
                        PermissionFilterRequest permissionFilterRequest) {
                Specification<Permission> spec = Specification
                                .allOf(PermissionSpecification.hasRealm(realmIdentifier))
                                .and(PermissionSpecification.hasStatus(permissionFilterRequest.getStatus())
                                                .and(PermissionSpecification.searchByNameIgnoreCase(
                                                                permissionFilterRequest.getName()))
                                                .and(BaseSpecification.withBaseFilters(permissionFilterRequest))
                                                .and(PermissionSpecification
                                                                .hasAction(permissionFilterRequest.getAction()))
                                                .and(PermissionSpecification
                                                                .hasResource(permissionFilterRequest.getResource()))
                                                .and(PermissionSpecification
                                                                .hasCreatedBy(permissionFilterRequest.getCreatedBy()))
                                                .and(PermissionSpecification.fetchWithCreatedBy()));
                return PagedResponse.fromPage(permissionRepository.findAll(spec, permissionFilterRequest.toPageable()),
                                PermissionDTO::from);
        }

        @Transactional(readOnly = true)
        public PermissionDTO getPermissionById(String realmIdentifier, Long permissionId) {
                Specification<Permission> spec = Specification
                                .allOf(PermissionSpecification.hasRealm(realmIdentifier))
                                .and(PermissionSpecification.hasId(permissionId))
                                .and(PermissionSpecification.fetchWithCreatedBy());
                return PermissionDTO.from(permissionRepository.findOne(spec).orElse(null));
        }

        @Retryable(retryFor = { ConnectException.class,
                        TimeoutException.class }, maxAttemptsExpression = "${retry.tenant.max-attempts}", backoff = @Backoff(delayExpression = "${retry.tenant.delay}", multiplierExpression = "${retry.tenant.multiplier}"))
        @RequireAnyRole(value = { "realm-admin" })
        @Transactional
        public Permission createPermission(String realmIdentifier, Permission permission) {
                Specification<Realm> spec = com.open.rbac.openrbac.specifications.RealmSpecification
                                .hasIdOrName(realmIdentifier);
                Realm realm = realmRepository.findOne(spec)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Realm " + realmIdentifier + " not found"));
                permission.setRealm(realm);

                final User createdBy = SecurityUtils.getAuthenticatedUser(jwt -> {
                        String sub = jwt.getSubject();
                        if (sub != null) {
                                return userRepository.findByKeycloakUserId(sub).orElse(null);
                        }
                        return null;
                });
                permission.setCreatedBy(createdBy);

                return permissionRepository.save(permission);
        }

        @Retryable(retryFor = { ConnectException.class,
                        TimeoutException.class }, maxAttemptsExpression = "${retry.tenant.max-attempts}", backoff = @Backoff(delayExpression = "${retry.tenant.delay}", multiplierExpression = "${retry.tenant.multiplier}"))
        @RequireAnyRole(value = { "realm-admin" })
        @Transactional
        public ArrayList<PermissionDTO> createStandardPermission(
                        String realmIdentifier,
                        @Valid StandardPermission standardPermission) {

                final User creator = SecurityUtils.getAuthenticatedUser(jwt -> {
                        String sub = jwt.getSubject();
                        if (sub != null) {
                                return userRepository.findByKeycloakUserId(sub).orElse(null);
                        }
                        return null;
                });

                Specification<Realm> spec = com.open.rbac.openrbac.specifications.RealmSpecification
                                .hasIdOrName(realmIdentifier);
                Realm realm = realmRepository.findOne(spec)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Realm " + realmIdentifier + " not found"));

                String resource = standardPermission.resource().toUpperCase();

                // 1. Build all standard permission names
                List<StandardAction> actions = List.of(
                                StandardAction.CREATE,
                                StandardAction.READ,
                                StandardAction.UPDATE,
                                StandardAction.DELETE);

                Map<String, StandardAction> permissionMap = actions.stream()
                                .collect(Collectors.toMap(
                                                a -> resource + ":" + a.name().toUpperCase(),
                                                a -> a));

                Set<String> permissionNames = permissionMap.keySet();

                // 2. Fetch existing permissions in ONE query
                Set<String> existing = permissionRepository
                                .findExistingNames(realm.getId(), permissionNames);

                // 3. Build only missing permissions
                List<Permission> toInsert = permissionMap.entrySet().stream()
                                .filter(e -> !existing.contains(e.getKey()))
                                .map(e -> {
                                        Permission p = new Permission();
                                        p.setRealm(realm);
                                        p.setCreatedBy(creator);
                                        p.setName(e.getKey());
                                        p.setResource(resource);
                                        p.setAction(e.getValue().name().toUpperCase());
                                        p.setDescription(
                                                        standardPermission.description() != null
                                                                        ? standardPermission.description()
                                                                        : "Allows " + p.getAction() + " on "
                                                                                        + resource);
                                        return p;
                                })
                                .toList();

                if (toInsert.isEmpty()) {
                        throw new RuntimeException("Resource not available for creation");
                }
                // 4. Batch insert (Hibernate will batch this)
                List<Permission> saved = permissionRepository.saveAll(toInsert);

                return saved.stream().map(PermissionDTO::from).collect(Collectors.toCollection(ArrayList::new));
        }

        public PagedResponse<String> getResources(Long realmId, ResourceFilterRequest resourceFilterRequest) {
                resourceFilterRequest.setSortBy("resource");
                var resources = permissionRepository.findDistinctResources(
                                realmId,
                                resourceFilterRequest.toPageable());
                return PagedResponse.fromPage(resources, String::valueOf);
        }

        public PagedResponse<String> getActions(Long realmId, ResourceFilterRequest resourceFilterRequest) {
                resourceFilterRequest.setSortBy("action");
                var resources = permissionRepository.findDistinctActions(
                                realmId,
                                resourceFilterRequest.toPageable());
                return PagedResponse.fromPage(resources, String::valueOf);
        }
}