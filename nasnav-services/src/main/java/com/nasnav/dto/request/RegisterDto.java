package com.nasnav.dto.request;

import com.nasnav.dto.UserDTOs;
import com.nasnav.dto.request.organization.OrganizationCreationDTO;
import lombok.Data;

@Data
public class RegisterDto {

    private UserDTOs.EmployeeUserCreationObject employeeUserJson;

    private OrganizationCreationDTO organizationCreationDTO ;
}
