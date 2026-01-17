package com.open.rbac.openrbac.repositories;

import com.open.rbac.openrbac.models.UserEffectivePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface UserEffectivePermissionRepository
        extends JpaRepository<UserEffectivePermission, String>, JpaSpecificationExecutor<UserEffectivePermission> {
}
