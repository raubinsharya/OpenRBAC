package com.open.rbac.openrbac.services;

import com.open.rbac.openrbac.dtos.AuditRevisionDto;
import com.open.rbac.openrbac.models.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    @PersistenceContext
    private final EntityManager entityManager;

    private static final Map<String, Class<?>> ENTITY_MAP = Map.of(
            "users", User.class,
            "roles", Role.class,
            "realms", Realm.class,
            "groups", Group.class,
            "user-roles", UserRole.class,
            "group-roles", GroupRole.class
    );

    @Transactional(readOnly = true)
    public List<AuditRevisionDto<Object>> getEntityRevisions(String entityType, String entityId) {
        Class<?> entityClass = ENTITY_MAP.get(entityType.toLowerCase());
        if (entityClass == null) {
            throw new IllegalArgumentException("Unknown entity type for audit: " + entityType);
        }

        Object parsedId = parseId(entityClass, entityId);

        AuditReader auditReader = AuditReaderFactory.get(entityManager);
        AuditQuery query = auditReader.createQuery()
                .forRevisionsOfEntity(entityClass, false, true)
                .add(AuditEntity.id().eq(parsedId));

        List<Object[]> results = query.getResultList();
        List<AuditRevisionDto<Object>> dtos = new ArrayList<>();

        for (Object[] result : results) {
            Object entity = result[0];
            Object revisionEntity = result[1];
            RevisionType revisionType = (RevisionType) result[2];

            Number revId = null;
            Date revDate = null;
            
            try {
                if (revisionEntity instanceof org.hibernate.envers.DefaultRevisionEntity) {
                    org.hibernate.envers.DefaultRevisionEntity dre = (org.hibernate.envers.DefaultRevisionEntity) revisionEntity;
                    revId = dre.getId();
                    revDate = dre.getRevisionDate();
                }
            } catch (Exception e) {
                log.warn("Failed to extract revision info", e);
            }

            AuditRevisionDto<Object> dto = AuditRevisionDto.<Object>builder()
                    .revisionId(revId)
                    .revisionDate(revDate)
                    .revisionType(revisionType.name())
                    .entity(entity)
                    .build();

            dtos.add(dto);
        }

        return dtos;
    }
    
    private Object parseId(Class<?> entityClass, String id) {
        if (entityClass.equals(User.class) || entityClass.equals(Role.class) || 
            entityClass.equals(Group.class) || entityClass.equals(Permission.class) ||
            entityClass.equals(RolePermission.class) || entityClass.equals(UserGroup.class)) {
            try {
                return Long.parseLong(id);
            } catch (NumberFormatException e) {
                return id; 
            }
        }
        return id;
    }
}
