package com.open.rbac.openrbac.repositories;

import com.open.rbac.openrbac.models.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Long>, JpaSpecificationExecutor<UserRole> {
    List<UserRole> findByUserId(Long userId);

    List<UserRole> findByRoleId(Long roleId);

    void deleteByUserIdAndRoleId(Long userId, Long roleId);

    @Modifying
    @Query("DELETE FROM UserRole ur WHERE ur.user.id = :userId AND ur.role.id IN :roleIds")
    void deleteByUserIdAndRoleIdIn(Long userId, Collection<Long> roleIds);

    boolean existsByUserIdAndRoleId(Long userId, Long roleId);

    boolean existsByUserIdAndRoleIdAndRole_Realm_Id(Long userId, Long roleId, Long realmId);

    boolean existsByUserIdAndRole_NameAndRole_Realm_Id(Long userId, String roleName, Long realmId);

    @Query("SELECT ur.role.id FROM UserRole ur WHERE ur.user.id = :userId AND ur.role.id IN :roleIds")
    List<Long> findExistingRoleIds(Long userId, Collection<Long> roleIds);
}
