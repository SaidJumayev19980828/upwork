package com.nasnav.dto.request.order.returned;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class ReceivedItem {
    private Long returnRequestItemId;
    private Integer receivedQuantity;
}
