package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
public class UserDTOs {
	
	
    public static class GenericUserObject {

        @ApiModelProperty(value = "User's email", example = "testuser@nasnav.com", required = true)
        @JsonProperty("email")
        public String email;
        
        @ApiModelProperty(value = "Organization id that client wants to log into", example = "123", required = false)
        @JsonProperty("org_id")
        public Integer org_id;
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

    @ApiModel(value = "Employee User Creation Data")
	public class EmployeeUserCreationObject extends GenericUserObject{
    	@ApiModelProperty(value = "User's name", example = "John Smith", required = true)
        @JsonProperty("name")
        public String name;
    	
        @ApiModelProperty(value = "Roles Separated by Comma", example = "STORE_ADMIN,ORGANIZATION_EMPLOYEE", required = true)
        @JsonProperty("role")
        public String role;
        
        @ApiModelProperty(value = "Store Id", example = "1234", required = false)
        @JsonProperty("store_id")
        public Long store_id;
    }
}
