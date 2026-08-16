package com.open.rbac.openrbac.dtos.kafka;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class KafkaEventDto {
    private String id;
    private long time;
    private String type; // LOGIN, REGISTER, LOGOUT, etc.
    private String realmId;
    private String clientId;
    private String userId;
    private String sessionId;
    private String ipAddress;
    private String error;
    private Map<String, String> details;
}
