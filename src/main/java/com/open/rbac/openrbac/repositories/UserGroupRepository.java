package com.open.rbac.openrbac.repositories;

import com.open.rbac.openrbac.models.UserGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface UserGroupRepository extends JpaRepository<UserGroup, Long>, JpaSpecificationExecutor<UserGroup> {

        boolean existsByUserIdAndGroupId(Long userId, Long groupId);

        boolean existsByUserIdAndGroup_IdAndGroup_Realm_IdAndUser_Realm_Id(Long userId, Long groupId, Long realmId,
                        Long userRealmId);

        Optional<UserGroup> findByUserIdAndGroup_IdAndGroup_Realm_Id(Long userId, Long groupId, Long realmId);

        @Query("SELECT ug.user.id FROM UserGroup ug WHERE ug.group.id = :groupId AND ug.user.id IN :userIds")
        List<Long> findExistingMemberIds(@Param("groupId") Long groupId,
                        @Param("userIds") List<Long> userIds);

        @Modifying
        @Query("DELETE FROM UserGroup ug WHERE ug.group.id = :groupId AND ug.user.id IN :userIds")
        void removeMembers(@Param("groupId") Long groupId, @Param("userIds") java.util.List<Long> userIds);

        @Modifying
        @Query("UPDATE UserGroup ug SET ug.expiryDate = :expiryDate WHERE ug.group.id = :groupId AND ug.id IN :ids")
        void updateExpiryDate(@Param("groupId") Long groupId,
                        @Param("ids") Set<Long> ids,
                        @Param("expiryDate") LocalDateTime expiryDate);

        List<UserGroup> findAllByIdIn(Set<Long> ids);

        List<UserGroup> findByUserIdAndGroup_Realm_Id(Long userId, Long realmId);
}
