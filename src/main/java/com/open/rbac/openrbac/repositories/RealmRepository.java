package com.open.rbac.openrbac.repositories;

import com.open.rbac.openrbac.models.Realm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RealmRepository extends JpaRepository<Realm, Long>, JpaSpecificationExecutor<Realm> {
    Optional<Realm> findByRealmId(String realmId);
}