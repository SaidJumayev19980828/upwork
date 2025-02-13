package com.nasnav.service;

import com.nasnav.dto.ProductStockDTO;
import com.nasnav.dto.ProductStocksDTO;
import com.nasnav.dto.StockUpdateDTO;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.persistence.ProductVariantsEntity;
import com.nasnav.persistence.StocksEntity;
import com.nasnav.response.StockUpdateResponse;
import com.nasnav.service.model.VariantCache;

import java.util.List;
import java.util.Map;

public interface StockService {
    List<StocksEntity> getProductStockForShop(Long productId, Long shopId) ;

    /**
     * if the product is bundle , its quantity is limited by the lowest quantity of its items.
     * if the bundle stock quantity is set to zero , then the bundle is not active anymore.
     * Set all stocks of the bundle to the calculated quantity.
     * */
    Integer getStockQuantity(StocksEntity stock);


    
    /**
     * @return a list of stocks for the product variant that belongs to the given shop.
     * if no shop is provided, return stocks of all shops
     * */
	List<StocksEntity> getVariantStockForShop(ProductVariantsEntity variant, Long shopId) ;
	
	
	List<StocksEntity> getVariantStockForShop(Long variantId, Long shopId);
	
	
	StockUpdateResponse updateStock(StockUpdateDTO stockUpdateReq) throws BusinessException;

	void deleteStocks(Long shopId);

	List<Long> updateStockBatch(List<StockUpdateDTO> stocks);

	List<Long> updateStockBatch(List<StockUpdateDTO> stocks, VariantCache variantCache);
	
	void reduceStockBy(StocksEntity stocksEntity, Integer quantity);
	
	void incrementStockBy(StocksEntity stocksEntity, Integer quantity);

	void updateStockQuantity(StockUpdateDTO updateDto);

	Long updateStocks(ProductStocksDTO productStocksDTO) throws BusinessException;

	Map<Long, List<ProductStockDTO>> getProductStocks(Long productId);
}
