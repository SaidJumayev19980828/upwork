package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
public class UserDTOs {
	
	@Data
    public static class GenericUserObject {

        @ApiModelProperty(value = "User's email", example = "testuser@nasnav.com", required = true)
        @JsonProperty("email")
        public String email;

        @ApiModelProperty(value = "Organization id that client wants to log into", example = "123", required = false)
        @JsonProperty("org_id")
        public Long orgId;
    }


    @ApiModel(value = "User Registration Data")
    public static class UserRegistrationObject extends GenericUserObject{
    	@ApiModelProperty(value = "User's name", example = "John Smith", required = true)
        @JsonProperty("name")
        public String name;
    }

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
    }

    @ApiModel(value = "User Login Data")
    public static class UserLoginObject extends GenericUserObject{

        @ApiModelProperty(value = "New user's password", example = "PaSSworD", required = true)
        @JsonProperty("password")
        public String password;
        
        @ApiModelProperty(value = "If user is an employee or not", example = "true", required = false)
        @JsonProperty("employee")
        public boolean employee;
    }

    @Data
    @ApiModel(value = "Employee User Creation Data")
	public static class EmployeeUserCreationObject extends GenericUserObject{
    	@ApiModelProperty(value = "User's name", example = "John Smith", required = true)
        @JsonProperty("name")
        public String name;
    	
        @ApiModelProperty(value = "Roles Separated by Comma", example = "STORE_ADMIN,ORGANIZATION_EMPLOYEE", required = true)
        @JsonProperty("role")
        public String role;

        @ApiModelProperty(value = "Organization Id", example = "123", required = true)
        @JsonProperty("org_id")
        public Long orgId;
        
        @ApiModelProperty(value = "Store Id", example = "1234", required = false)
        @JsonProperty("store_id")
        public Long storeId;
    }

    @Data
    @ApiModel(value = "User Updating Data")
    public static class EmployeeUserUpdatingObject extends GenericUserObject{
        @ApiModelProperty(value = "Updated user id", example = "1234", required = false)
        @JsonProperty("updated_user_id")
        private Long updatedUserId;

        @ApiModelProperty(value = "If user is an employee or not", example = "true", required = false)
        @JsonProperty("employee")
        public boolean employee;

        @ApiModelProperty(value = "User's name", example = "John Smith", required = false)
        @JsonProperty("name")
        private String name;

        @ApiModelProperty(value = "Roles Separated by Comma", example = "STORE_ADMIN,ORGANIZATION_EMPLOYEE", required = false)
        @JsonProperty("role")
        private String role;

        @ApiModelProperty(value = "Store Id", example = "1234", required = false)
        @JsonProperty("store_id")
        private Long storeId;

        @ApiModelProperty(value = "Avatar", example = "1234", required = false)
        @JsonProperty("avatar")
        private String avatar;

        @ApiModelProperty(value = "Gender", example = "Male", required = false)
        @JsonProperty("gender")
        private String gender;

        @ApiModelProperty(value = "Birth Date", example = "08/08/2010", required = false)
        @JsonProperty("birth_date")
        private String birthDate;

        @ApiModelProperty(value = "Address", example = "50 Kira st. Nasr City", required = false)
        @JsonProperty("address")
        private String address;

        @ApiModelProperty(value = "Country", example = "Egypt", required = false)
        @JsonProperty("country")
        private String addressCountry;

        @ApiModelProperty(value = "City", example = "Cairo", required = false)
        @JsonProperty("city")
        private String addressCity;

        @ApiModelProperty(value = "Phone Number", example = "01012345678", required = false)
        @JsonProperty("phone_number")
        private String phoneNumber;

        @ApiModelProperty(value = "Postal Code", example = "11595", required = false)
        @JsonProperty("post_code")
        private String postCode;

        @ApiModelProperty(value = "Flat Number", example = "8", required = false)
        @JsonProperty("flat_number")
        private Integer flatNumber;

        @ApiModelProperty(value = "User Image", example = "/images/image_568.jpg", required = false)
        @JsonProperty("image")
        private String image;
    }
}
