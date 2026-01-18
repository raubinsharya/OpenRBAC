package com.open.rbac.openrbac.repositories;

import com.open.rbac.openrbac.models.UserEffectiveRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface UserEffectiveRoleRepository
        extends JpaRepository<UserEffectiveRole, String>, JpaSpecificationExecutor<UserEffectiveRole> {
}
