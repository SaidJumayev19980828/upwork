package com.nasnav.service.impl;

import com.nasnav.dao.ExtraAttributesRepository;
import com.nasnav.dao.ProductExtraAttributesEntityRepository;
import com.nasnav.dao.ProductFeaturesRepository;
import com.nasnav.dao.ProductVariantsRepository;
import com.nasnav.dao.ShopsRepository;
import com.nasnav.dao.TagsRepository;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.ExtraAttributesEntity;
import com.nasnav.persistence.ProductFeaturesEntity;
import com.nasnav.persistence.ProductVariantsEntity;
import com.nasnav.persistence.dto.query.result.products.ProductTagsBasicData;
import com.nasnav.persistence.dto.query.result.products.export.ProductExportedData;
import com.nasnav.persistence.dto.query.result.products.export.VariantExtraAttribute;
import com.nasnav.querydsl.sql.QBrands;
import com.nasnav.querydsl.sql.QProductVariants;
import com.nasnav.querydsl.sql.QProducts;
import com.nasnav.querydsl.sql.QStocks;
import com.nasnav.querydsl.sql.QUnits;
import com.nasnav.request.OrderSearchParam;
import com.nasnav.response.OrdersListResponse;
import com.nasnav.service.DataExportService;
import com.nasnav.service.ImportExportHelper;
import com.nasnav.service.OrderService;
import com.nasnav.service.ProductService;
import com.nasnav.service.SecurityService;
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
import java.util.Set;

import static com.nasnav.commons.utils.CollectionUtils.divideToBatches;
import static com.nasnav.commons.utils.CollectionUtils.mapInBatches;
import static com.nasnav.enumerations.Roles.STORE_MANAGER;
import static com.nasnav.exceptions.ErrorCodes.S$0005;
import static com.nasnav.exceptions.ErrorCodes.S$0006;
import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
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

	@Autowired
	private OrderService orderService;

	@Override
	public List<CsvRow> exportProductsData(Long orgId, Long shopId) {
		validateShopId(orgId, shopId);

		var stocks = getExportQuery(orgId, shopId);

		var result =
				template.query(stocks.getSQL().getSQL(),
						new BeanPropertyRowMapper<>(ProductExportedData.class));

		Set<Long> variantsIds = result.stream()
				.map(ProductExportedData::getVariantId)
				.collect(toSet());
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

	@Override
	public OrdersListResponse exportOrdersData(OrderSearchParam params)  {
		return orderService.getAllOrdersList(params);
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




	private Map<Long, Map<String, String>> fetchVariantsExtraAttributes(Long orgId, Long shopId) {
		List<VariantExtraAttribute> variantAttributes = emptyList();
		if(shopId != null){
			variantAttributes =  prodExtraAttributeRepo.findByVariantShopId(shopId);
		}else{
			variantAttributes = prodExtraAttributeRepo.findByVariantOrgId(orgId);
		}
		return variantAttributes
				.stream()
				.collect(groupingBy(VariantExtraAttribute::getVariantId, toMap(VariantExtraAttribute::getName, VariantExtraAttribute::getValue)));
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
			, Map<Long, Map<String, String>> extraAttributes
			, Map<String, String> emptyFeatureValuesMap
			, Map<String, String> emptyExtraAttributesMap) {
		var row = createCsvRow(productData);

		setTags(row, productData, productTags);
		setFeatures(row, productData, features, emptyFeatureValuesMap);
		setExtraAttributes(row, productData, extraAttributes, emptyExtraAttributesMap);
		return row;
	}



	private void setExtraAttributes(CsvRow row, ProductExportedData productData, Map<Long, Map<String, String>> extraAttributes,
									Map<String, String> emptyExtraAttributesMap) {
		var extraAttrMap  =
				ofNullable(productData)
						.map(ProductExportedData::getVariantId)
						.map(extraAttributes::get)
						.orElse(new HashMap<>());
		for(Map.Entry<String, String> e : emptyExtraAttributesMap.entrySet()) {
			if (!extraAttrMap.containsKey(e.getKey()) )
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
						.orElse(new HashMap<>());
		for(Map.Entry<String, String> e : emptyFeatureValuesMap.entrySet()) {
			if (!features.containsKey(e.getKey()))
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

