package com.open.rbac.openrbac.repositories;

import com.open.rbac.openrbac.models.UserPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface UserPermissionRepository
                extends JpaRepository<UserPermission, Long>, JpaSpecificationExecutor<UserPermission> {

        List<UserPermission> findByUserId(Long userId);

        @Modifying
        @Query("DELETE FROM UserPermission up WHERE up.user.id = :userId AND up.permission.id IN :permissionIds")
        void deleteByUserIdAndPermissionIdIn(@Param("userId") Long userId,
                        @Param("permissionIds") Collection<Long> permissionIds);

        @Query("SELECT up.permission.id FROM UserPermission up WHERE up.user.id = :userId AND up.permission.id IN :permissionIds")
        List<Long> findExistingPermissionIds(@Param("userId") Long userId,
                        @Param("permissionIds") Collection<Long> permissionIds);
}
