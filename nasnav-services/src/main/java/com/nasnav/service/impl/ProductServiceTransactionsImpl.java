package com.nasnav.service.impl;

import com.nasnav.dao.*;
import com.nasnav.service.ProductServiceTransactions;

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
public class ProductServiceTransactionsImpl implements ProductServiceTransactions {
	
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


	@Override
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

	@Override
	public void deleteVariants(List<Long> variantIds, Boolean forceDelete) {
		if (isBlankOrNull(variantIds)){
			return;
		}
		processInBatches(variantIds, 5000, cartRepo::deleteByVariantIdIn);
		processInBatches(variantIds, 5000, stockRepo::setVariantStocksQuantityZero);
		if (forceDelete) {
			processInBatches(variantIds, 5000, collectionItemRepo::deleteItemsByVariantIds);
		}
		processInBatches(variantIds, 5000, variantRepo::deleteByIdIn);
	}

	@Override
	public void deleteBundle(Long bundleId){
		List<Long> idAsList = singletonList(bundleId);
		cartRepo.deleteByProductIdIn(idAsList);
		stockRepo.setProductStocksQuantityZero(idAsList);
		variantRepo.deleteAllByProductIdIn(idAsList);
		bundleRepo.deleteById(bundleId);
	}
}
