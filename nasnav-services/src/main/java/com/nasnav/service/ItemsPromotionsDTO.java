package com.nasnav.service;

import java.util.Map;
import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nasnav.dto.response.PromotionDTO;
import lombok.*;

@Data
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class ItemsPromotionsDTO {
    private Map<Long, List<Long>> productPromotionIds;
    private Map<Long, List<Long>> brandPromotionIds;
    private Map<Long, List<Long>> TagPromotionIds;
    private Map<Long, PromotionDTO> promotions;
}