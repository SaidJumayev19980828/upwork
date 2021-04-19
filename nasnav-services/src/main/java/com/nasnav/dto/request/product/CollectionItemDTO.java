package com.nasnav.dto.request.product;


import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nasnav.constatnts.EntityConstants.Operation;
import lombok.Data;

import java.util.List;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class CollectionItemDTO {
    private Long productId;
    private List<Long> variantIds;
    private Operation operation;
}
