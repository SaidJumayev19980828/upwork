package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
public class UserDTOs {

    @ApiModel(value = "User Registration Data")
    public static class UserRegistrationObject {
        @ApiModelProperty(value = "User's name", example = "John Smith", required = true)
        @JsonProperty("name")
        public String name;

        @ApiModelProperty(value = "User's email", example = "testuser@nasnav.com", required = true)
        @JsonProperty("email")
        public String email;
    }

    @ApiModel(value = "Password Reset Data")
    public static class PasswordResetObject {
        @ApiModelProperty(value = "Password reset token", example = "224c793yXg5hXyuqX", required = true)
        @JsonProperty("token")
        public String token;

        @ApiModelProperty(value = "New ueser's password", example = "PaSSworD", required = true)
        @JsonProperty("password")
        public String password;
    }

    @ApiModel(value = "User Login Data")
    public static class UserLoginObject {
        @ApiModelProperty(value = "User's email", example = "testuser@nasnav.com", required = true)
        @JsonProperty("email")
        public String email;

        @ApiModelProperty(value = "New ueser's password", example = "PaSSworD", required = true)
        @JsonProperty("password")
        public String password;
    }
}
