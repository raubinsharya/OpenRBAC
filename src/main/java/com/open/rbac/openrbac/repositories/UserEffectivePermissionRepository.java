package com.open.rbac.openrbac.repositories;

import com.open.rbac.openrbac.models.UserEffectivePermission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserEffectivePermissionRepository
    extends JpaRepository<UserEffectivePermission, String>,
    JpaSpecificationExecutor<UserEffectivePermission> {

  @Query("""
      SELECT CASE WHEN COUNT(uep) > 0 THEN true ELSE false END
      FROM UserEffectivePermission uep
      WHERE uep.user.id = :userId
        AND uep.user.realm.id = :realmId
        AND uep.permission.realm.id = :realmId
        AND (:resource IS NULL OR LOWER(uep.permission.resource) = LOWER(CAST(:resource AS string)))
        AND (:action IS NULL OR LOWER(uep.permission.action) = LOWER(CAST(:action AS string)))
        AND (:permissionName IS NULL OR LOWER(uep.permission.name) = LOWER(CAST(:permissionName AS string)))
        AND (uep.isActive IS NULL OR uep.isActive = true)
        AND (uep.expiryDate IS NULL OR uep.expiryDate > CURRENT_TIMESTAMP)
        AND (:assignmentType IS NULL OR
             uep.assignmentType = UPPER(CAST(:assignmentType AS string)))
      """)
  boolean checkPermission(
      @Param("realmId") Long realmId,
      @Param("userId") Long userId,
      @Param("resource") String resource,
      @Param("action") String action,
      @Param("assignmentType") String assignmentType,
      @Param("permissionName") String permissionName);

  @Query("""
      SELECT DISTINCT uep.permission.resource
      FROM UserEffectivePermission uep
      WHERE uep.user.id = :userId
        AND uep.user.realm.id = :realmId
        AND uep.permission.realm.id = :realmId
        AND (uep.isActive IS NULL OR uep.isActive = true)
        AND (uep.expiryDate IS NULL OR uep.expiryDate > CURRENT_TIMESTAMP)
        AND (:assignmentType IS NULL OR uep.assignmentType = UPPER(CAST(:assignmentType AS string)))
      """)
  Page<String> findDistinctResourcesByUser(
      @Param("realmId") Long realmId,
      @Param("userId") Long userId,
      @Param("assignmentType") String assignmentType,
      Pageable pageable);

  @Query("""
      SELECT DISTINCT uep.permission.action
      FROM UserEffectivePermission uep
      WHERE uep.user.id = :userId
        AND uep.user.realm.id = :realmId
        AND uep.permission.realm.id = :realmId
        AND (uep.isActive IS NULL OR uep.isActive = true)
        AND (uep.expiryDate IS NULL OR uep.expiryDate > CURRENT_TIMESTAMP)
        AND (:assignmentType IS NULL OR uep.assignmentType = UPPER(CAST(:assignmentType AS string)))
      """)
  Page<String> findDistinctActionsByUser(
      @Param("realmId") Long realmId,
      @Param("userId") Long userId,
      @Param("assignmentType") String assignmentType,
      Pageable pageable);
}
