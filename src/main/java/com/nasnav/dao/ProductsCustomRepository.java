package com.nasnav.dao;

import java.util.Set;

import com.nasnav.request.ProductSearchParam;
import com.nasnav.service.model.ProductTagPair;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.sql.SQLQuery;

public interface ProductsCustomRepository {

	void batchInsertProductTags(Set<ProductTagPair> validProductTags);

	SQLQuery<?> getProductsBaseQuery(BooleanBuilder predicate, ProductSearchParam params);

}