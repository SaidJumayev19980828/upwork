package com.nasnav.test.helpers;
import static java.util.Arrays.asList;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.nasnav.dao.BundleRepository;
import com.nasnav.dao.OrdersRepository;
import com.nasnav.dao.ProductRepository;
import com.nasnav.dao.ProductVariantsRepository;
import com.nasnav.dao.StockRepository;
import com.nasnav.persistence.BundleEntity;
import com.nasnav.persistence.OrdersEntity;
import com.nasnav.persistence.ProductEntity;
import com.nasnav.persistence.ProductVariantsEntity;
import com.nasnav.persistence.StocksEntity;

@Component 
public class TestHelper {
	
	@Autowired
	private BundleRepository bundleRepo;
	
	@Autowired
	private StockRepository stockRepo;
	
	@Autowired
	private ProductVariantsRepository variantRepo;
	
	@Autowired
	private OrdersRepository orderRepo;
	
	@Autowired
	private ProductRepository productRepo;
	
	
	@Transactional(readOnly = true)
	public Set<StocksEntity> getBundleItems(Long bundleId){
		BundleEntity bundle = bundleRepo.findById(bundleId).get();
		bundle.getItems().size(); // just to force fetching the elements inside this transaction
		return bundle.getItems();
	}
	
	
	
	
	
	@Transactional
	public  void deleteProductWithDependencies(ProductEntity productEntity) {
		Set<ProductVariantsEntity> variants = productEntity.getProductVariants();
		
		if(variants != null) {
					
			for( ProductVariantsEntity var : variants ){
				var.getStocks().forEach(stockRepo::delete);	
			};
			
			Set<Long> variantIds = variants.stream()
											.map(ProductVariantsEntity::getId)
											.collect(Collectors.toSet());
			
			productEntity.getProductVariants().clear();			
			variantIds.forEach(variantRepo::deleteById);
		}
		
		
	}
	
	
	
	@Transactional
	public StocksEntity getStockFullData(Long stockId) {
		StocksEntity stock = stockRepo.getOne(stockId);
		
		//call the getters inside the transaction to fetch and cache them by hibernate
		stock.getShopsEntity();
		stock.getProductVariantsEntity();
		stock.getOrganizationEntity();
		
		return stock;
	} 
	
	
	
	
	@Transactional
	public List<StocksEntity> getShopStocksFullData(Long shopId) {
		List<StocksEntity> stocks = stockRepo.findByShopsEntity_Id(shopId);
		for(StocksEntity stock: stocks) {
			stock.getShopsEntity();
			stock.getProductVariantsEntity().getId();
			stock.getProductVariantsEntity().getProductEntity().getId();
			stock.getOrganizationEntity();
		};
		
		return stocks;
	}
	
	
	@Transactional
	public ProductVariantsEntity getVariantFullData(Long id) {
		Optional<ProductVariantsEntity> variant = variantRepo.getVariantFullData(id);		
		return variant.get();
	}
	
	
	
	
	@Transactional
	public OrdersEntity getOrderEntityFullData(Long orderId) {
		return orderRepo.findFullDataById(orderId).get();
	}
	
	
	
	
	@Transactional
	public ProductEntity getProductFullData(Long productId) {
		return productRepo
				.findFullDataByIdIn(asList(productId))
				.stream()
				.findFirst()
				.get();
	}
		

}
