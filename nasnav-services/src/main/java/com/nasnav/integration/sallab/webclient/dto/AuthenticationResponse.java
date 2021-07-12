package com.nasnav.integration.sallab.webclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AuthenticationResponse {
    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("instance_url")
    private String instanceUrl;

    private String id;

    @JsonProperty("token_type")
    private String tokenType;

    @JsonProperty("issued_at")
    private String issuedAt;

    @JsonProperty("signature")
    private String signature;

}
