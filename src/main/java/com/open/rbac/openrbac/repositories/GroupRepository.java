package com.open.rbac.openrbac.repositories;

import com.open.rbac.openrbac.models.Group;
import com.open.rbac.openrbac.models.Realm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long>, JpaSpecificationExecutor<Group> {
  Optional<Group> findByIdAndRealm(Long id, Realm realm);

  Optional<Group> findByIdAndRealm_Id(Long id, Long realmId);

  @Query(value = """
      SELECT
                    g.id,
                    g.realm_id,
                    g.name,
                    g.description,
                    g.parent_group_id,
                    g.path,
                    g.level,
                    g.status,
                    g.created_at,
                    g.updated_at
                FROM groups g
                WHERE g.realm_id = :realmId
                  AND (
                        -- descendants + self
                        g.path LIKE (
                            SELECT path || '%'
                            FROM groups
                            WHERE id = :groupId
                              AND realm_id = :realmId
                        )
                        OR
                        -- ancestors + self
                        (
                            SELECT path
                            FROM groups
                            WHERE id = :groupId
                              AND realm_id = :realmId
                        ) LIKE g.path || '%'
                      );

      """, nativeQuery = true)
  List<Group> findGroupHierarchy(@Param("realmId") Long realmId, @Param("groupId") Long groupId);
}