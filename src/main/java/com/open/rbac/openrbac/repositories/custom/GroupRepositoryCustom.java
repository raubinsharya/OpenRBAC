package com.open.rbac.openrbac.repositories.custom;

import com.open.rbac.openrbac.models.Group;
import java.util.List;

public interface GroupRepositoryCustom {
    List<Group> findGroupHierarchy(String realmIdentifier, Long groupId);
}
