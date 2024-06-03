package com.nasnav.dto.referral_code;


import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InfluencerReferralConstraints {

    private LocalDate startDate;
    private LocalDate endDate;

    @Schema(description = "Percentage of the cashback that will be multiplied to amount then adding the result to wallet", example = "0.02")
    private BigDecimal cashbackPercentage;

    @Schema(description = "Value of the cashback that will be added to the wallet", example = "20.00")
    private BigDecimal cashbackValue;

    @Schema(description = "Discount value that will be subtracted from the order amount", example = "20.00")
    private BigDecimal discountValue;

    @Schema(description = "Discount percentage that will be subtracted from the order amount", example = "0.02")
    private BigDecimal discountPercentage;

    @Schema(description = "Allowed products that the influencer referral discount will appleid to it")
    private List<Long> products;

}
