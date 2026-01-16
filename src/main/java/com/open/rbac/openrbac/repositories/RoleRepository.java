package com.open.rbac.openrbac.repositories;

import com.open.rbac.openrbac.models.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long>, JpaSpecificationExecutor<Role> {
    boolean existsByIdAndRealm_id(Long id, Long realmId);

    List<Role> findAllByIdInAndRealm_Id(Collection<Long> ids, Long realmId);
}