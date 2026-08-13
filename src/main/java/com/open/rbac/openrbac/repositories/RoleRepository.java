package com.open.rbac.openrbac.repositories;

import com.open.rbac.openrbac.models.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long>, JpaSpecificationExecutor<Role> {
    boolean existsByIdAndRealm_id(Long id, Long realmId);

    Optional<Role> findByNameAndRealm_Id(String name, Long realmId);

    Optional<Role> findByKeycloakRoleId(String keycloakRoleId);

    List<Role> findAllByIdInAndRealm_Id(Collection<Long> ids, Long realmId);
}