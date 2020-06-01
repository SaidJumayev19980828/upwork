package com.nasnav.dao;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.nasnav.model.querydsl.sql.QProductTags;
import com.nasnav.service.model.ProductTagPair;
import com.querydsl.sql.SQLQueryFactory;
import com.querydsl.sql.dml.SQLInsertClause;

@Repository
@Transactional
public class ProductsCustomRepositoryImpl implements ProductsCustomRepository {
	
	@Autowired
	private SQLQueryFactory queryFactory;
	
	
	@Override
	public void batchInsertProductTags(Set<ProductTagPair> validProductTags) {
		QProductTags productTags = QProductTags.productTags; 
		SQLInsertClause insertClause = queryFactory.insert(productTags);
		
		validProductTags.forEach(productTag -> insertProductTag(productTags, insertClause, productTag));
		
		if(!validProductTags.isEmpty()) {
			insertClause.execute();
		}
	}
	
	
	
	private void  insertProductTag(QProductTags productTags, SQLInsertClause insert,
			ProductTagPair productTag) {
		Long productId = productTag.getProductId();
		Long tagId = productTag.getTagId();
		
		insert
		.set(productTags.productId ,productId)
		.set(productTags.tagId, tagId)
		.addBatch();
	}
}
