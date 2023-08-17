package com.nasnav.dto.response;

import com.nasnav.dto.OrganizationRepresentationObject;
import com.nasnav.dto.UserRepresentationObject;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class BankAccountDTO {
    private Long id;
    private UserRepresentationObject user;
    private OrganizationRepresentationObject org;
    @NotNull
    @NotEmpty
    private String wallerAddress;

}
