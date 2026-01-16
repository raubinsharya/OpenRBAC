package com.open.rbac.openrbac.repositories;

import com.open.rbac.openrbac.models.Permission;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long>, JpaSpecificationExecutor<Permission> {

        List<Permission> findAllByIdInAndRealm_Id(Set<Long> ids, Long realmId);

        @Query("SELECT rp.permission FROM Role r JOIN r.rolePermissions rp WHERE r.id = :roleId AND r.realm.id = :realmId")
        Page<Permission> findByRoleIdAndRealmId(@Param("roleId") Long roleId, @Param("realmId") Long realmId,
                        Pageable pageable);

        @Query("""
                            select p.name
                            from Permission p
                            where p.realm.id = :realmId
                            and p.name in :names
                        """)
        Set<String> findExistingNames(
                        @Param("realmId") Long realmId,
                        @Param("names") Set<String> names);

        @Query("""
                        select distinct p.resource
                        from Permission p
                        where p.realm.id = :realmId
                        """)
        Page<String> findDistinctResources(
                        @Param("realmId") Long realmId,
                        Pageable pageable);

        @Query("""
                        select distinct p.action
                        from Permission p
                        where p.realm.id = :realmId
                        """)
        Page<String> findDistinctActions(
                        @Param("realmId") Long realmId,
                        Pageable pageable);

        @Query("""
                        SELECT DISTINCT p.resource
                        FROM Permission p
                        WHERE p.realm.id = :realmId
                        AND (
                            EXISTS (SELECT 1 FROM User u JOIN u.permissions up WHERE u.id = :userId AND up.id = p.id)
                            OR
                            (:includeRoles = true AND EXISTS (SELECT 1 FROM UserRole ur WHERE ur.user.id = :userId AND ur.role.id IN (SELECT rp.role.id FROM RolePermission rp WHERE rp.permission.id = p.id) AND ur.isActive = true AND (ur.expiryDate IS NULL OR ur.expiryDate > CURRENT_TIMESTAMP)))
                            OR
                            (:includeGroups = true AND EXISTS (
                                SELECT 1 FROM UserGroup ug
                                JOIN ug.group g
                                JOIN GroupRole gr ON gr.group.id = g.id
                                WHERE ug.user.id = :userId
                                AND ug.isActive = true
                                AND (ug.expiryDate IS NULL OR ug.expiryDate > CURRENT_TIMESTAMP)
                                AND gr.role.id IN (SELECT rp.role.id FROM RolePermission rp WHERE rp.permission.id = p.id)
                                AND gr.isActive = true
                                AND (gr.expiryDate IS NULL OR gr.expiryDate > CURRENT_TIMESTAMP)
                            ))
                        )
                        """)
        Page<String> findDistinctResourcesByUserId(
                        @Param("realmId") Long realmId,
                        @Param("userId") Long userId,
                        @Param("includeRoles") boolean includeRoles,
                        @Param("includeGroups") boolean includeGroups,
                        Pageable pageable);

}
