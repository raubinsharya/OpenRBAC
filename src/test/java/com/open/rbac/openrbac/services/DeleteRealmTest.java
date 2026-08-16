package com.open.rbac.openrbac.services;

import com.open.rbac.openrbac.models.*;
import com.open.rbac.openrbac.repositories.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.HashSet;

@SpringBootTest
public class DeleteRealmTest {

    @Autowired
    private RealmRepository realmRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private UserRoleRepository userRoleRepository;

    @Test
    public void testDeleteRealm() {
        Realm realm = new Realm();
        realm.setName("test-delete-realm2");
        realm.setRealmId("test-delete-realm2");
        realm = realmRepository.save(realm);

        User user = new User();
        user.setUsername("testuser2");
        user.setEmail("test2@test.com");
        user.setFirstName("test");
        user.setKeycloakUserId("test-user-id2");
        user.setRealm(realm);
        user = userRepository.save(user);

        Role role = new Role();
        role.setName("testrole2");
        role.setKeycloakRoleId("test-role-id2");
        role.setRealm(realm);
        role = roleRepository.save(role);

        UserRole ur = new UserRole();
        ur.setUser(user);
        ur.setRole(role);
        userRoleRepository.save(ur);

        System.out.println("Now deleting realm manually...");
        try {
            // Delete users first
            userRepository.deleteAll(userRepository.findAll()); // Just for test

            // Delete roles
            roleRepository.deleteAll(roleRepository.findAll());

            // Delete realm
            realmRepository.delete(realm);
        } catch (Exception e) {
            System.out.println("CAUGHT EXCEPTION DURING DELETE:");
            e.printStackTrace();
            throw e;
        }
    }
}
