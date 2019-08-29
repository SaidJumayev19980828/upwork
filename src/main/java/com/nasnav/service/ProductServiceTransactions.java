package com.nasnav.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nasnav.dao.ProductImagesRepository;
import com.nasnav.dao.ProductRepository;


/**
 * operations that are used inside transactions.
 * Because the @Transactional annotation and logic only works when the method is called
 * from outside because it uses proxies
 * */
@Service
@Transactional(rollbackFor = Exception.class)
public class ProductServiceTransactions {
	
	@Autowired
	private ProductRepository productRepository;

	
	@Autowired
	private  ProductImagesRepository productImagesRepository;
	
	
	public void deleteProduct(Long productId) {
		//TODO still need to implement business logic of removing empty stocks, and 
		//variants with empty stocks
		productImagesRepository.deleteByProductEntity_Id(productId);
		productRepository.deleteById(productId);
	}
}
