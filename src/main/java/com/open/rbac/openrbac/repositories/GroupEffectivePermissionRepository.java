package com.open.rbac.openrbac.repositories;

import com.open.rbac.openrbac.models.GroupEffectivePermission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GroupEffectivePermissionRepository
        extends JpaRepository<GroupEffectivePermission, String>, JpaSpecificationExecutor<GroupEffectivePermission> {

    @Query("SELECT COUNT(gep) > 0 FROM GroupEffectivePermission gep " +
            "WHERE gep.group.id = :groupId " +
            "AND gep.permission.realm.id = :realmId " +
            "AND (:permissionId IS NULL OR gep.permission.id = :permissionId) " +
            "AND (:permissionName IS NULL OR lower(gep.permission.name) like lower(concat('%', :permissionName, '%'))) "
            +
            "AND (gep.expiryDate IS NULL OR gep.expiryDate > CURRENT_TIMESTAMP) " +
            "AND gep.isActive = true")
    boolean checkPermission(@Param("realmId") Long realmId,
            @Param("groupId") Long groupId,
            @Param("permissionId") Long permissionId,
            @Param("permissionName") String permissionName);

    @Query(value = "SELECT DISTINCT p.resource FROM GroupEffectivePermission gep JOIN gep.permission p WHERE gep.group.id = :groupId AND p.realm.id = :realmId AND (:fromRole IS FALSE OR gep.assignmentType = 'ROLE')")
    Page<String> findDistinctResourcesByGroup(@Param("realmId") Long realmId,
            @Param("groupId") Long groupId,
            @Param("fromRole") boolean fromRole,
            Pageable pageable);

    @Query(value = "SELECT DISTINCT p.action FROM GroupEffectivePermission gep JOIN gep.permission p WHERE gep.group.id = :groupId AND p.realm.id = :realmId AND (:fromRole IS FALSE OR gep.assignmentType = 'ROLE')")
    Page<String> findDistinctActionsByGroup(@Param("realmId") Long realmId,
            @Param("groupId") Long groupId,
            @Param("fromRole") boolean fromRole,
            Pageable pageable);
}
