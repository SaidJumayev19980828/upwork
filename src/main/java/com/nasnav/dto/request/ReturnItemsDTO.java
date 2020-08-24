package com.nasnav.dto.request;


import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nasnav.dto.*;
import lombok.Data;

import java.util.List;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class ReturnItemsDTO {

    List<ReturnedItem> returnedItems;
    List<ReturnedBasketItem> basketItems;
}
