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
    private Float amountIn;
    private Float amountOut;
    private LocalDateTime activityDate;
}
