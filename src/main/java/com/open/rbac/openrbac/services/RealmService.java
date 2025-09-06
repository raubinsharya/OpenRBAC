package com.open.rbac.openrbac.services;

import com.open.rbac.openrbac.dtos.RealmDTO;
import com.open.rbac.openrbac.models.Realm;
import com.open.rbac.openrbac.repositories.RealmRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RealmService {

    private final RealmRepository realmRepository;

    public List<RealmDTO> getAllRealms() {
        return realmRepository.findAll()
                .stream()
                .map(RealmDTO::from)
                .toList();
    }

    public List<RealmDTO> getAllRealmsWithUsers() {

        return realmRepository.findAllWithUsers()
                .stream()
                .map(RealmDTO::fromWithUsers)
                .toList();

    }

    public Optional<RealmDTO> getRealmById(Long id) {
        return realmRepository.findById(id).map(RealmDTO::from);
    }

    public Optional<Realm> getRealmByRealmId(String realmId) {
        return realmRepository.findByRealmId(realmId);
    }

    public Optional<RealmDTO> getRealmWithUsers(Long realmId) {
        return realmRepository.findByIdWithUsers(realmId).map(RealmDTO::fromWithUsers);
    }

    public Optional<RealmDTO> getRealmWithUsersDTO(Long realmId) {
      return realmRepository.findByIdWithUsers(realmId).map(RealmDTO::fromWithUsers);
     }

    public Optional<RealmDTO> getRealmDTO(Long realmId) {
        return realmRepository.findById(realmId)
                .map(RealmDTO::from);
    }

    public Optional<RealmDTO> getRealmDTOByRealmId(String realmId) {
        return realmRepository.findByRealmId(realmId)
                .map(RealmDTO::from);
    }

    public Optional<RealmDTO> getRealmWithUsersDTOByRealmId(String realmId) {
        Optional<Realm> realmOpt = realmRepository.findByRealmIdWithUsers(realmId);
        return realmOpt.map(RealmDTO::fromWithUsers);
    }
}