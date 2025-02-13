package com.nasnav.dto.response.navbox;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.util.Map;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class WishlistItemQuantity {
    private Long itemId;
    private Integer quantity;
    private Long orgId;
    private Map<String,Object> additionalData;
}
