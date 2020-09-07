package com.nasnav.dto.request.order.returned;


import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class ReceivedItemsDTO {
    private List<ReceivedItem> returnedItems;
    private List<ReceivedBasketItem> basketItems;
}
