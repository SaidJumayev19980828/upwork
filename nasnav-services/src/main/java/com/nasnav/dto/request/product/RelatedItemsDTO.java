package com.nasnav.dto.request.product;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.util.List;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class RelatedItemsDTO {
    private boolean add;
    private Long productId;
    private List<Long> relatedProductsIds;
}
