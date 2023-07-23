package com.nasnav.dto.response;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.time.LocalDateTime;

@Builder
@Getter
public class BankReservationDTO {
    private Long id;
    private BankAccountDTO account;
    private Long amount;
    private LocalDateTime activityDate;
    private Boolean fulfilled;
    private LocalDateTime fulfilledDate;
}
