package com.nasnav.service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.nasnav.dao.BundleRepository;
import com.nasnav.dao.ProductRepository;
import com.nasnav.dao.StockRepository;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.persistence.ProductEntity;
import com.nasnav.persistence.ProductTypes;
import com.nasnav.persistence.ProductVariantsEntity;
import com.nasnav.persistence.StocksEntity;

@Service
public class StockServiceImpl implements StockService {

    @Autowired
    StockRepository stockRepo;

    @Autowired
    ProductRepository productRepo;

    @Autowired
    BundleRepository bundleRepo;



    public List<StocksEntity> getProductStockForShop(Long productId, Long shopId) throws BusinessException {
        Optional<ProductEntity> prodOpt = productRepo.findById(productId);
        
        if(prodOpt == null || !prodOpt.isPresent())
            throw new BusinessException(
            		String.format("No product exists with id [%d]!",productId)
            		, "INVALID PARAM:product_id"
            		, HttpStatus.NOT_ACCEPTABLE);

        List<StocksEntity> stocks  = stockRepo.findByProductIdAndShopsId(productId, shopId);;

        if(stocks == null || stocks.isEmpty())
        	throw new BusinessException(
            		String.format("Product with id [%d] has no stocks!",productId)
            		, "INVALID PARAM:product_id"
            		, HttpStatus.NOT_ACCEPTABLE);

        stocks.stream().forEach(this::updateStockQuantity);

        return stocks;
    }



    private void updateStockQuantity(StocksEntity stock) {
        if(stock == null)
            return;
        stock.setQuantity(getStockQuantity(stock));
    }


    /**
     * if the product is bundle , its quantity is limited by the lowest quantity of its items.
     * if the bundle stock quantity is set to zero , then the bundle is not active anymore.
     * Set all stocks of the bundle to the calculated quantity.
     * */
    @Transactional
    public Integer getStockQuantity(StocksEntity stock){
        ProductEntity product = Optional.ofNullable(stock.getProductVariantsEntity())
        								.map(ProductVariantsEntity::getProductEntity)
        								.orElse(null);
        if(product == null){
            return stock.getQuantity();
        }

        Integer productType = product.getProductType();

        if( productType.equals(ProductTypes.BUNDLE) ){
        	if(stock.getQuantity().equals(0))
        		return 0;
        	else 
        		return bundleRepo.getStockQuantity(product.getId());
        }else{
            return stock.getQuantity();
        }
    }



    /**
     * @return the sum of actual stock items quantities in the given list.
     * Bundles and services stock items are excluded.
     * */
    public Long getStockItemsQuantitySum(List<StocksEntity> stocks) {
        return stocks.stream()        		
                .filter(this::isPhysicalProduct)
                .mapToLong(stock -> stock.getQuantity())
                .sum();
    }
    
    
    
    
    public Boolean isPhysicalProduct(StocksEntity stock) {
    	return Optional.ofNullable(stock)
		    			.map(StocksEntity::getProductVariantsEntity)
		    			.map(ProductVariantsEntity::getProductEntity)
		    			.filter(product -> Objects.equals( product.getProductType(), ProductTypes.STOCK_ITEM) )
		    			.isPresent();
    }


    

	@Override
	public List<StocksEntity> getVariantStockForShop(ProductVariantsEntity variant, Long shopId) throws BusinessException {

        List<StocksEntity> stocks  = stockRepo.findByShopsEntity_IdAndProductVariantsEntity_Id(shopId, variant.getId());

        if(stocks == null || stocks.isEmpty())
        	throw new BusinessException(
            		String.format("Product Variant with id [%d] has no stocks!", variant.getId())
            		, "INVALID PARAM:product_id"
            		, HttpStatus.NOT_ACCEPTABLE);

        stocks.stream().forEach(this::updateStockQuantity);

        return stocks;
	}
}
