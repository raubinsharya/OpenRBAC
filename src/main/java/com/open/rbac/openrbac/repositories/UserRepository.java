package com.open.rbac.openrbac.repositories;

import com.open.rbac.openrbac.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    Optional<User> findByUsername(String username);

    Optional<User> findByKeycloakUserId(String keycloakUserId);

    Optional<List<User>> findAllByIdInAndRealm_Id(List<Long> ids, Long realmId);
}