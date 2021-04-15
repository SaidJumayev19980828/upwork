package com.nasnav.service;

import com.nasnav.dao.ProductExtraAttributesEntityRepository;
import com.nasnav.dao.ProductFeaturesRepository;
import com.nasnav.dao.ShopsRepository;
import com.nasnav.dao.TagsRepository;
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
import static com.nasnav.exceptions.ErrorCodes.S$0005;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
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


	@Override
	public List<CsvRow> exportProductsData(Long orgId, Long shopId) {

		if(shopId != null && !shopRepo.existsByIdAndOrganizationEntity_Id(shopId, orgId)){
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, S$0005, shopId, orgId);
		}

		SQLQuery<?> stocks = getExportQuery(orgId, shopId);

		List<ProductExportedData> result =
				template.query(stocks.getSQL().getSQL(),
						new BeanPropertyRowMapper<>(ProductExportedData.class));
		
		Map<Long, List<VariantExtraAtrribute>> extraAttributes = fetchVariantsExtraAttributes(orgId, shopId);
		Map<Long,List<ProductTagsBasicData>> productTags = createProductTagsMap(result);
		Map<Integer, ProductFeaturesEntity> features = createFeaturesMap();		
		return result
				.stream()
				.map(product -> toCsvRow(product, productTags, features, extraAttributes))
				.collect(toList());
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
		Long orgId = security.getCurrentUserOrganizationId();
		return feautreRepo
				.findByOrganizationId(orgId)
				.stream()
				.collect(toMap(ProductFeaturesEntity::getId, t -> t));
	}




	private Map<Long, List<ProductTagsBasicData>> createProductTagsMap(List<ProductExportedData> result) {
		List<Long> productIds = 
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
		CsvRow row = createCsvRow(productData);
		
		setTags(row, productData, productTags);
		setFeatures(row, productData, features);
		setExtraAttributes(row, productData, extraAttributes);
		return row;
	}
	
	
	
	private void setExtraAttributes(CsvRow row, ProductExportedData productData,
			Map<Long, List<VariantExtraAtrribute>> extraAttributes) {
		Map<String,String> extraAttrMap  = 
				extraAttributes
				.getOrDefault(productData.getVariantId(), emptyList())
				.stream()
				.collect(toMap(VariantExtraAtrribute::getName, VariantExtraAtrribute::getValue));
		row.setExtraAttributes(extraAttrMap);
	}



	private void setFeatures(CsvRow row, ProductExportedData productData,
			Map<Integer, ProductFeaturesEntity> featuresMap) {
		Map<String,String> features = 
				ofNullable(productData)
				.map(ProductExportedData::getFeatureSpec)
				.flatMap(this::toFeaturesJson)
				.map(json -> toFreaturesMap(json, featuresMap))
				.orElse(emptyMap());
		
		row.setFeatures(features);
	}
	
	
	
	private Map<String,String> toFreaturesMap(JSONObject json, Map<Integer, ProductFeaturesEntity> featuresMap){
		Map<String,String> features = new  HashMap<>();
		for(String key : json.keySet()) {
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
			JSONObject json = new JSONObject(jsonStr);
			return Optional.of(json);
		}catch(Throwable e) {
			return Optional.empty();
		}
	}

	
	
	
	
	private CsvRow createCsvRow(ProductExportedData data) {
		CsvRow row = new CsvRow();
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
		List<String> tagsList = getTagsNames(productData.getProductId(), productTags);
		if(tagsList.size() > 0) {
			String tags = toTagsString(tagsList);
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
		QStocks stock = QStocks.stocks;
		QProducts product = QProducts.products;
		QProductVariants variant = QProductVariants.productVariants;
		QBrands brand = QBrands.brands;
		QUnits unit = QUnits.units;

		SQLQuery<?> fromClause = getProductsBaseQuery(queryFactory, orgId, shopId);
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

		SQLQuery<?> stocks = queryFactory.from(productsQuery.as("total_products"));

		stocks.select((Expressions.template(CsvRow.class,"*")));

		return stocks;
	}





	private SQLQuery<?> getVariantsQuery(Long orgId) {
		QStocks stock = QStocks.stocks;
		QProducts product = QProducts.products;
		QProductVariants variant = QProductVariants.productVariants;
		QBrands brand = QBrands.brands;
		QUnits unit = QUnits.units;

		SQLQuery<?> fromClause = getOrganizationProductsBaseQuery(queryFactory, orgId);
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

		SQLQuery<?> stocks = queryFactory.from(productsQuery.as("total_products"));

		stocks.select((Expressions.template(CsvRow.class,"*")));

		return stocks;
	}



	private String toTagsString(List<String> tags) {
		String tagsString = "";
		for(String tag : tags) {
			tagsString += ";"+tag;
		}
		return tagsString.substring(1);
	}


	private SQLQuery<?> getProductsBaseQuery(SQLQueryFactory query, Long orgId, Long shopId) {
		QStocks stock = QStocks.stocks;
		QProducts product = QProducts.products;
		QProductVariants variant = QProductVariants.productVariants;
		QBrands brand = QBrands.brands;
		QUnits unit = QUnits.units;

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
		QStocks stock = QStocks.stocks;
		QProducts product = QProducts.products;
		QProductVariants variant = QProductVariants.productVariants;
		QBrands brand = QBrands.brands;
		QUnits unit = QUnits.units;

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
