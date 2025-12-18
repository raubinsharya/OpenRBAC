package com.open.rbac.openrbac.repositories;

import com.open.rbac.openrbac.models.Permission;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long>, JpaSpecificationExecutor<Permission> {
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

}
