package com.nasnav.service;

import com.nasnav.dao.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.nasnav.commons.utils.CollectionUtils.processInBatches;
import static com.nasnav.commons.utils.StringUtils.isBlankOrNull;
import static java.util.Collections.singletonList;


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
	private ProductCollectionItemRepository collectionItemRepo;

	@Autowired
	private StockRepository stockRepo;

	@Autowired
	private CartItemRepository cartRepo;

	@Autowired
	private BundleRepository bundleRepo;


	public void deleteProducts(List<Long> productIds, Boolean forceDelete) {
		if (isBlankOrNull(productIds)){
			return;
		}
		processInBatches(productIds, 5000, cartRepo::deleteByProductIdIn);
		processInBatches(productIds, 5000, stockRepo::setProductStocksQuantityZero);
		if (forceDelete) {
			processInBatches(productIds, 5000, collectionItemRepo::deleteItemsByProductIds);
		}
		processInBatches(productIds, 5000, variantRepo::deleteAllByProductIdIn);
		processInBatches(productIds, 5000, productRepo::deleteAllByIdIn);
	}




	public void deleteBundle(Long bundleId){
		List<Long> idAsList = singletonList(bundleId);
		cartRepo.deleteByProductIdIn(idAsList);
		stockRepo.setProductStocksQuantityZero(idAsList);
		variantRepo.deleteAllByProductIdIn(idAsList);
		bundleRepo.deleteById(bundleId);
	}
}
