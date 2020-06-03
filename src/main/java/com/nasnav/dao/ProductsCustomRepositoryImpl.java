package com.nasnav.dao;

import java.util.Set;

import com.nasnav.model.querydsl.sql.QProductVariants;
import com.nasnav.model.querydsl.sql.QProducts;
import com.nasnav.model.querydsl.sql.QStocks;
import com.nasnav.request.ProductSearchParam;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.sql.SQLQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.nasnav.model.querydsl.sql.QProductTags;
import com.nasnav.service.model.ProductTagPair;
import com.querydsl.sql.SQLQueryFactory;
import com.querydsl.sql.dml.SQLInsertClause;

import static com.querydsl.sql.SQLExpressions.select;

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
	
	@Override
	public SQLQuery<?> getProductsBaseQuery(BooleanBuilder predicate, ProductSearchParam params) {
		QStocks stock = QStocks.stocks;
		QProducts product = QProducts.products;
		QProductVariants variant = QProductVariants.productVariants;
		QProductTags productTags = QProductTags.productTags;;

		SQLQuery<?> baseQuery = queryFactory.from(stock)
				.innerJoin(variant).on(stock.variantId.eq(variant.id))
				.innerJoin(product).on(variant.productId.eq(product.id))
				.where(predicate);

		SQLQuery<?> productTagsQuery = getProductTagsQuery(queryFactory, productTags, params);

		if (productTagsQuery != null)
			baseQuery.where(product.id.in((com.querydsl.core.types.Expression<? extends Long>) productTagsQuery));

		return baseQuery;
	}


	private SQLQuery<Long> getProductTagsQuery(SQLQueryFactory query, QProductTags productTags, ProductSearchParam params) {
		if (params.getTags() == null)
			return null;

		return query.select(Expressions.numberPath(Long.class, "id"))
				.from(
						select(
								productTags.productId.as("id")
								, productTags.tagId.count().as("count"))
								.from(productTags)
								.where(productTags.tagId.in(params.getTags()))
								.groupBy(productTags.productId)
								.having(productTags.tagId.count().eq((long) params.getTags().size()))
								.as("productTags"));
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
