package com.open.rbac.openrbac.repositories.custom;

import com.open.rbac.openrbac.models.Group;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class GroupRepositoryImpl implements GroupRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Group> findGroupHierarchy(String realmIdentifier, Long groupId) {
        Long realmIdVal = null;
        try {
            realmIdVal = Long.parseLong(realmIdentifier);
        } catch (NumberFormatException e) {
            // ignore, treated as string name
        }

        String jpql = """
                SELECT g FROM Group g
                LEFT JOIN FETCH g.createdBy
                JOIN g.realm r
                WHERE (
                    (:realmIdVal IS NOT NULL AND r.id = :realmIdVal)
                    OR r.name = :realmStringVal
                    OR r.realmId = :realmStringVal
                )
                AND (
                    g.path LIKE CONCAT((SELECT t.path FROM Group t WHERE t.id = :groupId), '%')
                    OR
                    (SELECT t.path FROM Group t WHERE t.id = :groupId) LIKE CONCAT(g.path, '%')
                )
                """;

        TypedQuery<Group> query = entityManager.createQuery(jpql, Group.class);
        query.setParameter("realmIdVal", realmIdVal);
        query.setParameter("realmStringVal", realmIdentifier);
        query.setParameter("groupId", groupId);

        return query.getResultList();
    }
}
