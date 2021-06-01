package com.nasnav.service;

import com.nasnav.dao.ProductExtraAttributesEntityRepository;
import com.nasnav.dao.ProductFeaturesRepository;
import com.nasnav.dao.ShopsRepository;
import com.nasnav.dao.TagsRepository;
import com.nasnav.enumerations.Roles;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.querydsl.sql.*;
import com.nasnav.persistence.ProductFeaturesEntity;
import com.nasnav.persistence.dto.query.result.products.ProductTagsBasicData;
import com.nasnav.persistence.dto.query.result.products.export.ProductExportedData;
import com.nasnav.persistence.dto.query.result.products.export.VariantExtraAtrribute;
import com.nasnav.service.model.importproduct.csv.CsvRow;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.sql.SQLExpressions;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.SQLQueryFactory;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.nasnav.commons.utils.CollectionUtils.divideToBatches;
import static com.nasnav.enumerations.Roles.STORE_MANAGER;
import static com.nasnav.exceptions.ErrorCodes.*;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.*;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;

@Service
public class DataExportServiceImpl implements DataExportService{
	@Autowired
	private JdbcTemplate template;
	
	@Autowired
	private TagsRepository tagsRepo;
	
	@Autowired
	private ProductFeaturesRepository feautreRepo;
	
	@Autowired
	private SecurityService security;
	
	@Autowired
	private ProductExtraAttributesEntityRepository prodExtraAttributeRepo;
	
	@Autowired
	private SQLQueryFactory queryFactory;

	@Autowired
	private ShopsRepository shopRepo;

	@Autowired
	protected ImportExportHelper helper;


	@Override
	public List<CsvRow> exportProductsData(Long orgId, Long shopId) {
		validateShopId(orgId, shopId);

		var stocks = getExportQuery(orgId, shopId);

		var result =
				template.query(stocks.getSQL().getSQL(),
						new BeanPropertyRowMapper<>(ProductExportedData.class));

		var extraAttributes = fetchVariantsExtraAttributes(orgId, shopId);
		var productTags = createProductTagsMap(result);
		var features = createFeaturesMap();
		return result
				.stream()
				.map(product -> toCsvRow(product, productTags, features, extraAttributes))
				.collect(toList());
	}



