package com.nasnav.dao;

import com.nasnav.querydsl.sql.*;
import com.nasnav.request.ProductSearchParam;
import com.nasnav.service.model.ProductAddonPair;
import com.nasnav.service.model.ProductTagPair;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.SQLQueryFactory;
import com.querydsl.sql.dml.SQLInsertClause;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.nasnav.commons.utils.StringUtils.isNotBlankOrNull;
import static com.querydsl.core.types.ExpressionUtils.count;
import static com.querydsl.sql.SQLExpressions.select;
import static com.querydsl.sql.SQLExpressions.selectDistinct;

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
		QShops shop = QShops.shops;
		QProducts product = QProducts.products;
		QProductVariants variant = QProductVariants.productVariants;
		QOrganizations organization = QOrganizations.organizations;

		SQLQuery<?> baseQuery = queryFactory.from(stock)
				.innerJoin(shop).on(stock.shopId.eq(shop.id))
				.innerJoin(variant).on(stock.variantId.eq(variant.id))
				.innerJoin(product).on(variant.productId.eq(product.id))
				.innerJoin(organization).on(product.organizationId.eq(organization.id))
				.where(predicate);

		addVariantsFeaturesAndTagsQuery(params, baseQuery);

		return baseQuery;
	}


	@Override
	public SQLQuery<?> getCollectionsBaseQuery(BooleanBuilder predicate, ProductSearchParam params) {
		QStocks stock = QStocks.stocks;
		QShops shop = QShops.shops;
		QProducts product = QProducts.products;
		QProductVariants variant = QProductVariants.productVariants;
		QProductCollections collection = QProductCollections.productCollections;
		QOrganizations organization = QOrganizations.organizations;

		SQLQuery<?> baseQuery = queryFactory.from(stock)
				.innerJoin(shop).on(stock.shopId.eq(shop.id))
				.innerJoin(variant).on(stock.variantId.eq(variant.id))
				.innerJoin(collection).on(variant.id.eq(collection.variantId))
				.innerJoin(product).on(product.id.eq(collection.productId))
				.innerJoin(organization).on(product.organizationId.eq(organization.id))
				.where(predicate);

		addVariantsFeaturesAndTagsQuery(params, baseQuery);

		return baseQuery;
	}

	private void addVariantsFeaturesAndTagsQuery(ProductSearchParam params, SQLQuery<?> baseQuery) {
		QProductVariants variant = QProductVariants.productVariants;
		QProductTags productTags = QProductTags.productTags;
		QProducts product = QProducts.products;

		if (isNotBlankOrNull(params.features)) {
			SQLQuery<Long> variantFeaturesQuery = getVariantFeaturesQuery(queryFactory, params);
			baseQuery.where(variant.id.in(variantFeaturesQuery));
		}

		SQLQuery<Long> productTagsQuery = getProductTagsQuery(queryFactory, productTags, params);
		if (productTagsQuery != null)
			baseQuery.where(product.id.in(productTagsQuery));
	}

	private SQLQuery<Long> getVariantFeaturesQuery(SQLQueryFactory query, ProductSearchParam params) {
		QProductFeatures feature = QProductFeatures.productFeatures;
		QVariantFeatureValues featureValue = QVariantFeatureValues.variantFeatureValues;
		BooleanBuilder featuresPredicate = new BooleanBuilder();

		for (Map.Entry<String, List<String>> e : params.features.entrySet()) {
			featuresPredicate.or(feature.name.eq(e.getKey()).and(featureValue.value.in(e.getValue())));
		}

		return query.select(featureValue.variantId)
				.from(featureValue)
				.join(feature).on(feature.id.eq(featureValue.featureId))
				.where(featuresPredicate)
				.groupBy(featureValue.variantId)
				.having(featureValue.countDistinct().eq((long)params.features.size()));
	}

	private SQLQuery<Long> getProductTagsQuery(SQLQueryFactory query, QProductTags productTags, ProductSearchParam params) {
		if (params.getTag_ids() == null)
			return null;

		return query.select(Expressions.numberPath(Long.class, "id"))
				.from(
						select(
								productTags.productId.as("id")
								, productTags.tagId.count().as("count"))
								.from(productTags)
								.where(productTags.tagId.in(params.getTag_ids()))
								.groupBy(productTags.productId)
								.having(productTags.tagId.count().eq((long) params.getTag_ids().size()))
								.as("productTags"));
	}

	@Override
	public SQLQuery<Long> getProductTagsByNameQuery(ProductSearchParam params) {
		if (params.getName() == null)
			return null;

		QTags tag = QTags.tags;
		QProductTags productTags = QProductTags.productTags;
		return select(productTags.productId)
				.from(productTags)
				.join(tag).on(tag.id.eq(productTags.tagId))
				.where(tag.name.lower().like(params.getName().toLowerCase()));
	}

	@Override
	public SQLQuery<Long> getProductTagsByCategoryNameQuery(ProductSearchParam params) {
		QTags tag = QTags.tags;
		QCategories category = QCategories.categories;
		QProductTags productTags = QProductTags.productTags;
		return select(productTags.productId)
				.from(productTags)
				.join(tag).on(tag.id.eq(productTags.tagId))
				.where(tag.categoryId.in(select(category.id)
										.from(category)
										.where(category.name.eq(params.getCategory_name()))));
	}

	@Override
	public SQLQuery<Long> getProductTagsByCategories(ProductSearchParam params) {
		QTags tag = QTags.tags;
		QProductTags productTags = QProductTags.productTags;
		return queryFactory.select(Expressions.numberPath(Long.class, "id"))
						.from(
							select(Expressions.numberPath(Long.class, "id"), Expressions.numberPath(Long.class, "category_id").count().as("count"))
							.from(
								selectDistinct(productTags.productId.as("id"), tag.categoryId.as("category_id"))
								.from(productTags)
								.join(tag).on(tag.id.eq(productTags.tagId))
								.where(tag.categoryId.in(params.getCategory_ids()))
								.as("productTags"))
							.groupBy(Expressions.numberPath(Long.class, "id"))
							.having(Expressions.numberPath(Long.class, "category_id").count().eq((long) params.getCategory_ids().size()))
						.as("productTags"));
	}
	
	private void insertProductTag(QProductTags productTags, SQLInsertClause insert, ProductTagPair productTag) {
		Long productId = productTag.getProductId();
		Long tagId = productTag.getTagId();
		
		insert
		.set(productTags.productId ,productId)
		.set(productTags.tagId, tagId)
		.addBatch();
	}

	@Override
	public void batchInsertProductAddons(Set<ProductAddonPair> validProductAddons) {
		QProductAddons productAddons = QProductAddons.productAddons; 
		SQLInsertClause insertClause = queryFactory.insert(productAddons);
		
		validProductAddons.forEach(productTag -> insertProductAddon(productAddons, insertClause, productTag));
		
		if(!validProductAddons.isEmpty()) {
			insertClause.execute();
		}
		
	}
	
	private void insertProductAddon(QProductAddons productTags, SQLInsertClause insert, ProductAddonPair productAddon) {
		Long productId = productAddon.getProductId();
		Long addonId = productAddon.getAddonId();
		
		insert
		.set(productTags.productId ,productId)
		.set(productTags.addonId, addonId)
		.addBatch();
	}
}
