package com.nasnav.dao;

import com.nasnav.request.ProductSearchParam;
import com.nasnav.service.model.ProductTagPair;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.sql.SQLQuery;

import java.util.Set;

public interface ProductsCustomRepository {

	void batchInsertProductTags(Set<ProductTagPair> validProductTags);

	SQLQuery<?> getProductsBaseQuery(BooleanBuilder predicate, ProductSearchParam params);

	SQLQuery<?> getCollectionsBaseQuery(BooleanBuilder predicate, ProductSearchParam params);

	SQLQuery<Long> getProductTagsByNameQuery(ProductSearchParam params);
}