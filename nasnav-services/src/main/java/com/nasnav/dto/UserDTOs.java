package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

public class UserDTOs {

    @Getter
    @Schema(name = "User Registration Data")
    public static class UserRegistrationObject {
        @Schema(name = "User's email", example = "testuser@nasnav.com", required = true)
        @JsonProperty("email")
        public String email;

        @Schema(name = "Organization id that client wants to log into", example = "123", required = true)
        @JsonProperty("org_id")
        public Long orgId;

    	@Schema(name = "User's name", example = "John Smith", required = true)
        @JsonProperty("name")
        public String name;

        @JsonProperty("phone_number")
        private String phoneNumber;
    }

    @Getter
    @Schema(name = "User Registration Data")
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
    @Schema(name = "Password Reset Data")
    public static class PasswordResetObject {
        @Schema(name = "Password reset token", example = "224c793yXg5hXyuqX", required = true)
        @JsonProperty("token")
        public String token;

        @Schema(name = "New user's password", example = "PaSSworD", required = true)
        @JsonProperty("password")
        public String password;

        @Schema(name = "true if user is employee", example = "true", required = false)
        @JsonProperty("employee")
        public boolean employee;

        @Schema(name = "Organization id that client wants to log into", example = "123", required = true)
        @JsonProperty("org_id")
        public Long orgId;
    }

    @Getter
    @Schema(name = "User Login Data")
    public static class UserLoginObject {
        @Schema(name = "user's password", example = "PaSSworD", required = true)
        @JsonProperty("password")
        public String password;
        
        @Schema(name = "If user is an employee or not", example = "true", required = false)
        @JsonProperty("employee")
        public boolean employee;

        @Schema(name = "User's email", example = "testuser@nasnav.com", required = true)
        @JsonProperty("email")
        public String email;

        @Schema(name = "Organization id that client wants to log into", example = "123", required = true)
        @JsonProperty("org_id")
        public Long orgId;

        @JsonProperty("remember_me")
        public boolean rememberMe;
    }

    @Getter
    @Schema(name = "Employee User Creation Data")
	public static class EmployeeUserCreationObject {
        @Schema(name = "User's email", example = "testuser@nasnav.com", required = true)
        @JsonProperty("email")
        public String email;

        @Schema(name = "Organization id that client wants to log into", example = "123", required = true)
        @JsonProperty("org_id")
        public Long orgId;

    	@Schema(name = "User's name", example = "John Smith", required = true)
        @JsonProperty("name")
        public String name;

        @Schema(name = "Roles Separated by Comma", example = "STORE_ADMIN,ORGANIZATION_EMPLOYEE", required = true)
        @JsonProperty("role")
        public String role;
        
        @Schema(name = "Store Id", example = "1234", required = false)
        @JsonProperty("store_id")
        public Long storeId;

        @Schema(name = "Avatar", example = "99001/avatar.jpg", required = false)
        @JsonProperty("avatar")
        private String avatar;
    }

    @Getter
    @Schema(name = "User Updating Data")
    public static class EmployeeUserUpdatingObject {
        @Schema(name = "Updated user id", example = "1234", required = false)
        @JsonProperty("updated_user_id")
        private Long updatedUserId;

        @Schema(name = "If user is an employee or not", example = "true", required = false)
        public boolean employee;

        @Schema(name = "User's email", example = "testuser@nasnav.com", required = false)
        public String email;

        @Schema(name = "Organization id that client wants to log into", example = "123", required = false)
        @JsonProperty("org_id")
        public Long orgId;

        @Schema(name = "User's name", example = "John Smith", required = false)
        private String name;

        @Schema(name = "Roles Separated by Comma", example = "STORE_ADMIN,ORGANIZATION_EMPLOYEE", required = false)
        private String role;

        @Schema(name = "Store Id", example = "1234", required = false)
        @JsonProperty("store_id")
        private Long storeId;

        @Schema(name = "Avatar", example = "1234", required = false)
        private String avatar;

        @Schema(name = "Gender", example = "Male", required = false)
        private String gender;

        @Schema(name = "Birth Date", example = "08/08/2010", required = false)
        @JsonProperty("birth_date")
        private String birthDate;

        @Schema(name = "Phone Number", example = "01012345678", required = false)
        @JsonProperty("phone_number")
        private String phoneNumber;

        private String mobile;

        @Schema(name = "User Image", example = "/images/image_568.jpg", required = false)
        private String image;

        @JsonProperty("first_name")
        private String firstName;

        @JsonProperty("last_name")
        private String lastName;


    }
}
