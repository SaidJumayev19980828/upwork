package com.nasnav.dto;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import com.nasnav.persistence.StocksEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper=false)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class StockDTO extends BaseRepresentationObject {
	private Long shopId;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal discount;


    public StockDTO(StocksEntity entity, Long shopId){
        this.shopId = shopId;
        this.quantity = entity.getQuantity();
        this.price = entity.getPrice();
        this.discount = entity.getDiscount();
    }
}
