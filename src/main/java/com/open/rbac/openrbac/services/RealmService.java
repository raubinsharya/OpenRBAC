package com.open.rbac.openrbac.services;

import com.open.rbac.openrbac.requestParams.RealmFilterRequest;
import com.open.rbac.openrbac.dtos.PagedResponse;
import com.open.rbac.openrbac.dtos.RealmDTO;
import com.open.rbac.openrbac.models.Realm;
import com.open.rbac.openrbac.repositories.RealmRepository;
import com.open.rbac.openrbac.specifications.BaseSpecification;
import com.open.rbac.openrbac.specifications.RealmSpecification;
import com.open.rbac.openrbac.utils.SecurityUtils;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

import org.springframework.data.jpa.domain.Specification;

@Service
@RequiredArgsConstructor
public class RealmService {

    private final RealmRepository realmRepository;

    public PagedResponse<RealmDTO> getAllRealms(Jwt jwt, String status, RealmFilterRequest realmFilterRequest) {
        boolean isAdmin = SecurityUtils.isRealmAdmin(jwt);
        String userRealm = SecurityUtils.extractRealmFromJwt(jwt);

        Specification<Realm> spec = Specification
                .allOf(RealmSpecification.hasStatus(status))
                .and(RealmSpecification.searchByNameIgnoreCase(realmFilterRequest.getName()))
                .and(BaseSpecification.withBaseFilters(realmFilterRequest));

        // Non-admins only see the realm they belong to (from their token issuer)
        if (!isAdmin && userRealm != null) {
            spec = spec.and(RealmSpecification.hasIdOrName(userRealm));
        }

        return PagedResponse.fromPage(this.realmRepository.findAll(spec, realmFilterRequest.toPageable()),
                RealmDTO::from);
    }

    @Cacheable(value = "realms", key = "#id + '-' + #includeUsers + '-' + #includeRoles + '-' + #includePermissions")
    public Optional<RealmDTO> getRealmById(Jwt jwt, Long id, boolean includeUsers, boolean includeRoles,
            boolean includePermissions) {
        Specification<Realm> spec = Specification.allOf(RealmSpecification.hasId(id))
                .and(RealmSpecification.includeUsers(includeUsers))
                .and(RealmSpecification.includeRoles(includeRoles))
                .and(RealmSpecification.includePermissions(includePermissions));

        return realmRepository.findAll(spec).stream().findFirst()
                .map(realm -> {
                    // Non-admins may only view their own realm
                    if (!SecurityUtils.isRealmAdmin(jwt)) {
                        String userRealm = SecurityUtils.extractRealmFromJwt(jwt);
                        if (userRealm == null || !realm.getName().equalsIgnoreCase(userRealm)) {
                            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                                    "Access denied: you do not have permission to view this realm");
                        }
                    }
                    return RealmDTO.from(realm, includeUsers, includeRoles, includePermissions);
                });
    }

    @Cacheable(value = "realms", key = "#realmId + '-' + #includeUsers + '-' + #includeRoles + '-' + #includePermissions")
    public Optional<RealmDTO> getRealmByRealmId(Jwt jwt, String realmId, boolean includeUsers, boolean includeRoles,
            boolean includePermissions) {

        Specification<Realm> spec = Specification.allOf(RealmSpecification.hasRealmId(realmId))
                .and(RealmSpecification.includeUsers(includeUsers))
                .and(RealmSpecification.includeRoles(includeRoles))
                .and(RealmSpecification.includePermissions(includePermissions));
        return realmRepository.findAll(spec).stream().findFirst()
                .map(realm -> {
                    // Non-admins may only view their own realm
                    if (!SecurityUtils.isRealmAdmin(jwt)) {
                        String userRealm = SecurityUtils.extractRealmFromJwt(jwt);
                        if (userRealm == null || !realm.getName().equalsIgnoreCase(userRealm)) {
                            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                                    "Access denied: you do not have permission to view this realm");
                        }
                    }
                    return RealmDTO.from(realm, includeUsers, includeRoles, includePermissions);
                });
    }
}