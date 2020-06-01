package com.nasnav.dao;

import java.util.Set;

import com.nasnav.service.model.ProductTagPair;

public interface ProductsCustomRepository {

	void batchInsertProductTags(Set<ProductTagPair> validProductTags);

}