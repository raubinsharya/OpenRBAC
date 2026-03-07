package com.open.rbac.openrbac.services;

import com.open.rbac.openrbac.requestParams.RealmFilterRequest;
import com.open.rbac.openrbac.dtos.PagedResponse;
import com.open.rbac.openrbac.dtos.RealmDTO;
import com.open.rbac.openrbac.models.Realm;
import com.open.rbac.openrbac.repositories.RealmRepository;
import com.open.rbac.openrbac.repositories.UserRepository;
import com.open.rbac.openrbac.specifications.BaseSpecification;
import com.open.rbac.openrbac.specifications.RealmSpecification;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

import org.springframework.data.jpa.domain.Specification;

@Service
@RequiredArgsConstructor
public class RealmService {

    private final RealmRepository realmRepository;
    private final UserRepository userRepository;

    public PagedResponse<RealmDTO> getAllRealms(String status, RealmFilterRequest realmFilterRequest) {
        Specification<Realm> spec = Specification
                .allOf(RealmSpecification.hasStatus(status))
                .and(RealmSpecification.searchByNameIgnoreCase(realmFilterRequest.getName()))
                .and(BaseSpecification.withBaseFilters(realmFilterRequest));
        return PagedResponse.fromPage(this.realmRepository.findAll(spec, realmFilterRequest.toPageable()),
                RealmDTO::from);
    }

    @Cacheable(value = "realms", key = "#id + '-' + #includeUsers + '-' + #includeRoles + '-' + #includePermissions")
    public Optional<RealmDTO> getRealmById(Long id, boolean includeUsers, boolean includeRoles,
            boolean includePermissions) {
        Specification<Realm> spec = Specification.allOf(RealmSpecification.hasId(id))
                .and(RealmSpecification.includeUsers(includeUsers))
                .and(RealmSpecification.includeRoles(includeRoles))
                .and(RealmSpecification.includePermissions(includePermissions));
        return realmRepository.findAll(spec).stream().findFirst()
                .map(r -> RealmDTO.from(r, includeUsers, includeRoles, includePermissions));
    }

    @Cacheable(value = "realms", key = "#realmId + '-' + #includeUsers + '-' + #includeRoles + '-' + #includePermissions")
    public Optional<RealmDTO> getRealmByRealmId(String realmId, boolean includeUsers, boolean includeRoles,
            boolean includePermissions) {
        Specification<Realm> spec = Specification.allOf(RealmSpecification.hasRealmId(realmId))
                .and(RealmSpecification.includeUsers(includeUsers))
                .and(RealmSpecification.includeRoles(includeRoles))
                .and(RealmSpecification.includePermissions(includePermissions));
        return realmRepository.findAll(spec).stream().findFirst()
                .map(r -> RealmDTO.from(r, includeUsers, includeRoles, includePermissions));
    }
}