	private void validateShopId(Long orgId, Long shopId) {
		if(isNull(shopId) && security.currentUserHasMaxRoleLevelOf(STORE_MANAGER)){
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, S$0006);
		}else if(nonNull(shopId)){
			shopRepo.findByIdAndOrganizationEntity_IdAndRemoved(shopId, orgId, 0)
					.orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, S$0005, shopId, orgId));
			helper.validateAdminCanManageTheShop(shopId);
		}
	}


	private SQLQuery<?> getExportQuery(Long orgId, Long shopId) {
		if(shopId == null){
			return getVariantsQuery(orgId);
		}else{
			return  getStocksQuery(orgId, shopId);
		}
	}




	private Map<Long, List<VariantExtraAtrribute>> fetchVariantsExtraAttributes(Long orgId, Long shopId) {
		List<VariantExtraAtrribute> variantAttributes = emptyList();
		if(shopId != null){
			variantAttributes =  prodExtraAttributeRepo.findByVariantShopId(shopId);
		}else{
			variantAttributes = prodExtraAttributeRepo.findByVariantOrgId(orgId);
		}
		return variantAttributes
				.stream()
				.collect(groupingBy(VariantExtraAtrribute::getVariantId));
	}



	private Map<Integer, ProductFeaturesEntity> createFeaturesMap() {
		var orgId = security.getCurrentUserOrganizationId();
		return feautreRepo
				.findByOrganizationId(orgId)
				.stream()
				.collect(toMap(ProductFeaturesEntity::getId, t -> t));
	}




	private Map<Long, List<ProductTagsBasicData>> createProductTagsMap(List<ProductExportedData> result) {
		var productIds =
				result
				.stream()
				.map(ProductExportedData::getProductId)
				.collect(toList()); 
		return divideToBatches(productIds, 500)
				.stream()
				.map(tagsRepo::getTagsByProductIdIn)
				.flatMap(List::stream)
				.collect(groupingBy(ProductTagsBasicData::getProductId));
	}




	private CsvRow toCsvRow(ProductExportedData productData
			, Map<Long,List<ProductTagsBasicData>> productTags
			, Map<Integer, ProductFeaturesEntity> features
			, Map<Long, List<VariantExtraAtrribute>> extraAttributes) {
		var row = createCsvRow(productData);
		
		setTags(row, productData, productTags);
		setFeatures(row, productData, features);
		setExtraAttributes(row, productData, extraAttributes);
		return row;
	}
	
	
	
	private void setExtraAttributes(CsvRow row, ProductExportedData productData,
			Map<Long, List<VariantExtraAtrribute>> extraAttributes) {
		var extraAttrMap  =
				extraAttributes
				.getOrDefault(productData.getVariantId(), emptyList())
				.stream()
				.collect(toMap(VariantExtraAtrribute::getName, VariantExtraAtrribute::getValue));
		row.setExtraAttributes(extraAttrMap);
	}



	private void setFeatures(CsvRow row, ProductExportedData productData,
			Map<Integer, ProductFeaturesEntity> featuresMap) {
		var features =
				ofNullable(productData)
				.map(ProductExportedData::getFeatureSpec)
				.flatMap(this::toFeaturesJson)
				.map(json -> toFreaturesMap(json, featuresMap))
				.orElse(emptyMap());
		
		row.setFeatures(features);
	}
	
	
	
	private Map<String,String> toFreaturesMap(JSONObject json, Map<Integer, ProductFeaturesEntity> featuresMap){
		Map<String,String> features = new  HashMap<>();
		for(var key : json.keySet()) {
			Optional.of(key)
			.map(k -> Integer.valueOf(k))
			.map(featuresMap::get)
			.map(ProductFeaturesEntity::getName)
			.ifPresent( name -> features.put(name, json.getString(key)));
		}
		return features;
	}
	
	

	private Optional<JSONObject> toFeaturesJson(String jsonStr) {
		try {
			var json = new JSONObject(jsonStr);
			return Optional.of(json);
		}catch(Throwable e) {
			return Optional.empty();
		}
	}

	
	
	
	
	private CsvRow createCsvRow(ProductExportedData data) {
		var row = new CsvRow();
		row.setBarcode(data.getBarcode());
		row.setBrand(data.getBrand());
		row.setDescription(data.getDescription());
		row.setDiscount(data.getDiscount());
		row.setExternalId(row.getExternalId());
		row.setName(data.getName());
		row.setPrice(data.getPrice());
		row.setProductGroupKey(data.getProductId().toString());
		row.setProductId(data.getProductId());
		row.setQuantity(data.getQuantity());
		row.setVariantId(data.getVariantId());
		row.setSku(data.getSku());
		row.setProductCode(data.getProductCode());
		row.setUnit(data.getUnitName());
		row.setWeight(data.getWeight());
		return row;
	}




	private void setTags(CsvRow row, ProductExportedData productData,
			Map<Long, List<ProductTagsBasicData>> productTags) {
		var tagsList = getTagsNames(productData.getProductId(), productTags);
		if(tagsList.size() > 0) {
			var tags = toTagsString(tagsList);
			row.setTags(tags);
		} else {
			row.setTags("");
		}
	}

	
	
	

	private List<String> getTagsNames(Long productId, Map<Long, List<ProductTagsBasicData>> productTags) {
		return ofNullable(productId)
				.map(productTags::get)
				.orElse(emptyList())
				.stream()
				.map(ProductTagsBasicData::getTagName)
				.collect(toList());
	}




	private SQLQuery<?> getStocksQuery(Long orgId, Long shopId) {
		var stock = QStocks.stocks;
		var product = QProducts.products;
		var variant = QProductVariants.productVariants;
		var brand = QBrands.brands;
		var unit = QUnits.units;

		var fromClause = getProductsBaseQuery(queryFactory, orgId, shopId);
		SQLQuery<?> productsQuery = fromClause.select(
											stock.quantity,
											stock.price,
											stock.discount,
											unit.name.as("unit_name"),
											product.organizationId.as("organization_id"),
											variant.id.as("variant_id"),
											variant.featureSpec,
											variant.barcode.as("barcode"),
											brand.name.as("brand"),
											product.description.as("description"),
											variant.name.as("name"),
											product.id.as("product_id"),
											product.hide.as("hide"),
											variant.sku.as("sku"),
											variant.productCode.as("product_code"),
											variant.weight.as("weight"),
											SQLExpressions.rowNumber()
													.over()
													.partitionBy(product.id)
													.orderBy(stock.price).as("row_num"));

		var stocks = queryFactory.from(productsQuery.as("total_products"));

		stocks.select((Expressions.template(CsvRow.class,"*")));

		return stocks;
	}





	private SQLQuery<?> getVariantsQuery(Long orgId) {
		var stock = QStocks.stocks;
		var product = QProducts.products;
		var variant = QProductVariants.productVariants;
		var brand = QBrands.brands;
		var unit = QUnits.units;

		var fromClause = getOrganizationProductsBaseQuery(queryFactory, orgId);
		SQLQuery<?> productsQuery = fromClause
											.distinct()
											.select(
											unit.name.as("unit_name"),
											product.organizationId.as("organization_id"),
											variant.id.as("variant_id"),
											variant.featureSpec,
											variant.barcode.as("barcode"),
											brand.name.as("brand"),
											product.description.as("description"),
											variant.name.as("name"),
											product.id.as("product_id"),
											product.hide.as("hide"),
											variant.sku.as("sku"),
											variant.productCode.as("product_code"),
											variant.weight.as("weight"));

		var stocks = queryFactory.from(productsQuery.as("total_products"));

		stocks.select((Expressions.template(CsvRow.class,"*")));

		return stocks;
	}



	private String toTagsString(List<String> tags) {
		var tagsString = "";
		for(var tag : tags) {
			tagsString += ";"+tag;
		}
		return tagsString.substring(1);
	}


	private SQLQuery<?> getProductsBaseQuery(SQLQueryFactory query, Long orgId, Long shopId) {
		var stock = QStocks.stocks;
		var product = QProducts.products;
		var variant = QProductVariants.productVariants;
		var brand = QBrands.brands;
		var unit = QUnits.units;

		return query.from(stock)
				.innerJoin(variant).on(stock.variantId.eq(variant.id))
				.innerJoin(product).on(variant.productId.eq(product.id))
				.innerJoin(brand).on(product.brandId.eq(brand.id))
				.leftJoin(unit).on(stock.unitId.eq(unit.id))
				.where(product.organizationId.eq(orgId)
						.and(stock.shopId.eq(shopId))
						.and(product.removed.eq(0)));
	}



	private SQLQuery<?> getOrganizationProductsBaseQuery(SQLQueryFactory query, Long orgId) {
		var stock = QStocks.stocks;
		var product = QProducts.products;
		var variant = QProductVariants.productVariants;
		var brand = QBrands.brands;
		var unit = QUnits.units;

		return query.from(stock)
				.distinct()
				.innerJoin(variant).on(stock.variantId.eq(variant.id))
				.innerJoin(product).on(variant.productId.eq(product.id))
				.innerJoin(brand).on(product.brandId.eq(brand.id))
				.leftJoin(unit).on(stock.unitId.eq(unit.id))
				.where(product.organizationId.eq(orgId)
						.and(product.removed.eq(0)));
	}
	

}
