package com.open.rbac.openrbac.repositories;

import com.open.rbac.openrbac.models.GroupPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface GroupPermissionRepository
                extends JpaRepository<GroupPermission, Long>, JpaSpecificationExecutor<GroupPermission> {

        @Query("SELECT gp.permission.id FROM GroupPermission gp WHERE gp.group.id = :groupId AND gp.permission.id IN :permissionIds")
        List<Long> findExistingPermissionIds(@Param("groupId") Long groupId,
                        @Param("permissionIds") Collection<Long> permissionIds);

        List<GroupPermission> findByGroupIdAndPermissionIdIn(Long groupId, Collection<Long> permissionIds);

        @org.springframework.data.jpa.repository.Modifying
        @Query("DELETE FROM GroupPermission gp WHERE gp.group.id = :groupId AND gp.permission.id IN :permissionIds")
        void deleteByGroupIdAndPermissionIdIn(@Param("groupId") Long groupId,
                        @Param("permissionIds") Collection<Long> permissionIds);
}
