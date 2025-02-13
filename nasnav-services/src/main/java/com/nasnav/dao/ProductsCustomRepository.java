package com.nasnav.dao;

import com.nasnav.request.ProductSearchParam;
import com.nasnav.service.model.ProductAddonPair;
import com.nasnav.service.model.ProductTagPair;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.sql.SQLQuery;

import java.util.Set;

public interface ProductsCustomRepository {

	void batchInsertProductTags(Set<ProductTagPair> validProductTags);
	
	void batchInsertProductAddons(Set<ProductAddonPair> validProductAddons);


	SQLQuery<?> getProductsBaseQuery(BooleanBuilder predicate, ProductSearchParam params);

	SQLQuery<?> getCollectionsBaseQuery(BooleanBuilder predicate, ProductSearchParam params);

	SQLQuery<Long> getProductTagsByNameQuery(ProductSearchParam params);

	SQLQuery<Long> getProductTagsByCategoryNameQuery(ProductSearchParam params);

	SQLQuery<Long> getProductTagsByCategories(ProductSearchParam params);
}