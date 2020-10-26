package com.nasnav.service;

import com.nasnav.dao.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * operations that are used inside transactions.
 * Because the @Transactional annotation and logic only works when the method is called
 * from outside because it uses proxies
 * */
@Service
@Transactional(rollbackFor = Exception.class)
public class ProductServiceTransactions {
	
	@Autowired
	private ProductRepository productRepo;

	
	@Autowired
	private  ProductImagesRepository imgRepo;
	
	
	@Autowired
	private ProductVariantsRepository variantRepo;

	@Autowired
	private StockRepository stockRepo;
	@Autowired
	private CartItemRepository cartRepo;


	public void deleteProduct(Long productId) {
		cartRepo.deleteByProductId(productId);
		stockRepo.setProductStocksQuantityZero(productId);
		variantRepo.deleteByProductEntity_Id(productId);
		productRepo.deleteById(productId);
	}
}
