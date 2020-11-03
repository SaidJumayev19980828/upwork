package com.nasnav.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class PromotionResponse {
    private Long total;
    private List<PromotionDTO> promotions;
}
