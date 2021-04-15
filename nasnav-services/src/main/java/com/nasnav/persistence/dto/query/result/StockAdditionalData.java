package com.nasnav.persistence.dto.query.result;

import com.nasnav.persistence.AddressesEntity;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class StockAdditionalData {
	private Long stockId;
	private Integer currency;
	private String variantBarcode;
	private String productName;
	private String variantSpecs;
	private Long shopId;
	private AddressesEntity shopAddress;
	private Long organizationId;
	private BigDecimal discount;
}
