package com.nasnav.shipping.services.mylerz.webclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class AuthenticationResponse {
    private String accessToken;
    private String tokenType;
    private Long expiresIn;
    @JsonProperty("userName")
    private String userName;
    @JsonProperty(".issued")
    private String issued;
    @JsonProperty(".expires")
    private String expires;
}
