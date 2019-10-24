package com.nasnav.test.helpers;
import java.util.List;
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
	private ProductRepository productRepo;
	
	@Autowired
	private ProductVariantsRepository variantRepo;
	
	
	@Autowired
	private OrdersRepository orderRepo;
	
	
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
		stocks.forEach(s ->{
			s.getShopsEntity();
			s.getProductVariantsEntity().getProductEntity();
			s.getOrganizationEntity();
		});
		
		return stocks;
	}
	
	
	@Transactional
	public ProductVariantsEntity getVariantFullData(Long id) {
		ProductVariantsEntity updatedVariant = variantRepo.getOne(id);
		
		//just call them to make hibernate cache these entities while being in the transaction
		updatedVariant.getProductEntity();
		updatedVariant.getStocks();
		
		return updatedVariant;
	}
	
	
	
	
	@Transactional
	public OrdersEntity getOrderEntityFullData(Long orderId) {
		OrdersEntity order = orderRepo.findById(orderId).get();
		
		//just call them to make hibernate cache these entities while being in the transaction
		order.getBasketsEntity().stream().forEach( i-> i.toString());		
		order.getOrganizationEntity();
		order.getShopsEntity();
		
		return order;
	}
		

}
