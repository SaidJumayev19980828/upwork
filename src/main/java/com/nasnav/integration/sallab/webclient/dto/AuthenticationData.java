package com.nasnav.integration.sallab.webclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class AuthenticationData {

    @JsonProperty("grant_type")
    private String grantType;

    @JsonProperty("client_id")
    private String clientId;

    @JsonProperty("client_secret")
    private String clientSecret;

    @JsonProperty("username")
    private String userName;

    @JsonProperty("password")
    private String password;

    @Override
    public String toString() {
        String result = "";

        if (this.grantType != null)
            result += "&grant_type="+this.grantType;

        if (this.clientId != null)
            result += "&client_id="+this.clientId;

        if (this.clientSecret != null)
            result += "&client_secret="+this.clientSecret;

        if (this.userName != null)
            result += "&username="+this.userName;

        if (this.password != null)
            result += "&password="+this.password;

        return result.length() > 0 ? result.substring(1) : "";
    }
}
