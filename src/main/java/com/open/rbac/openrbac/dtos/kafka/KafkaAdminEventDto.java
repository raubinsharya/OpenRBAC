package com.open.rbac.openrbac.dtos.kafka;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class KafkaAdminEventDto {
    private String id;
    private long time;
    private String realmId;
    private AuthDetails authDetails;
    private String operationType; // CREATE, UPDATE, DELETE, ACTION
    private String resourceType; // USER, GROUP, REALM_ROLE, CLIENT, etc.
    private String resourcePath;
    private String representation;
    private String error;
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AuthDetails {
        private String realmId;
        private String clientId;
        private String userId;
        private String ipAddress;
    }
}
