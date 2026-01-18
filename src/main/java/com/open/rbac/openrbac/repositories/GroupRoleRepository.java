package com.open.rbac.openrbac.repositories;

import com.open.rbac.openrbac.models.GroupRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface GroupRoleRepository extends JpaRepository<GroupRole, Long>, JpaSpecificationExecutor<GroupRole> {
    List<GroupRole> findByGroupId(Long groupId);

    List<GroupRole> findByRoleId(Long roleId);

    List<GroupRole> findByGroupIdAndRoleIdIn(Long groupId, Collection<Long> roleIds);

    void deleteByGroupIdAndRoleId(Long groupId, Long roleId);

    void deleteByGroupIdAndRoleIdIn(Long groupId, Collection<Long> roleIds);

    void deleteBySourceGroupIdAndRoleIdIn(Long sourceGroupId, Collection<Long> roleIds);

    boolean existsByGroupIdAndRoleId(Long groupId, Long roleId);

    boolean existsByGroupIdAndRoleIdAndRole_Realm_Id(Long groupId, Long roleId, Long realmId);

    boolean existsByGroupIdAndRole_NameAndRole_Realm_Id(Long groupId, String roleName, Long realmId);

    @Query("SELECT gr.role.id FROM GroupRole gr WHERE gr.group.id = :groupId AND gr.role.id IN :roleIds")
    List<Long> findExistingRoleIds(@org.springframework.data.repository.query.Param("groupId") Long groupId,
            @org.springframework.data.repository.query.Param("roleIds") Collection<Long> roleIds);
}
