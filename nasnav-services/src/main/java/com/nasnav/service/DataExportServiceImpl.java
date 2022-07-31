package com.nasnav.service;

import com.nasnav.dao.*;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.ExtraAttributesEntity;
import com.nasnav.persistence.ProductVariantsEntity;
import com.nasnav.querydsl.sql.*;
import com.nasnav.persistence.ProductFeaturesEntity;
import com.nasnav.persistence.dto.query.result.products.ProductTagsBasicData;
import com.nasnav.persistence.dto.query.result.products.export.ProductExportedData;
import com.nasnav.persistence.dto.query.result.products.export.VariantExtraAttribute;
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
import static com.nasnav.commons.utils.CollectionUtils.mapInBatches;
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
	private ProductVariantsRepository variantsRepo;
	@Autowired
	private ExtraAttributesRepository extraAttributesRepo;
	@Autowired
	private ProductExtraAttributesEntityRepository prodExtraAttributeRepo;
	@Autowired
	private ShopsRepository shopRepo;

	@Autowired
	private SecurityService security;
	@Autowired
	private ProductService productService;

	@Autowired
	private SQLQueryFactory queryFactory;

	@Autowired
	protected ImportExportHelper helper;


	@Override
	public List<CsvRow> exportProductsData(Long orgId, Long shopId) {
		validateShopId(orgId, shopId);

		var stocks = getExportQuery(orgId, shopId);

		var result =
				template.query(stocks.getSQL().getSQL(),
						new BeanPropertyRowMapper<>(ProductExportedData.class));

		List<Long> variantsIds = result.stream()
				.map(ProductExportedData::getVariantId)
				.collect(toList());
		Map<Long, Map<String, String>> variantsFeaturesMap = mapInBatches(variantsIds, 1000, variantsRepo::findByIdIn)
				.stream()
				.collect(toMap(ProductVariantsEntity::getId, variant -> productService.parseVariantFeatures(variant, 0)));

		Map<String, String> emptyFeatureValuesMap = feautreRepo.findByOrganizationId(orgId)
				.stream()
				.collect(toMap(ProductFeaturesEntity::getName, f -> "" ));

		Map<String, String> emptyExtraAttributesMap = extraAttributesRepo.findByOrganizationId(orgId)
				.stream()
				.collect(toMap(ExtraAttributesEntity::getName, v -> "" ));

		var extraAttributes = fetchVariantsExtraAttributes(orgId, shopId);
		var productTags = createProductTagsMap(result);
		return result
				.stream()
				.map(product -> toCsvRow(product, productTags, variantsFeaturesMap, extraAttributes, emptyFeatureValuesMap, emptyExtraAttributesMap))
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




	private Map<Long, List<VariantExtraAttribute>> fetchVariantsExtraAttributes(Long orgId, Long shopId) {
		List<VariantExtraAttribute> variantAttributes = emptyList();
		if(shopId != null){
			variantAttributes =  prodExtraAttributeRepo.findByVariantShopId(shopId);
		}else{
			variantAttributes = prodExtraAttributeRepo.findByVariantOrgId(orgId);
		}
		return variantAttributes
				.stream()
				.collect(groupingBy(VariantExtraAttribute::getVariantId));
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
			, Map<Long, Map<String, String>> features
			, Map<Long, List<VariantExtraAttribute>> extraAttributes
			, Map<String, String> emptyFeatureValuesMap
			, Map<String, String> emptyExtraAttributesMap) {
		var row = createCsvRow(productData);
		
		setTags(row, productData, productTags);
		setFeatures(row, productData, features, emptyFeatureValuesMap);
		setExtraAttributes(row, productData, extraAttributes, emptyExtraAttributesMap);
		return row;
	}
	
	
	
	private void setExtraAttributes(CsvRow row, ProductExportedData productData,
			Map<Long, List<VariantExtraAttribute>> extraAttributes, Map<String, String> emptyExtraAttributesMap) {
		var extraAttrMap  =
				extraAttributes
				.getOrDefault(productData.getVariantId(), emptyList())
				.stream()
				.collect(toMap(VariantExtraAttribute::getName, VariantExtraAttribute::getValue));
		for(Map.Entry<String, String> e : emptyExtraAttributesMap.entrySet()) {
			if (!extraAttrMap.containsKey(e.getKey()) && !e.getValue().isEmpty())
				extraAttrMap.put(e.getKey(), e.getValue());
		}

		row.setExtraAttributes(extraAttrMap);
	}



	private void setFeatures(CsvRow row, ProductExportedData productData,
							 Map<Long, Map<String, String>> featuresMap,
							 Map<String, String> emptyFeatureValuesMap) {
		Map<String, String> features =
				ofNullable(productData)
				.map(ProductExportedData::getVariantId)
				.map(featuresMap::get)
				.orElse(emptyMap());
		for(Map.Entry<String, String> e : emptyFeatureValuesMap.entrySet()) {
			if (!features.containsKey(e.getKey()) && !e.getValue().isEmpty())
				features.put(e.getKey(), e.getValue());
		}
		
		row.setFeatures(features);
	}
	
	
	
	private Map<String,String> toFreaturesMap(JSONObject json, Map<Integer, ProductFeaturesEntity> featuresMap){
		Map<String,String> features = new  HashMap<>();
		for(var key : json.keySet()) {
			Optional.of(key)
			.map(Integer::valueOf)
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
											variant.barcode.as("barcode"),
											brand.name.as("brand"),
											variant.description.as("description"),
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
											variant.barcode.as("barcode"),
											brand.name.as("brand"),
											variant.description.as("description"),
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
						.and(product.removed.eq(0))
						.and(variant.removed.eq(0)));
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
						.and(product.removed.eq(0))
						.and(variant.removed.eq(0)));
	}
	

}
