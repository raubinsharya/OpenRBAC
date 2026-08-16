package com.open.rbac.openrbac.services;

import com.open.rbac.openrbac.requestParams.RealmFilterRequest;
import com.open.rbac.openrbac.dtos.PagedResponse;
import com.open.rbac.openrbac.dtos.RealmDTO;
import com.open.rbac.openrbac.models.Realm;
import com.open.rbac.openrbac.repositories.RealmRepository;
import com.open.rbac.openrbac.specifications.BaseSpecification;
import com.open.rbac.openrbac.specifications.RealmSpecification;
import com.open.rbac.openrbac.utils.SecurityUtils;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

import org.springframework.data.jpa.domain.Specification;

@Service
@RequiredArgsConstructor
public class RealmService {

    private final RealmRepository realmRepository;

    public PagedResponse<RealmDTO> getAllRealms(Jwt jwt, String status, RealmFilterRequest realmFilterRequest) {
        Specification<Realm> spec = RealmSpecification.hasStatus(status)
                .and(RealmSpecification.searchByNameIgnoreCase(realmFilterRequest.getName()))
                .and(BaseSpecification.withBaseFilters(realmFilterRequest));

        spec = applyAccessControl(jwt, spec);

        return PagedResponse.fromPage(realmRepository.findAll(spec, realmFilterRequest.toPageable()),
                RealmDTO::from);
    }

    // NOTE: @Cacheable is intentionally omitted — the JWT determines which realms are
    // accessible per role. Caching without user identity in the key would bypass security.
    public Optional<RealmDTO> getRealmById(Jwt jwt, Long id, boolean includeUsers, boolean includeRoles,
            boolean includePermissions) {
        Specification<Realm> spec = applyAccessControl(jwt, RealmSpecification.hasId(id)
                .and(RealmSpecification.includeUsers(includeUsers))
                .and(RealmSpecification.includeRoles(includeRoles))
                .and(RealmSpecification.includePermissions(includePermissions)));

        return realmRepository.findAll(spec).stream().findFirst()
                .map(realm -> RealmDTO.from(realm, includeUsers, includeRoles, includePermissions));
    }

    // NOTE: @Cacheable is intentionally omitted — same reason as getRealmById.
    public Optional<RealmDTO> getRealmByRealmId(Jwt jwt, String realmId, boolean includeUsers, boolean includeRoles,
            boolean includePermissions) {
        Specification<Realm> spec = applyAccessControl(jwt, RealmSpecification.hasRealmId(realmId)
                .and(RealmSpecification.includeUsers(includeUsers))
                .and(RealmSpecification.includeRoles(includeRoles))
                .and(RealmSpecification.includePermissions(includePermissions)));

        return realmRepository.findAll(spec).stream().findFirst()
                .map(realm -> RealmDTO.from(realm, includeUsers, includeRoles, includePermissions));
    }

    /**
     * Applies realm-level access control to the given specification based on the caller's JWT role.
     * <ul>
     *   <li><b>platform-admin</b>: unrestricted — all realms visible</li>
     *   <li><b>realm-admin</b>: scoped to realms where the user has a record (by username/email)</li>
     *   <li>Regular user: scoped to the single realm from the JWT issuer claim</li>
     * </ul>
     */
    private Specification<Realm> applyAccessControl(Jwt jwt, Specification<Realm> spec) {
        if (SecurityUtils.isPlatformAdmin(jwt)) {
            return spec;
        }
        if (SecurityUtils.isRealmAdmin(jwt)) {
            String username = jwt.getClaim("preferred_username");
            String email = jwt.getClaim("email");
            return spec.and(RealmSpecification.hasUserWithUsernameOrEmail(username, email));
        }
        // Regular user: restrict to the realm from their token issuer
        String userRealm = SecurityUtils.extractRealmFromJwt(jwt);
        if (userRealm == null) {
            return spec.and((root, query, cb) -> cb.disjunction()); // deny all — cannot determine realm
        }
        return spec.and(RealmSpecification.hasIdOrName(userRealm));
    }
}