package com.nasnav.service;

import com.nasnav.dto.StockUpdateDTO;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.persistence.ProductVariantsEntity;
import com.nasnav.persistence.StocksEntity;
import com.nasnav.response.StockUpdateResponse;
import com.nasnav.service.model.VariantCache;

import java.util.List;

public interface StockService {
    List<StocksEntity> getProductStockForShop(Long productId, Long shopId) throws BusinessException;

    /**
     * if the product is bundle , its quantity is limited by the lowest quantity of its items.
     * if the bundle stock quantity is set to zero , then the bundle is not active anymore.
     * Set all stocks of the bundle to the calculated quantity.
     * */
    Integer getStockQuantity(StocksEntity stock);


    /**
     * @return the sum of actual stock items quantities in the given list.
     * Bundles and services stock items are excluded.
     * */
    Long getStockItemsQuantitySum(List<StocksEntity> stocks);

    
    /**
     * @return a list of stocks for the product variant that belongs to the given shop.
     * if no shop is provided, return stocks of all shops
     * */
	List<StocksEntity> getVariantStockForShop(ProductVariantsEntity variant, Long shopId) ;
	
	
	List<StocksEntity> getVariantStockForShop(Long variantId, Long shopId);
	
	
	StockUpdateResponse updateStock(StockUpdateDTO stockUpdateReq) throws BusinessException;

	List<Long> updateStockBatch(List<StockUpdateDTO> stocks);

	List<Long> updateStockBatch(List<StockUpdateDTO> stocks, VariantCache variantCache);
}
