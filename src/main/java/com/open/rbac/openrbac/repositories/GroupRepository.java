package com.open.rbac.openrbac.repositories;

import com.open.rbac.openrbac.models.Group;
import com.open.rbac.openrbac.models.Realm;
import com.open.rbac.openrbac.repositories.custom.GroupRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GroupRepository
    extends JpaRepository<Group, Long>, JpaSpecificationExecutor<Group>, GroupRepositoryCustom {
  Optional<Group> findByIdAndRealm(Long id, Realm realm);

  Optional<Group> findByIdAndRealm_Id(Long id, Long realmId);

  boolean existsByIdAndRealm_Id(Long id, Long realmId);

}