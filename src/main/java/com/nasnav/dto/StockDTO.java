package com.nasnav.dto;

import static com.nasnav.commons.utils.MathUtils.calculatePercentage;
import static java.math.BigDecimal.ZERO;
import static java.util.Optional.ofNullable;

import java.math.BigDecimal;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nasnav.persistence.CountriesEntity;
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
	private String unit;
	private String currencyValue;

    public StockDTO(StocksEntity entity, Long shopId){
    	this.id = entity.getId();
        this.shopId = shopId;
        this.quantity = ofNullable(entity.getQuantity()).orElse(0);
        this.price = ofNullable(entity.getPrice()).orElse(ZERO);
        this.unit = ofNullable(entity.getUnit()).orElse("");
		CountriesEntity country = entity.getOrganizationEntity().getCountry();
		if (country != null){
			this.currencyValue = country.getCurrency();
		}
        this.discount = 
        		ofNullable(entity.getDiscount())
        		.map(discount -> calcDiscountPercentage(discount, this.price))
        		.orElse(null);
    }


	public StockDTO(StocksEntity stock) {
		this(stock, stock.getShopsEntity().getId());
	}
	
	
	
	private BigDecimal calcDiscountPercentage(BigDecimal discount, BigDecimal price) {
		return calculatePercentage(discount, price);
	}
}
