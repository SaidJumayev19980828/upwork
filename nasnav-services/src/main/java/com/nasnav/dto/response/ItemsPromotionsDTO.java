package com.nasnav.dto.response;

import java.util.Map;
import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.*;

@Data
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class ItemsPromotionsDTO {
    private Map<Long, List<Long>> productPromotionIds;
    private Map<Long, List<Long>> brandPromotionIds;
    private Map<Long, List<Long>> tagPromotionIds;
    private Map<Long, PromotionDTO> promotions;
}