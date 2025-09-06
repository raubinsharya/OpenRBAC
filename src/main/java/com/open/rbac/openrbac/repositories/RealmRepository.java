package com.open.rbac.openrbac.repositories;

import com.open.rbac.openrbac.models.Realm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RealmRepository extends JpaRepository<Realm, Long> {
 
    Optional<Realm> findByRealmId(String realmId);

    Optional<Realm> findByName(String name);
    
    List<Realm> findByStatus(com.open.rbac.openrbac.enums.EntityStatus status);

    // === FETCH REALM WITH USERS ===

    /**
     * Find realm by ID and eagerly fetch all its users
     * Uses JOIN FETCH to avoid N+1 query problem
     */
    @Query("SELECT r FROM Realm r LEFT JOIN FETCH r.users WHERE r.id = :id")
    Optional<Realm> findByIdWithUsers(@Param("id") Long id);

    /**
     * Find realm by ID and eagerly fetch users with specific status
     */
    @Query("SELECT r FROM Realm r LEFT JOIN FETCH r.users u WHERE r.id = :id AND (u.status = :userStatus OR u.status IS NULL)")
    Optional<Realm> findByIdWithUsersByStatus(@Param("id") Long id, @Param("userStatus") com.open.rbac.openrbac.enums.EntityStatus userStatus);

    /**
     * Find realm by realmId and eagerly fetch all its users
     */
    @Query("SELECT r FROM Realm r LEFT JOIN FETCH r.users WHERE r.realmId = :realmId")
    Optional<Realm> findByRealmIdWithUsers(@Param("realmId") String realmId);

    /**
     * Find realm by realmId and eagerly fetch users with specific status
     */
    @Query("SELECT r FROM Realm r LEFT JOIN FETCH r.users u WHERE r.realmId = :realmId AND (u.status = :userStatus OR u.status IS NULL)")
    Optional<Realm> findByRealmIdWithUsersByStatus(@Param("realmId") String realmId, @Param("userStatus") com.open.rbac.openrbac.enums.EntityStatus userStatus);

    /**
     * Find realm by name and eagerly fetch all its users
     */
    @Query("SELECT r FROM Realm r LEFT JOIN FETCH r.users WHERE r.name = :name")
    Optional<Realm> findByNameWithUsers(@Param("name") String name);

    /**
     * Find all realms and eagerly fetch their users
     * Note: This can be expensive for large datasets
     */
    @Query("SELECT DISTINCT r FROM Realm r LEFT JOIN FETCH r.users")
    List<Realm> findAllWithUsers();

    /**
     * Find all active realms with their users
     */
    @Query("SELECT DISTINCT r FROM Realm r LEFT JOIN FETCH r.users WHERE r.status = 'ACTIVE'")
    List<Realm> findAllActiveWithUsers();
    
    /**
     * Find all realms by status with their users
     */
    @Query("SELECT DISTINCT r FROM Realm r LEFT JOIN FETCH r.users WHERE r.status = :status")
    List<Realm> findByStatusWithUsers(@Param("status") com.open.rbac.openrbac.enums.EntityStatus status);

    /**
     * Find all realms with users filtered by user status
     */
    @Query("SELECT DISTINCT r FROM Realm r LEFT JOIN FETCH r.users u WHERE u.status = :userStatus OR u.status IS NULL")
    List<Realm> findAllWithUsersByUserStatus(@Param("userStatus") com.open.rbac.openrbac.enums.EntityStatus userStatus);

    /**
     * Find realms by status with users filtered by user status
     */
    @Query("SELECT DISTINCT r FROM Realm r LEFT JOIN FETCH r.users u WHERE r.status = :status AND (u.status = :userStatus OR u.status IS NULL)")
    List<Realm> findByStatusWithUsersByUserStatus(@Param("status") com.open.rbac.openrbac.enums.EntityStatus status, @Param("userStatus") com.open.rbac.openrbac.enums.EntityStatus userStatus);
}