package com.nasnav.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class BankActivityDetailsDTO {
    private Long amountIn;
    private Long amountOut;
    private LocalDateTime activityDate;
}
