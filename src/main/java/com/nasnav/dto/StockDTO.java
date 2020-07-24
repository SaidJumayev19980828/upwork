package com.nasnav.dto;

import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.HALF_EVEN;
import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nasnav.persistence.StocksEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper=false)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class StockDTO extends BaseRepresentationObject {
	private Long id;
	private Long shopId;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal discount;


    public StockDTO(StocksEntity entity, Long shopId){
    	this.id = entity.getId();
        this.shopId = shopId;
        this.quantity = ofNullable(entity.getQuantity()).orElse(0);
        this.price = ofNullable(entity.getPrice()).orElse(ZERO);
        this.discount = 
        		ofNullable(entity.getDiscount())
        		.map(discount -> calcDiscountPercentage(discount, this.price))
        		.orElse(null);
    }


	public StockDTO(StocksEntity stock) {
		this(stock, stock.getShopsEntity().getId());
	}
	
	
	
	private BigDecimal calcDiscountPercentage(BigDecimal discount, BigDecimal price) {
		if(isNull(price) || price.equals(ZERO)) {
			return null;
		}
		return discount
				.divide(price, 10, HALF_EVEN)
				.multiply(new BigDecimal("100"));
	}
}
