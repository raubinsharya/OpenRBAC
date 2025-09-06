package com.open.rbac.openrbac.services;

import com.open.rbac.openrbac.dtos.RealmDTO;
import com.open.rbac.openrbac.repositories.RealmRepository;
import com.open.rbac.openrbac.specifications.RealmSpecification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RealmService {

    private final RealmRepository realmRepository;

    public List<RealmDTO> getAllRealms(String status) {
        return realmRepository.findAll(RealmSpecification.hasStatus(status.toUpperCase()))
                .stream()
                .map(RealmDTO::from)
                .toList();
    }

    public Optional<RealmDTO> getRealmById(Long id) {
        return realmRepository.findById(id).map(RealmDTO::from);
    }

    public Optional<RealmDTO> getRealmByRealmId(String realmId) {
        return realmRepository.findByRealmId(realmId).map(RealmDTO::from);
    }
}