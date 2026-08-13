package com.open.rbac.openrbac.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.open.rbac.openrbac.dtos.kafka.KafkaAdminEventDto;
import com.open.rbac.openrbac.dtos.kafka.KafkaEventDto;
import com.open.rbac.openrbac.dtos.kafka.UserRepresentationDto;
import com.open.rbac.openrbac.enums.EntityStatus;
import com.open.rbac.openrbac.models.Realm;
import com.open.rbac.openrbac.models.User;
import com.open.rbac.openrbac.repositories.RealmRepository;
import com.open.rbac.openrbac.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.type.TypeReference;
import com.open.rbac.openrbac.dtos.kafka.RoleRepresentationDto;
import com.open.rbac.openrbac.models.Role;
import com.open.rbac.openrbac.models.UserRole;
import com.open.rbac.openrbac.repositories.RoleRepository;
import com.open.rbac.openrbac.repositories.UserRoleRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class KeycloakSyncService {

    private final UserRepository userRepository;
    private final RealmRepository realmRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void processAdminEvent(KafkaAdminEventDto event) {
        log.info("Processing admin event: {} on {} for realm {}", event.getOperationType(), event.getResourceType(), event.getRealmId());

        if ("USER".equals(event.getResourceType())) {
            handleUserEvent(event);
        } else if ("REALM_ROLE_MAPPING".equals(event.getResourceType())) {
            handleRealmRoleMappingEvent(event);
        } else if ("REALM_ROLE".equals(event.getResourceType())) {
            handleRealmRoleEvent(event);
        } else {
            log.debug("Resource type {} is currently not synchronized", event.getResourceType());
        }
    }

    @Transactional
    public void processClientEvent(KafkaEventDto event) {
        log.info("Processing client event: {} for user {} in realm {}", event.getType(), event.getUserId(), event.getRealmId());

        if ("REGISTER".equals(event.getType()) || "UPDATE_PROFILE".equals(event.getType()) || 
            "UPDATE_EMAIL".equals(event.getType()) || "DELETE_ACCOUNT".equals(event.getType())) {
            handleClientUserEvent(event);
        } else {
            log.debug("Client event type {} is currently not synchronized", event.getType());
        }
    }

    private void handleUserEvent(KafkaAdminEventDto event) {
        try {
            String operation = event.getOperationType();
            String resourcePath = event.getResourcePath(); // e.g. users/4e17b8f0-1234-5678-abcd-ef0123456789
            
            // Extract Keycloak user ID from resource path
            String keycloakUserId = null;
            if (resourcePath != null && resourcePath.startsWith("users/")) {
                String[] parts = resourcePath.split("/");
                if (parts.length > 1) {
                    keycloakUserId = parts[1];
                }
            }

            if (keycloakUserId == null) {
                log.warn("Could not extract keycloak user ID from resource path: {}", resourcePath);
                return;
            }

            if ("CREATE".equals(operation) || "UPDATE".equals(operation)) {
                if (event.getRepresentation() == null) {
                    log.warn("Representation is missing for {} user operation. Path: {}", operation, resourcePath);
                    return;
                }

                UserRepresentationDto userDto = objectMapper.readValue(event.getRepresentation(), UserRepresentationDto.class);
                
                Realm realm = realmRepository.findByRealmId(event.getRealmId()).orElse(null);
                if (realm == null) {
                    log.error("Realm not found: {}. Cannot sync user.", event.getRealmId());
                    return;
                }

                User user = userRepository.findByKeycloakUserId(keycloakUserId).orElse(new User());
                user.setKeycloakUserId(keycloakUserId);
                user.setUsername(userDto.getUsername());
                user.setEmail(userDto.getEmail());
                user.setFirstName(userDto.getFirstName() != null ? userDto.getFirstName() : "");
                user.setLastName(userDto.getLastName());
                user.setRealm(realm);
                user.setStatus(userDto.isEnabled() ? EntityStatus.ACTIVE : EntityStatus.DISABLED);

                userRepository.save(user);
                log.info("Successfully synced user {} ({})", user.getUsername(), keycloakUserId);
                
            } else if ("DELETE".equals(operation)) {
                final String finalKeycloakUserId = keycloakUserId;
                userRepository.findByKeycloakUserId(finalKeycloakUserId).ifPresent(user -> {
                    userRepository.delete(user);
                    log.info("Successfully deleted user with keycloak ID {}", finalKeycloakUserId);
                });
            }

        } catch (Exception e) {
            log.error("Failed to handle user event", e);
        }
    }

    private void handleClientUserEvent(KafkaEventDto event) {
        try {
            String keycloakUserId = event.getUserId();
            if (keycloakUserId == null) {
                log.warn("User ID is missing from client event: {}", event.getType());
                return;
            }

            if ("DELETE_ACCOUNT".equals(event.getType())) {
                userRepository.findByKeycloakUserId(keycloakUserId).ifPresent(user -> {
                    userRepository.delete(user);
                    log.info("Successfully deleted user {} from DELETE_ACCOUNT client event", keycloakUserId);
                });
                return;
            }

            Realm realm = realmRepository.findByRealmId(event.getRealmId()).orElse(null);
            if (realm == null) {
                log.error("Realm not found: {}. Cannot sync client event.", event.getRealmId());
                return;
            }

            java.util.Map<String, String> details = event.getDetails();
            if (details == null) {
                log.warn("Details missing from client event for user {}", keycloakUserId);
                return;
            }

            User user = userRepository.findByKeycloakUserId(keycloakUserId).orElse(new User());
            user.setKeycloakUserId(keycloakUserId);
            user.setRealm(realm);
            
            if (details.containsKey("username")) {
                user.setUsername(details.get("username"));
            }
            if (details.containsKey("email")) {
                user.setEmail(details.get("email"));
            }
            if (details.containsKey("first_name")) {
                user.setFirstName(details.get("first_name"));
            } else if (user.getFirstName() == null) {
                user.setFirstName("");
            }
            if (details.containsKey("last_name")) {
                user.setLastName(details.get("last_name"));
            }
            
            if (user.getId() == null) {
                user.setStatus(EntityStatus.ACTIVE);
            }

            userRepository.save(user);
            log.info("Successfully synced user {} from client event {}", user.getUsername(), event.getType());

        } catch (Exception e) {
            log.error("Failed to handle client user event", e);
        }
    }

    private void handleRealmRoleMappingEvent(KafkaAdminEventDto event) {
        try {
            String operation = event.getOperationType();
            String resourcePath = event.getResourcePath();
            
            // Extract Keycloak user ID from resource path e.g., users/{userId}/role-mappings/realm
            String keycloakUserId = null;
            if (resourcePath != null && resourcePath.startsWith("users/")) {
                String[] parts = resourcePath.split("/");
                if (parts.length > 1) {
                    keycloakUserId = parts[1];
                }
            }

            if (keycloakUserId == null) {
                log.warn("Could not extract keycloak user ID from resource path for REALM_ROLE_MAPPING: {}", resourcePath);
                return;
            }

            if (event.getRepresentation() == null) {
                log.warn("Representation is missing for {} role mapping. Path: {}", operation, resourcePath);
                return;
            }

            List<RoleRepresentationDto> roleDtos = objectMapper.readValue(event.getRepresentation(), new TypeReference<List<RoleRepresentationDto>>() {});
            
            Realm realm = realmRepository.findByRealmId(event.getRealmId()).orElse(null);
            if (realm == null) {
                log.error("Realm not found: {}. Cannot sync role mapping.", event.getRealmId());
                return;
            }

            User user = userRepository.findByKeycloakUserId(keycloakUserId).orElse(null);
            if (user == null) {
                log.warn("User not found for keycloak ID: {}. Cannot map roles.", keycloakUserId);
                return;
            }

            if ("CREATE".equals(operation)) {
                for (RoleRepresentationDto roleDto : roleDtos) {
                    Role role = roleRepository.findByNameAndRealm_Id(roleDto.getName(), realm.getId()).orElse(null);
                    if (role != null) {
                        if (!userRoleRepository.existsByUserIdAndRoleId(user.getId(), role.getId())) {
                            UserRole userRole = UserRole.builder()
                                    .user(user)
                                    .role(role)
                                    .isActive(true)
                                    .build();
                            userRoleRepository.save(userRole);
                            log.info("Mapped role {} to user {}", role.getName(), user.getUsername());
                        }
                    } else {
                        log.warn("Role {} not found in local DB for realm {}, skipping mapping", roleDto.getName(), realm.getName());
                    }
                }
            } else if ("DELETE".equals(operation)) {
                for (RoleRepresentationDto roleDto : roleDtos) {
                    Role role = roleRepository.findByNameAndRealm_Id(roleDto.getName(), realm.getId()).orElse(null);
                    if (role != null) {
                        userRoleRepository.deleteByUserIdAndRoleId(user.getId(), role.getId());
                        log.info("Removed role {} from user {}", role.getName(), user.getUsername());
                    }
                }
            }

        } catch (Exception e) {
            log.error("Failed to handle realm role mapping event", e);
        }
    }

    private void handleRealmRoleEvent(KafkaAdminEventDto event) {
        try {
            String operation = event.getOperationType();
            String resourcePath = event.getResourcePath();
            
            String roleName = null;
            String roleIdFromPath = null;
            
            if (resourcePath != null) {
                if (resourcePath.startsWith("roles/")) {
                    String[] parts = resourcePath.split("/");
                    if (parts.length > 1) {
                        roleName = parts[1];
                    }
                } else if (resourcePath.startsWith("roles-by-id/")) {
                    String[] parts = resourcePath.split("/");
                    if (parts.length > 1) {
                        roleIdFromPath = parts[1];
                    }
                }
            }

            if ("CREATE".equals(operation) || "UPDATE".equals(operation)) {
                if (event.getRepresentation() == null) {
                    log.warn("Representation is missing for {} realm role operation. Path: {}", operation, resourcePath);
                    return;
                }

                RoleRepresentationDto roleDto = objectMapper.readValue(event.getRepresentation(), RoleRepresentationDto.class);
                roleName = roleDto.getName();
                String keycloakRoleId = roleDto.getId();
                
                Realm realm = realmRepository.findByRealmId(event.getRealmId()).orElse(null);
                if (realm == null) {
                    log.error("Realm not found: {}. Cannot sync realm role.", event.getRealmId());
                    return;
                }

                Role role = roleRepository.findByNameAndRealm_Id(roleName, realm.getId()).orElse(new Role());
                role.setKeycloakRoleId(keycloakRoleId);
                role.setName(roleName);
                role.setDescription(roleDto.getDescription());
                role.setRealm(realm);
                role.setStatus(EntityStatus.ACTIVE);
                if (role.getId() == null) {
                    role.setIsSystemRole(false);
                }

                roleRepository.save(role);
                log.info("Successfully synced realm role {} for realm {}", roleName, realm.getName());

            } else if ("DELETE".equals(operation)) {
                if (roleIdFromPath != null) {
                    final String finalRoleId = roleIdFromPath;
                    roleRepository.findByKeycloakRoleId(finalRoleId).ifPresent(role -> {
                        roleRepository.delete(role);
                        log.info("Successfully deleted realm role by keycloak ID {}", finalRoleId);
                    });
                } else if (roleName != null) {
                    final String finalRoleName = roleName;
                    realmRepository.findByRealmId(event.getRealmId()).ifPresent(realm -> {
                        roleRepository.findByNameAndRealm_Id(finalRoleName, realm.getId()).ifPresent(role -> {
                            roleRepository.delete(role);
                            log.info("Successfully deleted realm role by name {}", finalRoleName);
                        });
                    });
                } else {
                    log.warn("Could not extract role name or ID from resource path for REALM_ROLE DELETE: {}", resourcePath);
                }
            }

        } catch (Exception e) {
            log.error("Failed to handle realm role event", e);
        }
    }
}
