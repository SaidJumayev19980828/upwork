package com.nasnav.persistence.dto.query.result;

import com.nasnav.persistence.AddressesEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
public class StockAdditionalData {
	private Long stockId;
	private Integer currency;
	private Long variantId;
	private String variantBarcode;
	private String productName;
	private Long shopId;
	private AddressesEntity shopAddress;
	private Long organizationId;
	private BigDecimal discount;
	private Map<String, String> features;

	public StockAdditionalData(Long stockId, Integer currency, Long variantId, String variantBarcode, String productName,
							   Long shopId, AddressesEntity shopAddress, Long organizationId, BigDecimal discount) {
		this.stockId = stockId;
		this.currency = currency;
		this.variantId = variantId;
		this.variantBarcode = variantBarcode;
		this.productName = productName;
		this.shopId = shopId;
		this.shopAddress = shopAddress;
		this.organizationId = organizationId;
		this.discount = discount;
	}
}
