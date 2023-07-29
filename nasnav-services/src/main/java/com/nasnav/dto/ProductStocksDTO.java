package com.nasnav.dto;

import java.util.List;

import com.nasnav.dto.StockUpdateDTO;

import lombok.Data;
@Data
public class ProductStocksDTO {
	private Long shopId;
	private List<StockUpdateDTO> stocks;
	

}
