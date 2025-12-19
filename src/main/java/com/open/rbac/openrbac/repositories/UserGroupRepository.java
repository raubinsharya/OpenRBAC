package com.open.rbac.openrbac.repositories;

import com.open.rbac.openrbac.models.UserGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserGroupRepository extends JpaRepository<UserGroup, Long>, JpaSpecificationExecutor<UserGroup> {

    boolean existsByUserIdAndGroupId(Long userId, Long groupId);

    @Query("SELECT ug.user.id FROM UserGroup ug WHERE ug.group.id = :groupId AND ug.user.id IN :userIds")
    List<Long> findExistingMemberIds(@Param("groupId") Long groupId,
                                     @Param("userIds") java.util.List<Long> userIds);
}
