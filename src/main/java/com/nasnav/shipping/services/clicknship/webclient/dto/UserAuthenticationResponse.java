package com.nasnav.shipping.services.clicknship.webclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class UserAuthenticationResponse {
    private String accessToken;
    private String tokenType;
    private Integer expiresIn;
    @JsonProperty("userName")
    private String userName;
    @JsonProperty(".issued")
    private String issuedAt;
    @JsonProperty(".expires")
    private String expiresAt;
}
