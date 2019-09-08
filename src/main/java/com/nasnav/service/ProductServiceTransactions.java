package com.nasnav.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nasnav.dao.ProductImagesRepository;
import com.nasnav.dao.ProductRepository;
import com.nasnav.dao.ProductVariantsRepository;


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

	
	
	public void deleteProduct(Long productId) {
		variantRepo.deleteByProductEntity_Id(productId);
		imgRepo.deleteByProductEntity_Id(productId);
		productRepo.deleteById(productId);
	}
}
