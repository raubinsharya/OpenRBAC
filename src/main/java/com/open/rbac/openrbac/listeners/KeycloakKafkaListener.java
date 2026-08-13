package com.open.rbac.openrbac.listeners;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.open.rbac.openrbac.dtos.kafka.KafkaAdminEventDto;
import com.open.rbac.openrbac.dtos.kafka.KafkaEventDto;
import com.open.rbac.openrbac.services.KeycloakSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KeycloakKafkaListener {

    private final ObjectMapper objectMapper;
    private final KeycloakSyncService keycloakSyncService;

    @KafkaListener(topics = "keycloak-events", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeClientEvent(String message) {
        log.debug("Received client event from Keycloak");
        try {
            KafkaEventDto event = objectMapper.readValue(message, KafkaEventDto.class);
            log.debug("Client event: type={}, user={}", event.getType(), event.getUserId());
            keycloakSyncService.processClientEvent(event);
        } catch (Exception e) {
            log.error("Error processing client event message: {}", message, e);
        }
    }

    @KafkaListener(topics = "keycloak-admin-events", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeAdminEvent(String message) {
        log.debug("Received admin event from Keycloak");
        try {
            KafkaAdminEventDto event = objectMapper.readValue(message, KafkaAdminEventDto.class);
            log.info("Admin event: op={}, resource={}", event.getOperationType(), event.getResourceType());
            keycloakSyncService.processAdminEvent(event);
        } catch (Exception e) {
            log.error("Error processing admin event message: {}", message, e);
        }
    }
}
