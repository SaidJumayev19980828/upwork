package com.nasnav.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Data
@EqualsAndHashCode
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class ProductPromotionDto {
    private Long id;
    private String identifier;
    private String name;
    private String description;
    private String type;
    private String code;
    private ZonedDateTime dateStart;
    private ZonedDateTime dateEnd;
    private PromosConstraints constrains;
    private Long organizationId;
    private String appliedPromotion;
}
