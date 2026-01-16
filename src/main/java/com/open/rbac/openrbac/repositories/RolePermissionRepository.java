package com.open.rbac.openrbac.repositories;

import com.open.rbac.openrbac.models.Permission;
import com.open.rbac.openrbac.models.RolePermission;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface RolePermissionRepository
                extends JpaRepository<RolePermission, Long>, JpaSpecificationExecutor<RolePermission> {

        @Query("SELECT rp.permission.id FROM RolePermission rp WHERE rp.role.id = :roleId AND rp.permission.id IN :permissionIds")
        List<Long> findExistingPermissionIds(@Param("roleId") Long roleId,
                        @Param("permissionIds") Set<Long> permissionIds);

        void deleteByRoleIdAndPermissionIdIn(Long roleId, Collection<Long> permissionIds);

        @Query("SELECT CASE WHEN COUNT(rp) > 0 THEN true ELSE false END FROM RolePermission rp " +
                        "WHERE rp.role.id = :roleId AND rp.role.realm.id = :realmId " +
                        "AND rp.role.status = com.open.rbac.openrbac.enums.EntityStatus.ACTIVE AND rp.permission.status = com.open.rbac.openrbac.enums.EntityStatus.ACTIVE "
                        +
                        "AND (rp.expiryDate IS NULL OR rp.expiryDate > CURRENT_TIMESTAMP) " +
                        "AND LOWER(rp.permission.resource) = LOWER(:resource) AND LOWER(rp.permission.action) = LOWER(:action)")
        boolean checkPermission(@Param("realmId") Long realmId,
                        @Param("roleId") Long roleId,
                        @Param("resource") String resource,
                        @Param("action") String action);
}
