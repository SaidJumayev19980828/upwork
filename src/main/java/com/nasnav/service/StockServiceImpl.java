package com.nasnav.service;

import com.nasnav.dao.BundleRepository;
import com.nasnav.dao.ProductRepository;
import com.nasnav.dao.StockRepository;
import com.nasnav.persistence.ProductEntity;
import com.nasnav.persistence.ProductTypes;
import com.nasnav.persistence.StocksEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class StockServiceImpl implements StockService {

    @Autowired
    StockRepository stockRepo;

    @Autowired
    ProductRepository productRepo;

    @Autowired
    BundleRepository bundleRepo;



    public List<StocksEntity> getProductStockForShop(Long productId, Long shopId) {
        Optional<ProductEntity> prodOpt = productRepo.findById(productId);
        //TODO : i think we should throw business exception here
        if(prodOpt == null || !prodOpt.isPresent())
            return null;

        List<StocksEntity> stocks  = stockRepo.findByProductEntity_IdAndShopsEntity_Id(productId, shopId);;

        //TODO : i think we should throw business exception here
        if(stocks == null || stocks.isEmpty())
            return null;

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
    public Integer getStockQuantity(StocksEntity stock){
        ProductEntity product = stock.getProductEntity();
        if(product == null){
            return stock.getQuantity();
        }

        Integer productType = product.getProductType();

        if(productType == ProductTypes.BUNDLE){
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
                .filter(s -> s.getProductEntity().getProductType() == ProductTypes.STOCK_ITEM)
                .mapToLong(stock -> stock.getQuantity())
                .sum();
    }
}
