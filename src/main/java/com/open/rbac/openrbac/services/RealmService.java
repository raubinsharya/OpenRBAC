package com.open.rbac.openrbac.services;

import com.open.rbac.openrbac.dtos.PagedResponse;
import com.open.rbac.openrbac.dtos.RealmDTO;
import com.open.rbac.openrbac.dtos.UserDTO;
import com.open.rbac.openrbac.models.Realm;
import com.open.rbac.openrbac.models.User;
import com.open.rbac.openrbac.repositories.RealmRepository;
import com.open.rbac.openrbac.repositories.UserRepository;
import com.open.rbac.openrbac.specifications.RealmSpecification;

import com.open.rbac.openrbac.specifications.UserSpecification;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.jpa.domain.Specification;

@Service
@RequiredArgsConstructor
public class RealmService {

    private final RealmRepository realmRepository;
    private final UserRepository userRepository;

    public PagedResponse<RealmDTO> getAllRealms(String status, int page, int size) {
        Specification<Realm> spec = Specification
                .allOf(RealmSpecification.hasStatus(status));
        Pageable pageable = PageRequest.of(page, size);
        return PagedResponse.fromPage(this.realmRepository.findAll(spec, pageable), RealmDTO::from);
    }

    public Optional<RealmDTO> getRealmById(Long id, boolean includeUsers, boolean includeRoles, boolean includePermissions) {
        Specification<Realm> spec = Specification.allOf(RealmSpecification.hasId(id))
                .and(RealmSpecification.includeUsers(includeUsers))
                .and(RealmSpecification.includeRoles(includeRoles))
                .and(RealmSpecification.includePermissions(includePermissions));
        return realmRepository.findAll(spec).stream().findFirst().map(r -> RealmDTO.from(r, includeUsers, includeRoles, includePermissions));
    }

    public Optional<RealmDTO> getRealmByRealmId(String realmId, boolean includeUsers, boolean includeRoles, boolean includePermissions) {
        Specification<Realm> spec = Specification.allOf(RealmSpecification.hasRealmId(realmId))
                .and(RealmSpecification.includeUsers(includeUsers))
                .and(RealmSpecification.includeRoles(includeRoles))
                .and(RealmSpecification.includePermissions(includePermissions));
        return realmRepository.findAll(spec).stream().findFirst().map(r -> RealmDTO.from(r, includeUsers, includeRoles, includePermissions));
    }
}