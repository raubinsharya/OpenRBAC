package com.open.rbac.openrbac.services;

import com.open.rbac.openrbac.dtos.PagedResponse;
import com.open.rbac.openrbac.dtos.PagedResponseMapper;
import com.open.rbac.openrbac.dtos.RealmDTO;
import com.open.rbac.openrbac.models.Realm;
import com.open.rbac.openrbac.repositories.RealmRepository;
import com.open.rbac.openrbac.specifications.RealmSpecification;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;
import org.springframework.data.jpa.domain.Specification;

@Service
@RequiredArgsConstructor
public class RealmService {

    private final RealmRepository realmRepository;

    public PagedResponse<RealmDTO> getAllRealms(String status, int page, int size) {
        Specification<Realm> spec = Specification
                .allOf(RealmSpecification.hasStatus(status));
        Pageable pageable = PageRequest.of(page, size);
        return PagedResponseMapper.fromPage(this.realmRepository.findAll(spec, pageable), RealmDTO::from);
    }

    public Optional<RealmDTO> getRealmById(Long id) {
        return realmRepository.findById(id).map(RealmDTO::from);
    }

    public Optional<RealmDTO> getRealmByRealmId(String realmId) {
        return realmRepository.findByRealmId(realmId).map(RealmDTO::from);
    }
}