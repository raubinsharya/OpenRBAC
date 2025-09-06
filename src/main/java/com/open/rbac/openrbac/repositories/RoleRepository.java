package com.open.rbac.openrbac.repositories;

import com.open.rbac.openrbac.models.Role;
import com.open.rbac.openrbac.models.Realm;
import com.open.rbac.openrbac.enums.EntityStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long>, JpaSpecificationExecutor<Role> {
    
    Optional<Role> findByNameAndRealm(String name, Realm realm);
    
    Optional<Role> findByNameAndRealmId(String name, Long realmId);
    
    List<Role> findByRealm(Realm realm);
    
    List<Role> findByRealmId(Long realmId);
    
    List<Role> findByStatus(EntityStatus status);
    
    List<Role> findByRealmIdAndStatus(Long realmId, EntityStatus status);
    
    List<Role> findByIsSystemRole(Boolean isSystemRole);
    
    boolean existsByNameAndRealmId(String name, Long realmId);
    
    @Query("SELECT r FROM Role r WHERE r.realm.id = :realmId AND r.status = :status")
    List<Role> findActiveRolesByRealm(@Param("realmId") Long realmId, @Param("status") EntityStatus status);
}