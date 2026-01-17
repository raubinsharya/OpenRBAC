package com.open.rbac.openrbac.repositories;

import com.open.rbac.openrbac.models.UserEffectivePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface UserEffectivePermissionRepository
    extends JpaRepository<UserEffectivePermission, String>,
    JpaSpecificationExecutor<UserEffectivePermission> {

  @org.springframework.data.jpa.repository.Query("""
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
      @org.springframework.data.repository.query.Param("realmId") Long realmId,
      @org.springframework.data.repository.query.Param("userId") Long userId,
      @org.springframework.data.repository.query.Param("resource") String resource,
      @org.springframework.data.repository.query.Param("action") String action,
      @org.springframework.data.repository.query.Param("assignmentType") String assignmentType,
      @org.springframework.data.repository.query.Param("permissionName") String permissionName);

  @org.springframework.data.jpa.repository.Query("""
      SELECT DISTINCT uep.permission.resource
      FROM UserEffectivePermission uep
      WHERE uep.user.id = :userId
        AND uep.user.realm.id = :realmId
        AND uep.permission.realm.id = :realmId
        AND (uep.isActive IS NULL OR uep.isActive = true)
        AND (uep.expiryDate IS NULL OR uep.expiryDate > CURRENT_TIMESTAMP)
        AND (:fromRole = true OR uep.assignmentType = 'DIRECT')
      """)
  org.springframework.data.domain.Page<String> findDistinctResourcesByUser(
      @org.springframework.data.repository.query.Param("realmId") Long realmId,
      @org.springframework.data.repository.query.Param("userId") Long userId,
      @org.springframework.data.repository.query.Param("fromRole") boolean fromRole,
      org.springframework.data.domain.Pageable pageable);

  @org.springframework.data.jpa.repository.Query("""
      SELECT DISTINCT uep.permission.action
      FROM UserEffectivePermission uep
      WHERE uep.user.id = :userId
        AND uep.user.realm.id = :realmId
        AND uep.permission.realm.id = :realmId
        AND (uep.isActive IS NULL OR uep.isActive = true)
        AND (uep.expiryDate IS NULL OR uep.expiryDate > CURRENT_TIMESTAMP)
        AND (:fromRole = true OR uep.assignmentType = 'DIRECT')
      """)
  org.springframework.data.domain.Page<String> findDistinctActionsByUser(
      @org.springframework.data.repository.query.Param("realmId") Long realmId,
      @org.springframework.data.repository.query.Param("userId") Long userId,
      @org.springframework.data.repository.query.Param("fromRole") boolean fromRole,
      org.springframework.data.domain.Pageable pageable);
}
