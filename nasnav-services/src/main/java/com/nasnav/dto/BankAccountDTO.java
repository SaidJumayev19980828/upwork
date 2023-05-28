package com.nasnav.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
public class BankAccountDTO {
    private Long id;
    private UserRepresentationObject user;
    private OrganizationRepresentationObject org;
    @NotNull
    @NotEmpty
    private String wallerAddress;
    private Long openingBalance;
    private LocalDateTime openingBalanceDate;
    private Boolean locked;
}
