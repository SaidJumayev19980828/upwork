package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;

public class UserDTOs {

    @Getter
    @Schema(name = "User Registration Data")
    public static class UserRegistrationObject {
        @Schema(example = "testuser@nasnav.com", required = true)
        public String email;

        @Schema(example = "123", required = true)
        @JsonProperty("org_id")
        public Long orgId;

    	@Schema(example = "John Smith", required = true)
        public String name;

        @JsonProperty("phone_number")
        private String phoneNumber;
        @JsonProperty("date_of_birth")
        private String dateOfBirth;
    }

    @Getter
    @Setter
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
        private String avatar;
        @JsonProperty("phone_number")
        private String phoneNumber;
        @JsonProperty(value = "activation_method",defaultValue = "VERIFICATION_LINK")
        private ActivationMethod activationMethod;
    }

    @Getter
    @Schema(name = "Password Reset Data")
    public static class PasswordResetObject {
        @Schema(example = "224c793yXg5hXyuqX", required = true)
        public String token;

        @Schema(example = "PaSSworD", required = true)
        public String password;

        @Schema(example = "true")
        public boolean employee;

        @Schema(example = "123", required = true)
        @JsonProperty("org_id")
        public Long orgId;
    }

    @Getter
    @Schema(name = "User Login Data")
    public static class UserLoginObject {
        @Schema(example = "PaSSworD", required = true)
        public String password;
        
        @Schema(example = "true")
        public boolean employee;

        @Schema(example = "testuser@nasnav.com", required = true)
        public String email;

        @Schema(example = "123", required = true)
        @JsonProperty("org_id")
        public Long orgId;

        @JsonProperty("remember_me")
        public boolean rememberMe;

        @Schema(example = "YYYYYYYYYY:XXXXXXXXXXXX")
        @JsonProperty("notification_token")
        private String notificationToken;
    }

    @Getter
    @Setter
    @Schema(name = "Employee User Creation Data")
	public static class EmployeeUserCreationObject {
        @Schema(example = "testuser@nasnav.com", required = true)
        public String email;

        @Schema(example = "123", required = true)
        @JsonProperty("org_id")
        public Long orgId;

    	@Schema(example = "John Smith", required = true)
        public String name;

        @Schema(example = "STORE_MANAGER,ORGANIZATION_EMPLOYEE", required = true)
        public String role;
        
        @Schema(example = "1234")
        @JsonProperty("shop_id")
        public Long storeId;

        @Schema(example = "99001/avatar.jpg")
        private String avatar;

    }

    @Getter
    @Schema(name = "User Updating Data")
    public static class EmployeeUserUpdatingObject {
        @Schema(example = "1234")
        @JsonProperty("updated_user_id")
        private Long updatedUserId;

        @Schema(example = "true")
        public boolean employee;

        @Schema(example = "testuser@nasnav.com")
        public String email;

        @Schema(example = "123")
        @JsonProperty("org_id")
        public Long orgId;

        @Schema(example = "John Smith")
        private String name;

        @Schema(example = "STORE_MANAGER,ORGANIZATION_EMPLOYEE")
        private String role;

        @Schema(example = "1234")
        @JsonProperty("shop_id")
        private Long storeId;

        @Schema(example = "1234")
        private String avatar;

        @Schema(example = "Male")
        private String gender;

        @Schema(example = "08/08/2010")
        @JsonProperty("birth_date")
        private String birthDate;

        @Schema(example = "01012345678")
        @JsonProperty("phone_number")
        private String phoneNumber;

        private String mobile;

        @Schema(example = "/images/image_568.jpg")
        private String image;

        @JsonProperty("first_name")
        private String firstName;

        @JsonProperty("last_name")
        private String lastName;

        @JsonProperty("tier_id")
        private Long tierId;

        @JsonProperty("family_id")
        private Long familyId;
    }
}
