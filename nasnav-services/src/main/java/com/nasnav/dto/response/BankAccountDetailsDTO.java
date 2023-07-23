package com.nasnav.dto.response;

import com.nasnav.dto.OrganizationRepresentationObject;
import com.nasnav.dto.UserRepresentationObject;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
public class BankAccountDetailsDTO extends BankAccountDTO{
    private Long openingBalance;
    private LocalDateTime openingBalanceDate;
    private Boolean locked;
    private BankBalanceSummary summary;
}
