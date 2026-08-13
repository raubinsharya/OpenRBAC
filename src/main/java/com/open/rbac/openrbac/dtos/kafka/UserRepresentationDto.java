package com.open.rbac.openrbac.dtos.kafka;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserRepresentationDto {
    private String id; // Keycloak User ID
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private boolean enabled;
}
