package com.nasnav.dto.request.order.returned;


import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class ReceivedItemsDTO {
    private List<ReceivedItem> returnedItems;
    private List<ReceivedBasketItem> basketItems;

    public ReceivedItemsDTO(){
        this.returnedItems = new ArrayList<>();
        this.basketItems = new ArrayList<>();
    }
}
