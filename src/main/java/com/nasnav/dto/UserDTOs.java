package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;

public class UserDTOs {

    @Getter
    @ApiModel(value = "User Registration Data")
    public static class UserRegistrationObject {
        @ApiModelProperty(value = "User's email", example = "testuser@nasnav.com", required = true)
        @JsonProperty("email")
        public String email;

        @ApiModelProperty(value = "Organization id that client wants to log into", example = "123", required = true)
        @JsonProperty("org_id")
        public Long orgId;

    	@ApiModelProperty(value = "User's name", example = "John Smith", required = true)
        @JsonProperty("name")
        public String name;

        @JsonProperty("phone_number")
        private String phoneNumber;
    }

    @Getter
    @ApiModel(value = "User Registration Data")
    public static class UserRegistrationObjectV2 {
        public String name;
        public String email;
        public String password;
        @JsonProperty("org_id")
        public Long orgId;
        @JsonProperty("confirmation_flag")
        public Boolean confirmationFlag;
        @JsonProperty("redirect_url")
        private String redirectUrl;

        @JsonProperty("phone_number")
        private String phoneNumber;
    }

    @Getter
    @ApiModel(value = "Password Reset Data")
    public static class PasswordResetObject {
        @ApiModelProperty(value = "Password reset token", example = "224c793yXg5hXyuqX", required = true)
        @JsonProperty("token")
        public String token;

        @ApiModelProperty(value = "New user's password", example = "PaSSworD", required = true)
        @JsonProperty("password")
        public String password;

        @ApiModelProperty(value = "true if user is employee", example = "true", required = false)
        @JsonProperty("employee")
        public boolean employee;

        @ApiModelProperty(value = "Organization id that client wants to log into", example = "123", required = true)
        @JsonProperty("org_id")
        public Long orgId;
    }

    @Getter
    @ApiModel(value = "User Login Data")
    public static class UserLoginObject {
        @ApiModelProperty(value = "user's password", example = "PaSSworD", required = true)
        @JsonProperty("password")
        public String password;
        
        @ApiModelProperty(value = "If user is an employee or not", example = "true", required = false)
        @JsonProperty("employee")
        public boolean employee;

        @ApiModelProperty(value = "User's email", example = "testuser@nasnav.com", required = true)
        @JsonProperty("email")
        public String email;

        @ApiModelProperty(value = "Organization id that client wants to log into", example = "123", required = true)
        @JsonProperty("org_id")
        public Long orgId;

        @JsonProperty("remember_me")
        public boolean rememberMe;
    }

    @Getter
    @ApiModel(value = "Employee User Creation Data")
	public static class EmployeeUserCreationObject {
        @ApiModelProperty(value = "User's email", example = "testuser@nasnav.com", required = true)
        @JsonProperty("email")
        public String email;

        @ApiModelProperty(value = "Organization id that client wants to log into", example = "123", required = true)
        @JsonProperty("org_id")
        public Long orgId;

    	@ApiModelProperty(value = "User's name", example = "John Smith", required = true)
        @JsonProperty("name")
        public String name;

        @ApiModelProperty(value = "Roles Separated by Comma", example = "STORE_ADMIN,ORGANIZATION_EMPLOYEE", required = true)
        @JsonProperty("role")
        public String role;
        
        @ApiModelProperty(value = "Store Id", example = "1234", required = false)
        @JsonProperty("store_id")
        public Long storeId;

        @ApiModelProperty(value = "Avatar", example = "99001/avatar.jpg", required = false)
        @JsonProperty("avatar")
        private String avatar;
    }

    @Getter
    @ApiModel(value = "User Updating Data")
    public static class EmployeeUserUpdatingObject {
        @ApiModelProperty(value = "Updated user id", example = "1234", required = false)
        @JsonProperty("updated_user_id")
        private Long updatedUserId;

        @ApiModelProperty(value = "If user is an employee or not", example = "true", required = false)
        public boolean employee;

        @ApiModelProperty(value = "User's email", example = "testuser@nasnav.com", required = false)
        public String email;

        @ApiModelProperty(value = "Organization id that client wants to log into", example = "123", required = false)
        @JsonProperty("org_id")
        public Long orgId;

        @ApiModelProperty(value = "User's name", example = "John Smith", required = false)
        private String name;

        @ApiModelProperty(value = "Roles Separated by Comma", example = "STORE_ADMIN,ORGANIZATION_EMPLOYEE", required = false)
        private String role;

        @ApiModelProperty(value = "Store Id", example = "1234", required = false)
        @JsonProperty("store_id")
        private Long storeId;

        @ApiModelProperty(value = "Avatar", example = "1234", required = false)
        private String avatar;

        @ApiModelProperty(value = "Gender", example = "Male", required = false)
        private String gender;

        @ApiModelProperty(value = "Birth Date", example = "08/08/2010", required = false)
        @JsonProperty("birth_date")
        private String birthDate;

        private AddressDTO address;

        @ApiModelProperty(value = "Phone Number", example = "01012345678", required = false)
        @JsonProperty("phone_number")
        private String phoneNumber;

        private String mobile;

        @ApiModelProperty(value = "User Image", example = "/images/image_568.jpg", required = false)
        private String image;
    }
}
