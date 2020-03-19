package com.nasnav.service;

import static com.nasnav.commons.utils.EntityUtils.nonIsEmpty;
import static com.nasnav.commons.utils.EntityUtils.noneIsNull;
import static com.nasnav.commons.utils.StringUtils.*;
import static com.nasnav.constatnts.EntityConstants.Operation.CREATE;
import static com.nasnav.constatnts.EntityConstants.Operation.UPDATE;
import static com.nasnav.constatnts.error.product.ProductSrvErrorMessages.ERR_CANNOT_DELETE_BUNDLE_ITEM;
import static com.nasnav.constatnts.error.product.ProductSrvErrorMessages.ERR_CANNOT_DELETE_PRODUCT_BY_OTHER_ORG_USER;
import static com.nasnav.constatnts.error.product.ProductSrvErrorMessages.ERR_CANNOT_DELETE_PRODUCT_USED_IN_NEW_ORDERS;
import static com.nasnav.constatnts.error.product.ProductSrvErrorMessages.ERR_INVALID_EXTRA_ATTR_STRING;
import static com.nasnav.constatnts.error.product.ProductSrvErrorMessages.ERR_PRODUCT_DELETE_FAILED;
import static com.nasnav.constatnts.error.product.ProductSrvErrorMessages.ERR_PRODUCT_HAS_NO_VARIANTS;
import static com.nasnav.constatnts.error.product.ProductSrvErrorMessages.ERR_PRODUCT_READ_FAIL;
import static com.nasnav.constatnts.error.product.ProductSrvErrorMessages.ERR_PRODUCT_STILL_USED;
import static com.nasnav.constatnts.error.product.ProductSrvErrorMessages.ERR_PRODUCT_NOT_EXISTS;
import static java.lang.String.format;
import static java.util.Comparator.comparing;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.*;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.criteria.*;
import javax.sql.DataSource;

import com.nasnav.dto.*;
import com.nasnav.model.querydsl.sql.QBrands;
import com.nasnav.model.querydsl.sql.QProductFeatures;
import com.nasnav.model.querydsl.sql.QProductTags;
import com.nasnav.model.querydsl.sql.QProductVariants;
import com.nasnav.model.querydsl.sql.QProducts;
import com.nasnav.model.querydsl.sql.QStocks;
import com.nasnav.response.*;
import com.nasnav.integration.events.data.StockEventParam;
import com.nasnav.integration.microsoftdynamics.webclient.dto.Stocks;
import com.nasnav.integration.sallab.webclient.dto.Product;
import com.nasnav.persistence.*;
import com.querydsl.core.BooleanBuilder;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.sql.*;
import org.apache.commons.beanutils.BeanUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.commons.enums.SortOrder;
import com.nasnav.commons.utils.EntityUtils;
import com.nasnav.commons.utils.StringUtils;
import com.nasnav.constatnts.EntityConstants.Operation;
import com.nasnav.dao.BasketRepository;
import com.nasnav.dao.BrandsRepository;
import com.nasnav.dao.BundleRepository;
import com.nasnav.dao.EmployeeUserRepository;
import com.nasnav.dao.ExtraAttributesRepository;
import com.nasnav.dao.ProductFeaturesRepository;
import com.nasnav.dao.ProductImagesRepository;
import com.nasnav.dao.ProductRepository;
import com.nasnav.dao.ProductVariantsRepository;
import com.nasnav.dao.StockRepository;
import com.nasnav.dao.TagsRepository;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.request.BundleSearchParam;
import com.nasnav.request.ProductSearchParam;
import com.sun.istack.logging.Logger;

@Service
public class ProductService {

	private Logger logger = Logger.getLogger(ProductService.class);

	//	@Value("${products.default.start}")
	private Integer defaultStart = 0;
	//	@Value("${products.default.count}")
	private Integer defaultCount = 10;
	//	@Value("${products.default.sort.attribute}")
	private String defaultSortAttribute = "creation_date";
	//	@Value("${products.default.order}")
	private String defaultOrder = "desc";

	private final ProductRepository productRepository;

	private final BundleRepository bundleRepository;

	private final StockRepository stockRepository;

	private final ProductImagesRepository productImagesRepository;

	private final ProductVariantsRepository productVariantsRepository;

	private final ProductFeaturesRepository productFeaturesRepository;

	private final StockService stockService;

	@Autowired
	private  FileService fileService;


	@Autowired
	private EmployeeUserRepository empRepo;


	@Autowired
	private BrandsRepository brandRepo;

	@Autowired
	private EntityManager em;

	@Autowired
	private BasketRepository basketRepo;

	@Autowired
	private ProductServiceTransactions transactions;

	@Autowired
	private SecurityService securityService;

	@Autowired
	private ProductImageService imgService;

	@Autowired
	private TagsRepository orgTagRepo;

	@Autowired
	private ExtraAttributesRepository extraAttrRepo;

	@Autowired
	private DataSource dataSource;

	@Autowired
	private JdbcTemplate template;

	@Autowired
	public ProductService(ProductRepository productRepository, StockRepository stockRepository,
	                      ProductVariantsRepository productVariantsRepository, ProductImagesRepository productImagesRepository,
	                      ProductFeaturesRepository productFeaturesRepository , BundleRepository bundleRepository,
	                      StockService stockService) {
		this.productRepository = productRepository;
		this.stockRepository = stockRepository;
		this.productImagesRepository = productImagesRepository;
		this.productVariantsRepository = productVariantsRepository;
		this.productFeaturesRepository = productFeaturesRepository;
		this.bundleRepository = bundleRepository;
		this.stockService = stockService;
	}




	public ProductDetailsDTO getProduct(Long productId, Long shopId) throws BusinessException{
		ProductEntity product =
				productRepository
					.findById( ofNullable(productId).orElse(-1L) )
					.orElseThrow(() ->
						new BusinessException(
								format(ERR_PRODUCT_NOT_EXISTS, productId)
								, "INVALID PARAM: product_id"
								, NOT_FOUND));

		List<ProductVariantsEntity> productVariants = getProductVariants(product);

		return createProductDetailsDTO(product, shopId, productVariants);
	}





	private ProductDetailsDTO createProductDetailsDTO(ProductEntity product, Long shopId, List<ProductVariantsEntity> productVariants) throws BusinessException {

		List<VariantDTO> variantsDTOList = getVariantsList(productVariants, product.getId(), shopId);
		List<TagsRepresentationObject> tagsDTOList = getProductTagsDTOList(product.getId());

		ProductDetailsDTO productDTO = null;
		try {
			productDTO = toProductDetailsDTO(product);
			productDTO.setVariants(variantsDTOList);
			if (variantsDTOList != null && variantsDTOList.size() > 1)
				productDTO.setMultipleVariants(true);
				productDTO.setVariantFeatures( getVariantFeatures(productVariants) );
				productDTO.setBundleItems( getBundleItems(product));
				productDTO.setImages( getProductImages(product.getId() ) );
				productDTO.setTags(tagsDTOList);
		} catch (IllegalAccessException | InvocationTargetException e) {
			logger.log(Level.SEVERE, e.getMessage(), e );
			throw new BusinessException(
					format(ERR_PRODUCT_READ_FAIL, product.getId())
					,"INTERNAL SERVER ERROR"
					, INTERNAL_SERVER_ERROR);
		}

		return productDTO;
	}

	private List<TagsRepresentationObject> getProductTagsDTOList(Long productId) {
		return orgTagRepo.findByIdIn(productRepository.getTagsByProductId(productId)
				.stream()
				.mapToLong(BigInteger::longValue).boxed()
				.collect(Collectors.toList()))
				.stream()
				.map(tag ->(TagsRepresentationObject) tag.getRepresentation())
				.collect(Collectors.toList());
	}

	private Map<Long,List<TagsRepresentationObject>> getProductsTagsDTOList(List<Long> productsIds) {
		Map<Long,List<TagsRepresentationObject>> result = new HashMap<>();

		List<Pair> productsTags = productRepository.getTagsByProductIdIn(productsIds);
		List<TagsEntity> productsTagsDTOs =  orgTagRepo.findByIdIn(productsTags.stream()
																			   .map(p -> p.getSecond())
																			   .distinct()
																			   .collect(toList()));
		for (Long productId: productsIds) {
			List<Long> productTagsIds = productsTags.stream()
													.filter(pair -> pair.getFirst().equals(productId))
													.map(pair -> pair.getSecond())
													.collect(Collectors.toList());
			List<TagsRepresentationObject> productTagsDTOs = productsTagsDTOs.stream()
															   .filter(tag -> productTagsIds.contains(tag.getId()))
															   .map(tag -> (TagsRepresentationObject)tag.getRepresentation())
															   .collect(toList());
			result.put(productId, productTagsDTOs);
		}

		return result;
	}

	private List getProductsVariantsMultipleFlag(List<Long> productsIds) {
		SQLQueryFactory queryFactory = new SQLQueryFactory(new Configuration(new PostgreSQLTemplates()), dataSource);

		QProducts product = QProducts.products;
		QProductVariants variant = QProductVariants.productVariants;

		SQLQuery query = queryFactory.select(product.id)
								.from(variant).innerJoin(product).on(product.id.eq(variant.productId))
								.where(product.id.in(productsIds))
								.having(variant.id.count().gt(1))
								.groupBy(product.id);

		return query.fetch();
	}


	private List<ProductVariantsEntity> getProductVariants(ProductEntity product) throws BusinessException {
		List<ProductVariantsEntity> productVariants = productVariantsRepository.findByProductEntity_Id(product.getId());
		if (productVariants == null || productVariants.isEmpty()) {
			throw new BusinessException(
					String.format(ERR_PRODUCT_HAS_NO_VARIANTS, product.getId())
					, "INVALID DATA"
					, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return productVariants;
	}

	private List<ProductRepresentationObject> getBundleItems(ProductEntity product) {

		List<Long> bundleProductsIdList = bundleRepository.getBundleItemsProductIds(product.getId());
		List<ProductEntity> bundleProducts = this.getProductsByIds(bundleProductsIdList , "asc", "name");
		ProductsResponse response = this.getProductsResponse(bundleProducts,"asc" , "name" ,  (long)bundleProducts.size() );
		List<ProductRepresentationObject> productRepList = response == null? new ArrayList<>() : response.getProducts();
		return productRepList;
	}


	private List<VariantDTO> getVariantsList(List<ProductVariantsEntity> productVariants, Long productId, Long shopId) throws BusinessException{

		return productVariants.stream()
				.map(variant -> createVariantDto(shopId, variant))
				.filter( variant -> !variant.getStocks().isEmpty())
				.collect(toList());
	}




	private VariantDTO createVariantDto(Long shopId, ProductVariantsEntity variant)  {
		VariantDTO variantObj = new VariantDTO();
		variantObj.setId(variant.getId());
		variantObj.setBarcode( variant.getBarcode() );
		variantObj.setStocks( getStockList(variant, shopId) );
		variantObj.setVariantFeatures( getVariantFeaturesValues(variant) );
		variantObj.setImages( getProductVariantImages(variant.getId()) );
		variantObj.setExtraAttributes( getExtraAttributesList(variant));
		return variantObj;
	}




	private List<ExtraAttributeDTO> getExtraAttributesList(ProductVariantsEntity variant) {
		return variant
				.getExtraAttributes()
				.stream()
				.filter(attr -> nonNull(attr.getExtraAttribute()))
				.map(this::toExtraAttributeDTO)
				.collect(toList());
	}





	private ExtraAttributeDTO toExtraAttributeDTO(ProductExtraAttributesEntity entity) {
		ExtraAttributeDTO dto = new ExtraAttributeDTO();
		ExtraAttributesEntity extraAttrEntity = entity.getExtraAttribute();
		dto.setId(extraAttrEntity.getId());
		dto.setIconUrl(extraAttrEntity.getIconUrl());
		dto.setName(extraAttrEntity.getName());
		dto.setType(extraAttrEntity.getType());
		dto.setValue(entity.getValue());
		return dto;
	}




	private Map<String,String> getVariantFeaturesValues(ProductVariantsEntity variant) {
		if(variant == null || !hasFeatures(variant))
			return null;

		JacksonJsonParser parser = new JacksonJsonParser();
		Map<String, Object> keyValueMap =  parser.parseMap(variant.getFeatureSpec());
		return keyValueMap.entrySet()
				.stream()
				.map(this::getVariantFeatureMapEntry)
				.filter(entry -> entry != null)
				.collect(Collectors.toMap(Map.Entry::getKey , Map.Entry::getValue));
	}





	private Map.Entry<String,String> getVariantFeatureMapEntry(Map.Entry<String,Object> entry) {
		if(entry == null || entry.getKey() == null)
			return null;

		Integer id = Integer.parseInt(entry.getKey());
		Optional<ProductFeaturesEntity> featureOptional = productFeaturesRepository.findById(id);
		if(!featureOptional.isPresent())
			return null;

		return new AbstractMap.SimpleEntry<>(
				featureOptional.get().getPname()
				, entry.getValue().toString());

	}





	private List<VariantFeatureDTO> getVariantFeatures(List<ProductVariantsEntity> productVariants) {
		List<VariantFeatureDTO> features = new ArrayList<>();

		if(productVariants != null ) {
			features =
					productVariants
					.stream()
					.filter(this::hasFeatures)
					.map(this::extractVariantFeatures)
					.flatMap(List::stream)
					.distinct()
					.collect(toList());
		}

		return features;
	}




	private List<VariantFeatureDTO> extractVariantFeatures(ProductVariantsEntity variant){
		JacksonJsonParser parser = new JacksonJsonParser();
		Map<String, Object> keyValueMap =  parser.parseMap(variant.getFeatureSpec());
		return keyValueMap
				.keySet()
				.stream()
				.map(Integer::parseInt)
				.map(productFeaturesRepository::findById)
				.filter(optionalFeature -> optionalFeature != null && optionalFeature.isPresent())
				.map(Optional::get)
				.map(VariantFeatureDTO::new)
				.collect(Collectors.toList());
	}







	private boolean hasFeatures(ProductVariantsEntity variant) {
		return variant.getFeatureSpec() != null && !variant.getFeatureSpec().isEmpty();
	}

	private Set<ProductImgDTO> getProductImages(Long productId) {

		List<ProductImagesEntity> productImages = productImagesRepository.findByProductEntity_IdOrderByPriority(productId);

		if (productImages != null && !productImages.isEmpty()) {
			return productImages
					.stream()
					.map(ProductImgDTO::new)
					.collect(toSet());
		}
		return null;
	}



	private List<ProductImgDTO> getProductVariantImages(Long variantId) {
		List<ProductImagesEntity> variantImages = productImagesRepository.findByProductVariantsEntity_Id(variantId);

		List<ProductImgDTO> variantImagesArray = new ArrayList<>();
		if (variantImages != null && !variantImages.isEmpty()) {
			variantImagesArray =
					variantImages
					.stream()
					.filter(Objects::nonNull)
					.map(ProductImgDTO::new)
					.collect(toList());
		}

		return variantImagesArray;
	}




	private List<StockDTO> getStockList(ProductVariantsEntity variant,Long shopId)  {

		List<StocksEntity> stocks = stockService.getVariantStockForShop(variant, shopId);

		return	stocks
				.stream()
				.filter(stock -> stock != null)
				.map(StockDTO::new)
				.collect(toList());
	}


	public ProductsResponse getProducts(ProductSearchParam requestParams) throws BusinessException, InvocationTargetException, IllegalAccessException, SQLException {
		ProductSearchParam params = getProductSearchParams(requestParams);

		SQLQuery countStocks = getProductsQuery(params);
		Long productsCount = countStocks.fetchCount();

		SQLQuery stocks = getProductsQuery(params);

		stocks.select((Expressions.template(ProductRepresentationObject.class,"*")))
				 .limit(params.count).offset(params.start);

		List<ProductRepresentationObject> result = template.query(stocks.getResults().getStatement().toString(),
				new BeanPropertyRowMapper<>(ProductRepresentationObject.class));

		return getProductResponseFromStocks(result, productsCount);
	}





	private SQLQuery getProductsQuery(ProductSearchParam params) {
		QStocks stock = QStocks.stocks;
		QProducts product = QProducts.products;
		QProductVariants variant = QProductVariants.productVariants;
		QProductTags productTags = QProductTags.productTags;

		SQLQueryFactory query = new SQLQueryFactory( new Configuration(new PostgreSQLTemplates()), dataSource);

		SQLQuery productTagsQuery = getProductTagsQuery(query, productTags, params);

		BooleanBuilder predicate = getQueryPredicate(params, product, stock);

		OrderSpecifier order = getProductQueryOrder(params, product, stock);

		SQLQuery fromClause = getProductsBaseQuery(query, predicate);

		SQLQuery sqlQuery = fromClause.select(stock.id.as("stock_id"), stock.quantity.as("quantity"), stock.price.as("price"),
                stock.discount, stock.currency,
				product.organizationId.as("organization_id"), stock.shopId.as("shop_id"),
				variant.barcode.as("variant_barcode"), variant.featureSpec,
				product.id, product.barcode, product.brandId.as("brand_id"),
				product.categoryId.as("category_id"), product.description.as("description"), product.name.as("name"),
				product.createdAt.as("creation_date"), product.updatedAt.as("update_date"),
				SQLExpressions.rowNumber()
						.over()
						.partitionBy(product.id)
						.orderBy(stock.price).as("row_num"));

		if (productTagsQuery != null)
			sqlQuery.where(product.id.in((com.querydsl.core.types.Expression<? extends Long>) productTagsQuery));

		if (order != null)
			sqlQuery.orderBy(order);

		SQLQuery stocks = query.from(sqlQuery.as("total_products"))
				.where(Expressions.numberPath(Long.class, "row_num").eq(1L));

		return stocks;
	}

	public ProductsFiltersResponse getProductAvailableFilters(ProductSearchParam param) throws BusinessException, IllegalAccessException, InvocationTargetException, SQLException {
		ProductSearchParam finalParams = getProductSearchParams(param);

		SQLQueryFactory factory = new SQLQueryFactory(new Configuration(new PostgreSQLTemplates()), dataSource);

		QStocks stock = QStocks.stocks;
		QProducts product = QProducts.products;

		BooleanBuilder predicate = getQueryPredicate(finalParams, product, stock);

		SQLQuery baseQuery = getProductsBaseQuery(factory, predicate);

		Prices prices = getProductPrices(baseQuery,stock);

		List<Organization_BrandRepresentationObject> brands = getProductBrands(factory, baseQuery, product);

		List<com.nasnav.dto.response.navbox.VariantFeatureDTO.VariantFeatureDTOList> variantsFeatures =
				getProductVariantFeatures(factory, baseQuery, product);

		ProductsFiltersResponse response = new ProductsFiltersResponse(prices, brands, variantsFeatures);

		return response;
	}



	private Prices getProductPrices(SQLQuery baseQuery, QStocks stock) throws SQLException {
		SQLQuery query = baseQuery.select(stock.price.min().as("minPrice"), stock.price.max().as("maxPrice"));

		return template.queryForObject(query.getResults().getStatement().toString(), new BeanPropertyRowMapper<>(Prices.class));
	}



	private List<Organization_BrandRepresentationObject> getProductBrands(SQLQueryFactory factory, SQLQuery baseQuery, QProducts product) throws SQLException {
		QBrands brand = QBrands.brands;

		SQLQuery query = factory.select(brand.id, brand.name).from(brand)
								.where(brand.id.in(baseQuery.select(product.brandId)));

		return template.query(query.getResults().getStatement().toString(),
				new BeanPropertyRowMapper<>(Organization_BrandRepresentationObject.class));
	}


	private List<com.nasnav.dto.response.navbox.VariantFeatureDTO.VariantFeatureDTOList> getProductVariantFeatures(SQLQueryFactory factory,
																							 SQLQuery baseQuery, QProducts product) throws SQLException {
		QProductVariants variant = QProductVariants.productVariants;
		QProductFeatures feature = QProductFeatures.productFeatures;

		SQLQuery featuresVal =
				factory.select(Expressions.numberTemplate(Long.class, "(json_each(text_to_json(feature_spec))).key::int8").as("id"),
				Expressions.stringTemplate("(json_each(text_to_json(feature_spec))).value::varchar").as("feature_value"))
				.from(baseQuery.select(variant.featureSpec).as("product_variants"))
				.where(variant.featureSpec.isNotNull().and(variant.featureSpec.ne("'{}'")));

		SQLQuery query = factory.select(Expressions.numberTemplate(Long.class, "features_val.id"),
										Expressions.stringTemplate("name"),
										Expressions.stringTemplate("p_name"),
										Expressions.stringTemplate("features_val.feature_value")).distinct()
								.from(featuresVal.as("features_val")).leftJoin(feature)
								.on(feature.id.eq(Expressions.numberTemplate(Long.class, "features_val.id")));

		List<com.nasnav.dto.response.navbox.VariantFeatureDTO.VariantFeatureDTOList> variantsList =
						template.query(query.getResults().getStatement().toString(),
							new BeanPropertyRowMapper<>(com.nasnav.dto.response.navbox.VariantFeatureDTO.VariantFeatureDTOList.class));

		return variantsList;/*variantsList.stream()
						   .collect( groupingBy(com.nasnav.dto.response.navbox.VariantFeatureDTO.VariantFeatureDTOList::getId))
						   .entrySet()
						   .stream()
						   .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));	*/
	}



	private SQLQuery getProductsBaseQuery(SQLQueryFactory query, BooleanBuilder predicate) {
		QStocks stock = QStocks.stocks;
		QProducts product = QProducts.products;
		QProductVariants variant = QProductVariants.productVariants;
		return query.from(stock)
					.innerJoin(variant).on(stock.variantId.eq(variant.id))
					.innerJoin(product).on(variant.productId.eq(product.id))
					.where(predicate);
	}


	private OrderSpecifier getProductQueryOrder(ProductSearchParam params, QProducts product, QStocks stock) {
		if (params.getOrder().equals(SortOrder.DESC))
			switch (params.getSort()) {
				case ID : return product.id.desc();
				case NAME: return product.name.desc();
				case P_NAME: return product.pName.desc();
				case CREATION_DATE: return product.createdAt.desc();
				case UPDATE_DATE: return product.updatedAt.desc();
				case PRICE: return stock.price.desc();
			}
		else if (params.getOrder().equals(SortOrder.ASC))
			switch (params.getSort()) {
				case ID : return product.id.asc();
				case NAME: return product.name.asc();
				case P_NAME: return product.pName.asc();
				case CREATION_DATE: return product.createdAt.asc();
				case UPDATE_DATE: return product.updatedAt.asc();
				case PRICE: return stock.price.asc();
			}
		return null;
	}

	private BooleanBuilder getQueryPredicate(ProductSearchParam params, QProducts product, QStocks stock) {
		BooleanBuilder predicate = new BooleanBuilder();

		predicate.and(product.removed.eq(0));
		predicate.and(product.hide.eq(false));

		if(params.org_id != null)
			predicate.and( product.organizationId.eq((params.org_id) ));

		if(params.brand_id != null)
			predicate.and( product.brandId.eq(params.brand_id) );

		if(params.category_id != null)
			predicate.and( product.categoryId.eq(params.category_id) );

		if(params.minPrice != null)
			predicate.and( stock.price.goe(params.minPrice));

		if(params.maxPrice != null)
			predicate.and( stock.price.loe(params.maxPrice));

		if(params.name != null)
			predicate.and( product.name.likeIgnoreCase("%" + params.name + "%")
						   .or(product.description.likeIgnoreCase("%" + params.name + "%") ));

		if(params.shop_id != null && params.org_id == null)
			predicate.and( stock.shopId.eq(params.shop_id) );

		return predicate;
	}

	private SQLQuery<Long> getProductTagsQuery(SQLQueryFactory query, QProductTags productTags, ProductSearchParam params) {
		if (params.getTags() == null)
			return null;

		return query.select(Expressions.numberPath(Long.class, "id"))
					.from(SQLExpressions.select(productTags.productId.as("id"), productTags.tagId.count().as("count"))
										.from(productTags)
										.where(productTags.tagId.in(params.getTags()))
										.groupBy(productTags.productId)
										.having(productTags.tagId.count().eq((long) params.getTags().size())).as("productTags"));
	}


	ProductSearchParam getProductSearchParams(ProductSearchParam oldParams) throws BusinessException, InvocationTargetException, IllegalAccessException {
		ProductSearchParam params = new ProductSearchParam();
		BeanUtils.copyProperties(params, oldParams);

		if (oldParams.order != null)
			params.setOrder(oldParams.order.getValue());

		if (params.sort != null && ProductSortOptions.getProductSortOptions(params.sort.getValue()) == null)
			throw new BusinessException("Sort is limited to id, name, pname, price", null, HttpStatus.BAD_REQUEST);

		if (params.order != null && !params.order.getValue().equals("asc") && !params.order.getValue().equals("desc"))
			throw new BusinessException("Order is limited to asc and desc only", null, HttpStatus.BAD_REQUEST);

		if (params.start != null && params.start < 0)
			throw new BusinessException("Start can be zero or more", null, HttpStatus.BAD_REQUEST);

		if (params.count != null && params.count < 1)
			throw new BusinessException("Count can be One or more", null, HttpStatus.BAD_REQUEST);

		if (params.org_id == null && params.shop_id == null)
			throw new BusinessException("Shop Id or Organization Id shall be provided", null, HttpStatus.BAD_REQUEST);

		if (params.minPrice != null && params.minPrice.compareTo(BigDecimal.ZERO) < 0)
			params.minPrice = BigDecimal.ZERO;

		if (params.start == null)
			params.start = defaultStart;

		params.count = getProductCountParam(params.count);

		if (params.sort == null)
			params.setSort(defaultSortAttribute);

		if (params.order == null)
			params.setOrder(defaultOrder);

		return params;
	}

	Integer getProductCountParam(Integer count) {
		return count == null ? defaultCount : count < 1000 ? count : 1000;
	}

	private Order getProductQueryOrderBy(ProductSearchParam params, CriteriaBuilder builder, Join root) {
		if (!params.sort.getValue().equals("price")) {
			Path orderByAttr = root.get(params.sort.getValue());
			Order orderBy = builder.asc(orderByAttr);
			if (params.order.equals(SortOrder.DESC)) {
				orderBy = builder.desc(orderByAttr);
			}
			return orderBy;
		}
		return null;
	}


	private List<ProductEntity> getProductsByIds(List<Long> productsIds, String order, String sort) {

		List<ProductEntity> products = null;

		if (ProductSortOptions.getProductSortOptions(sort) == ProductSortOptions.ID) {
			if (order.equals("asc")) {
				products = productRepository.findByIdInOrderByIdAsc(productsIds);
			} else {
				products = productRepository.findByIdInOrderByIdDesc(productsIds);
			}
		} else if (ProductSortOptions.getProductSortOptions(sort) == ProductSortOptions.NAME) {
			if (order.equals("asc")) {
				products = productRepository.findByIdInOrderByNameAsc(productsIds);
			} else {
				products = productRepository.findByIdInOrderByNameDesc(productsIds);
			}
		} else if (ProductSortOptions.getProductSortOptions(sort) == ProductSortOptions.P_NAME) {
			if (order.equals("asc")) {
				products = productRepository.findByIdInOrderByPnameAsc(productsIds);
			} else {
				products = productRepository.findByIdInOrderByPnameDesc(productsIds);
			}
		} else {
			products = productRepository.findByIdIn(productsIds);
		}
		return products;
	}


	private ProductsResponse getProductsResponse(List<ProductEntity> products, String order, String sort, Long productsCount) {
		if(products == null)
			return new ProductsResponse();

		List<Long> productIdList = products.stream()
				.map(ProductEntity::getId)
				.collect(Collectors.toList());

		Map<Long, String> productCoverImages = imgService.getProductsCoverImages(productIdList);

		List<ProductRepresentationObject> productsRep =
				products.stream()
					  .map(prod -> getProductRepresentation(prod, productCoverImages))
					  .collect(Collectors.toList());

		if (ProductSortOptions.getProductSortOptions(sort) == ProductSortOptions.PRICE)
			sortByPrice(productsRep, order);

		return new ProductsResponse(productsCount, productsRep);

	}

	private ProductsResponse getProductResponseFromStocks(List<ProductRepresentationObject> stocks,
														  Long productsCount) {
		if(stocks == null || stocks.isEmpty())
			return new ProductsResponse();

		List<Long> productIdList = stocks.stream()
				.map(ProductRepresentationObject::getId)
				.collect(Collectors.toList());

		Map<Long, String> productCoverImages = imgService.getProductsCoverImages(productIdList);

		Map<Long, List<TagsRepresentationObject>> productsTags = getProductsTagsDTOList(productIdList);

		List<Long> productsVariantsCountFlag = getProductsVariantsMultipleFlag(productIdList);

		stocks.stream()
			.map(s -> setAdditionalInfo(s, productCoverImages))
			.map(s -> setProductTags(s, productsTags))
			.map(s -> setProductMultipleVariants(s, productsVariantsCountFlag))
			.collect(Collectors.toList());

		return new ProductsResponse(productsCount, stocks);

	}

	private ProductRepresentationObject setProductMultipleVariants(ProductRepresentationObject product,
																   List<Long> productsWithMultipleVariants) {
		if (productsWithMultipleVariants.contains(product.getId()))
			product.setMultipleVariants(true);

		return product;
	}
	private ProductRepresentationObject setProductTags(ProductRepresentationObject product,
													   Map<Long, List<TagsRepresentationObject>>tagsMap) {
		List<TagsRepresentationObject> tags = tagsMap.get(product.getId());
		product.setTags(tags);
		return product;
	}

	private void sortByPrice(List<ProductRepresentationObject> productsRep, String order) {
		if (order.equals("desc")) {
			Collections.sort(productsRep, comparing(ProductRepresentationObject::getPrice).reversed() );
		} else {
			Collections.sort(productsRep, comparing(ProductRepresentationObject::getPrice));
		}
	}




	private ProductRepresentationObject setAdditionalInfo(ProductRepresentationObject product,
														  Map<Long, String> productCoverImgs) {
		String imgUrl = productCoverImgs.get(product.getId());
		product.setImageUrl(imgUrl);
		if (imgUrl.equals(imgService.NO_IMG_FOUND_URL))
			product.setHidden(true);

		return product;
	}





	private Optional<StocksEntity> getDefaultProductStock(ProductEntity product) {
		return ofNullable(product)
				.map(ProductEntity::getProductVariants)
				.orElseGet(Collections::emptySet)
				.stream()
				.map(ProductVariantsEntity::getStocks)
				.filter(Objects::nonNull)
				.flatMap(Set::stream)
				.min( comparing(StocksEntity::getPrice));
	}





	public ProductUpdateResponse updateProduct(String productJson, Boolean isBundle) throws BusinessException {
		BaseUserEntity user =  securityService.getCurrentUser();

		ObjectMapper mapper = createObjectMapper();
		JsonNode rootNode;
		try {
			rootNode = mapper.readTree(productJson);
		} catch (IOException e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
			throw new BusinessException("Failed to deserialize JSON string ["+ productJson + "]", "INTERNAL SERVER ERROR", HttpStatus.INTERNAL_SERVER_ERROR);
		}

		validateProductDto(rootNode, user);

		ProductEntity entity = prepareProdcutEntity(rootNode, user,isBundle);
		ProductEntity saved = productRepository.save(entity);

		return new ProductUpdateResponse(true, saved.getId());
	}




	private ProductEntity prepareProdcutEntity(JsonNode productJsonNode, BaseUserEntity user, Boolean isBundle)
			throws BusinessException {

		Long id = productJsonNode.path("product_id").asLong();
		JsonNode operationNode = productJsonNode.path("operation");
		Operation operation = Operation.valueOf(operationNode.asText().toUpperCase());

		ProductEntity entity;

		if(Operation.CREATE.equals(operation)) {
			entity = new ProductEntity();
			if(isBundle)
				entity.setProductType(ProductTypes.BUNDLE);
		}
		else {
			entity = productRepository.findById(id)
					.orElseThrow(()-> new BusinessException("No prodcut exists with  ID: "+ id, "INVALID_PARAM:id" , HttpStatus.NOT_ACCEPTABLE));
		}

		updateProductEntityFromJson(entity, productJsonNode, user);

		return entity;
	}




	private void updateProductEntityFromJson(ProductEntity entity, JsonNode productJsonNode, BaseUserEntity user)
			throws BusinessException {
		ProductUpdateDTO productDto = new ProductUpdateDTO();
		try {

			BeanUtils.copyProperties(productDto, entity);

			//readerForUpdating makes the reader update the properties that ONLY exists in JSON string
			//That's why we are parsing the JSON instead of spring (-_-)
			ObjectMapper mapper = createObjectMapper();
			productDto = mapper.readerForUpdating(productDto).readValue(productJsonNode.toString());

			productDto.setOrganizationId(user.getOrganizationId());

			if(StringUtils.isBlankOrNull(productDto.getPname())) {
				productDto.setPname(StringUtils.encodeUrl( productDto.getName() ));
			}

			BeanUtils.copyProperties(entity, productDto);
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
			throw new BusinessException(e.getMessage(), "INTERNAL SERVER ERROR", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}




	private void validateProductDto(JsonNode productJsonNode, BaseUserEntity user) throws BusinessException {
		JsonNode operationNode = productJsonNode.path("operation");

		if(operationNode.isMissingNode()) {
			throw new BusinessException("No Operation provided! parameter operation should have values in[\"create\",\"update\"]!", "INVALID_PARAM:operation" , HttpStatus.NOT_ACCEPTABLE);
		}

		String operationStr = operationNode.asText().toUpperCase();
		Operation operation = Operation.valueOf(operationStr);

		if(operation.equals(Operation.UPDATE)) {
			validateProductDtoToUpdate(productJsonNode, user);
		}else {
			validateProductDtoToCreate(productJsonNode, user);
		}

	}




	private ObjectMapper createObjectMapper() {
		ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		return mapper;
	}




	private void validateProductDtoToCreate(JsonNode productJson, BaseUserEntity user ) throws BusinessException {

		checkCreateProuctReqParams(productJson);

		JsonNode brandId = productJson.path("brand_id");
		validateBrandId(user, brandId);
	}




	private void validateProductDtoToUpdate(JsonNode productJson, BaseUserEntity user)
			throws BusinessException {
		JsonNode id = productJson.path("product_id");
		JsonNode brandId = productJson.path("brand_id");

		if(id.isMissingNode())
			throw new BusinessException("No product id provided!", "INVALID_PARAM:product_id" , HttpStatus.NOT_ACCEPTABLE);

		if(!id.isNull() && !productRepository.existsById(id.asLong()))
			throw new BusinessException("No prodcut exists with ID: "+ id + " !", "INVALID_PARAM:product_id" , HttpStatus.NOT_ACCEPTABLE);

		if(!brandId.isMissingNode() )
			validateBrandId(user, brandId);
	}




	private void checkCreateProuctReqParams(JsonNode productJson)
			throws BusinessException {
		JsonNode name = productJson.path("name");
		JsonNode brandId = productJson.path("brand_id");

		if(name.isMissingNode())
			throw new BusinessException("Product name Must be provided! ", "MISSING_PARAM:name" , HttpStatus.NOT_ACCEPTABLE);

		if( name.isNull() )
			throw new BusinessException("Product name cannot be Null ", "MISSING_PARAM:name" , HttpStatus.NOT_ACCEPTABLE);

		if(brandId.isMissingNode())
			throw new BusinessException("Brand Id Must be provided!" , "MISSING_PARAM:brand_Id" , HttpStatus.NOT_ACCEPTABLE);
	}






	private void validateBrandId(BaseUserEntity user, JsonNode brandId) throws BusinessException {
		if(brandId.isMissingNode() || brandId.isNull()) //brand_id is optional and can be null
			return;

		long id = brandId.asLong();
		if(!brandRepo.existsById(id) )
			throw new BusinessException("No Brand exists with ID: " + id + " !" , "INVALID_PARAM:brand_id" , HttpStatus.NOT_ACCEPTABLE);

		BrandsEntity brand = brandRepo.findById(id)
				.orElseThrow(() -> new BusinessException("No Brand exists with ID: " + id + " !", "INVALID_PARAM:brand_Id" , HttpStatus.NOT_ACCEPTABLE));

		Long brandOrgId = brand.getOrganizationEntity().getId();
		if( !brandOrgId.equals( user.getOrganizationId() )) {
			String msg = String.format("Brand with id [%d] doesnot belong to organization with id [%d]", id, user.getOrganizationId());
			throw new BusinessException(msg , "INVALID_PARAM:brand_Id" , HttpStatus.NOT_ACCEPTABLE);
		}
	}



	public ProductsDeleteResponse deleteProduct(List<Long> productIds) throws BusinessException {

		for(Long productId : productIds) {
			if(!productRepository.existsById(productId)) {
				return new ProductsDeleteResponse(true, Collections.singletonList(productId)); //if the product doesn't exists, then..mission accomplished!
			}

			validateProductToDelete(productId);

			try {
				transactions.deleteProduct(productId);
			} catch (DataIntegrityViolationException e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
				throw new BusinessException(format(ERR_PRODUCT_STILL_USED, productId), "INVAILID PARAM:product_id", HttpStatus.NOT_ACCEPTABLE);
			} catch (Throwable e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
				throw new BusinessException(format(ERR_PRODUCT_DELETE_FAILED, productId), "INVAILID PARAM:product_id", HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
		return new ProductsDeleteResponse(true, productIds);
	}




	private void validateProductToDelete(Long productId) throws BusinessException {
		validateUserCanDeleteProduct(productId);

		validateProductIsNotInBundle(productId);

		validateProductNotUsedInNewOrders(productId);
	}




	private void validateProductNotUsedInNewOrders(Long productId) throws BusinessException {
		Long count = basketRepo.countByProductIdAndOrderEntity_status(productId, 0);
		if(count > 0) {
			throw new BusinessException(
							format(ERR_CANNOT_DELETE_PRODUCT_USED_IN_NEW_ORDERS, productId)
							, "INVALID_PARAM:product_id"
							, HttpStatus.NOT_ACCEPTABLE);
		}
	}




	private void validateUserCanDeleteProduct(Long productId) throws BusinessException {
		Long userOrgId = securityService.getCurrentUserOrganizationId();

		productRepository.findById(productId)
				.filter(p -> p.getOrganizationId().equals(userOrgId) )
				.orElseThrow(() -> getUserCannotDeleteProductException(productId, userOrgId));
	}




	private BusinessException getUserCannotDeleteProductException(Long productId, Long userOrgId) {
		return new BusinessException(
				format(ERR_CANNOT_DELETE_PRODUCT_BY_OTHER_ORG_USER, productId, userOrgId)
				, "INSUFFICIENT_RIGHTS"
				, HttpStatus.FORBIDDEN);
	}




	private void validateProductIsNotInBundle(Long productId) throws BusinessException {
		List<BundleEntity> bundles = bundleRepository.getBundlesHavingItemsWithProductId(productId);
		if(bundles.size() != 0) {
			String bundleIds = bundles.stream()
								.map(BundleEntity::getId)
								.map(String::valueOf)
								.collect(Collectors.joining(","));

			throw new BusinessException(
					String.format(ERR_CANNOT_DELETE_BUNDLE_ITEM, productId, bundleIds)
					, "INVALID PARAM:product_id"
					, HttpStatus.NOT_ACCEPTABLE);
		}
	}



	public ProductsDeleteResponse deleteBundle(Long bundleId) throws BusinessException {
		validateBundleToDelete(bundleId);

		return deleteProduct(Collections.singletonList(bundleId));
	}



	private void validateBundleToDelete(Long productId) throws BusinessException {
		if(!bundleRepository.existsById(productId)
				&& productRepository.existsById(productId)) {

			throw new BusinessException(
					String.format("Can only delete bundles using this API, product with id[%d] is not a bundle!", productId)
					, "INVALID PARAM:product_id"
					, HttpStatus.NOT_ACCEPTABLE);

		}

	}




	public ProductImageUpdateResponse updateProductImage(MultipartFile file, ProductImageUpdateDTO imgMetaData) throws BusinessException {
		validateProductImg(file, imgMetaData);

		ProductImageUpdateResponse response = saveProductImg(file, imgMetaData);
		return response;
	}



	/**
	 * assert all required parameters are validated before calling this, so we don't validate twice and
	 * validation should be centralized !
	 * if something fails here due to invalid data , then the validation should be fixed!
	 * */
	private ProductImageUpdateResponse saveProductImg(MultipartFile file, ProductImageUpdateDTO imgMetaData) throws BusinessException {
		Operation opr = imgMetaData.getOperation();

		if(opr.equals(Operation.CREATE))
			return saveNewProductImg(file, imgMetaData);
		else
			return saveUpdatedProductImg(file, imgMetaData);
	}




	private ProductImageUpdateResponse saveUpdatedProductImg(MultipartFile file, ProductImageUpdateDTO imgMetaData) throws BusinessException {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		BaseUserEntity user =  empRepo.getOneByEmail(auth.getName());


		Long imgId = imgMetaData.getImageId();
		ProductImagesEntity entity = productImagesRepository.findById(imgId).get();



		String url = null;
		String oldUrl = null;
		if(file != null && !file.isEmpty()) {
			url = fileService.saveFile(file, user.getOrganizationId());
			oldUrl = entity.getUri();
		}


		//to update a value , it should be already present in the JSON
		if(imgMetaData.isUpdated("priority"))
			entity.setPriority( imgMetaData.getPriority() );

		if(imgMetaData.isUpdated("type"))
			entity.setType( imgMetaData.getType() );

		if(imgMetaData.isUpdated("productId")) {
			Long productId = imgMetaData.getProductId();
			Optional<ProductEntity> productEntity = productRepository.findById( productId );
			entity.setProductEntity(productEntity.get());
		}

		if(url != null)
			entity.setUri(url);

		if(imgMetaData.isUpdated("variantId")) {
			Optional.ofNullable( imgMetaData.getVariantId() )
					.flatMap(productVariantsRepository::findById)
					.ifPresent(entity::setProductVariantsEntity);
		}

		entity = productImagesRepository.save(entity);

		if(url != null && oldUrl != null) {
			fileService.deleteFileByUrl(oldUrl);
		}

		return new ProductImageUpdateResponse(entity.getId(), url);
	}




	private ProductImageUpdateResponse saveNewProductImg(MultipartFile file, ProductImageUpdateDTO imgMetaData)
			throws BusinessException {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		BaseUserEntity user =  empRepo.getOneByEmail(auth.getName());

		String url = fileService.saveFile(file, user.getOrganizationId());

		Long imgId = saveProductImgToDB(imgMetaData, url);

		return new ProductImageUpdateResponse(imgId, url);
	}




	private Long saveProductImgToDB(ProductImageUpdateDTO imgMetaData, String uri) throws BusinessException {
		Long productId = imgMetaData.getProductId();
		Optional<ProductEntity> productEntity = productRepository.findById( productId );


		ProductImagesEntity entity = new ProductImagesEntity();
		entity.setPriority(imgMetaData.getPriority());
		entity.setProductEntity(productEntity.get());
		entity.setType(imgMetaData.getType());
		entity.setUri(uri);
		Optional.ofNullable( imgMetaData.getVariantId() )
				.flatMap(productVariantsRepository::findById)
				.ifPresent(entity::setProductVariantsEntity);

		entity = productImagesRepository.save(entity);

		return entity.getId();
	}




	private void validateProductImg(MultipartFile file, ProductImageUpdateDTO imgMetaData) throws BusinessException {
		if(imgMetaData == null)
			throw new BusinessException("No Metadata provided for product image!", "INVALID PARAM", HttpStatus.NOT_ACCEPTABLE);

		if(!imgMetaData.isRequiredPropertyProvided("operation"))
			throw new BusinessException("No operation provided!", "INVALID PARAM:operation", HttpStatus.NOT_ACCEPTABLE);


		if(imgMetaData.getOperation().equals( Operation.CREATE )) {
			validateNewProductImg(file, imgMetaData);
		}else {
			validateUpdatedProductImg(file, imgMetaData);
		}

	}



	private void validateUpdatedProductImg(MultipartFile file, ProductImageUpdateDTO imgMetaData) throws BusinessException {
		if(!imgMetaData.areRequiredForUpdatePropertiesProvided()) {
			throw new BusinessException(
					String.format("Missing required parameters! required parameters for updating existing image are: %s", imgMetaData.getRequiredPropertyNamesForDataUpdate())
					, "MISSING PARAM"
					, HttpStatus.NOT_ACCEPTABLE);
		}

		validateProductOfImg(imgMetaData);

		validateImgId(imgMetaData);

		if(file != null)
			validateProductImgFile(file);

	}




	private void validateImgId(ProductImageUpdateDTO imgMetaData) throws BusinessException {
		//based on previous validations assert imageId is provided
		Long imgId = imgMetaData.getImageId();

		if( !productImagesRepository.existsById(imgId))
			throw new BusinessException(
					String.format("No product image exists with id: %d !", imgId)
					, "INVALID PARAM:image_id"
					, HttpStatus.NOT_ACCEPTABLE);
	}




	private void validateNewProductImg(MultipartFile file, ProductImageUpdateDTO imgMetaData) throws BusinessException {
		if(!imgMetaData.areRequiredForCreatePropertiesProvided()) {
			throw new BusinessException(
					String.format("Missing required parameters! required parameters for adding new image are: %s", imgMetaData.getRequiredPropertiesForDataCreate())
					, "MISSING PARAM"
					, HttpStatus.NOT_ACCEPTABLE);
		}

		validateProductOfImg(imgMetaData);

		validateProductImgFile(file);
	}




	private void validateProductOfImg(ProductImageUpdateDTO imgMetaData) throws BusinessException {

		Long productId = imgMetaData.getProductId();
		Optional<ProductEntity> product = productRepository.findById(productId);
		if( !product.isPresent() )
			throw new BusinessException(
					String.format("Product Id :[%d] doesnot exists!", productId)
					, "INVALID PARAM:product_id"
					, HttpStatus.NOT_ACCEPTABLE);


		validateUserCanModifyProduct(product);

		validateProductVariantForImg(imgMetaData, productId);

	}




	private void validateProductVariantForImg(ProductImageUpdateDTO imgMetaData, Long productId) throws BusinessException {
		Long variantId = imgMetaData.getVariantId();
		if(variantId != null ) {
			Optional<ProductVariantsEntity> variant = productVariantsRepository.findById(variantId);
			if(variantId != null && !variant.isPresent())
				throw new BusinessException(
						String.format("Product variant with id [%d] doesnot exists!", variantId)
						, "INVALID PARAM:variant_id"
						, HttpStatus.NOT_ACCEPTABLE);


			if(variantNotForProduct(variant, productId))
				throw new BusinessException(
						String.format("Product variant with id [%d] doesnot belong to product with id [%d]!", variantId, productId)
						, "INVALID PARAM:variant_id"
						, HttpStatus.NOT_ACCEPTABLE);
		}
	}



	/**
	 * product must follow the organization of the user
	 * */
	private void validateUserCanModifyProduct(Optional<ProductEntity> product) throws BusinessException {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		BaseUserEntity user =  empRepo.getOneByEmail(auth.getName());
		Long userOrg = user.getOrganizationId();
		Long productOrg = product.get().getOrganizationId();

		if(!Objects.equals(userOrg, productOrg))
			throw new BusinessException(
					String.format("User with email [%s] have no rights to modify products from organization of id[%d]!", user.getEmail(), productOrg)
					, "INSUFFICIENT RIGHTS"
					, HttpStatus.FORBIDDEN);
	}





	private Boolean variantNotForProduct(Optional<ProductVariantsEntity> variant, Long productId) {
		Boolean variantNotForProduct = variant.get().getProductEntity() == null
				|| !variant.get().getProductEntity().getId().equals(productId);
		return variantNotForProduct;
	}





	private void validateProductImgFile(MultipartFile file) throws BusinessException {
		if(file == null || file.isEmpty() || file.getContentType() == null)
			throw new BusinessException(
					"No image file provided!"
					, "MISSIG PARAM:image"
					, HttpStatus.NOT_ACCEPTABLE);

		String mimeType = file.getContentType();
		if(!mimeType.startsWith("image"))
			throw new BusinessException(
					String.format("Invalid file type[%]! only MIME 'image' types are accepted!", mimeType)
					, "MISSIG PARAM:image"
					, HttpStatus.NOT_ACCEPTABLE);
	}





	public ProductImageDeleteResponse deleteImage(Long imgId) throws BusinessException {
		ProductImagesEntity img =
				productImagesRepository.findById(imgId)
						.orElseThrow(()-> new BusinessException("No Image exists with id ["+ imgId+"] !", "INVALID PARAM:image_id", HttpStatus.NOT_ACCEPTABLE));

		Long productId = Optional.ofNullable(img.getProductEntity())
				.map(prod -> prod.getId())
				.orElse(null);

		validateImgToDelete(img);

		productImagesRepository.deleteById(imgId);

		fileService.deleteFileByUrl(img.getUri());

		return new ProductImageDeleteResponse(productId);
	}




	private void validateImgToDelete(ProductImagesEntity img) throws BusinessException {
		Long orgId = Optional.ofNullable(img.getProductEntity())
				.map(prod -> prod.getOrganizationId())
				.orElse(null);

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		BaseUserEntity user =  empRepo.getOneByEmail(auth.getName());

		if(!user.getOrganizationId().equals(orgId)) {
			throw new BusinessException(
					String.format("User from organization of id[%d] have no rights to delete product image of id[%d]",orgId, img.getId())
					, "UNAUTHRORIZED"
					, HttpStatus.FORBIDDEN);
		}
	}




	public BundleResponse getBundles(BundleSearchParam params) throws BusinessException {
		//validate params
		if(params.getBundle_id() == null && params.getOrg_id() == null)
			throw new BusinessException("Missing request parameters! Either bundle_Id or org_id must be provided!"
					, "MISSING PARAM:bundle_id,org_id"
					, HttpStatus.NOT_ACCEPTABLE);


		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<BundleEntity> query = builder.createQuery(BundleEntity.class);
		Root<BundleEntity> root = query.from(BundleEntity.class);

		Predicate[] predicatesArr = getBundleQueryPredicates(params, builder, root);
		Order orderBy = getBundleQueryOrderBy(params, builder, root);

		query.where(predicatesArr);
		query.orderBy(orderBy);

		List<BundleDTO> bundleDTOList = em.createQuery(query)
				.setMaxResults(params.getCount())
				.setFirstResult(params.getStart())
				.getResultList()
				.stream()
				.map(this::toBundleDTO)
				.collect(Collectors.toList());

		Long count = getQueryCount(builder, predicatesArr);

		return new BundleResponse( count,  bundleDTOList);
	}




	private Long getQueryCount(CriteriaBuilder builder, Predicate[] predicatesArr) {
		CriteriaQuery<Long> countQuery = builder.createQuery(Long.class);
		countQuery.select(  builder.count( countQuery.from(BundleEntity.class) ) )
				.where(predicatesArr);
		Long count = em.createQuery(countQuery).getSingleResult();
		return count;
	}




	private Predicate[] getBundleQueryPredicates(BundleSearchParam params, CriteriaBuilder builder,
	                                             Root<BundleEntity> root) {
		List<Predicate> predicates = new ArrayList<>();

		if(params.getBundle_id() != null)
			predicates.add( builder.equal(root.get("id"), params.getBundle_id()) );
		else
			predicates.add( builder.equal(root.get("organizationId"), params.getOrg_id()) );


		Predicate[] predicatesArr = predicates.stream().toArray( Predicate[]::new) ;
		return predicatesArr;
	}




	private Order getBundleQueryOrderBy(BundleSearchParam params, CriteriaBuilder builder, Root<BundleEntity> root) {
//		CriteriaBuilder builder = em.getCriteriaBuilder();
		@SuppressWarnings("rawtypes")
		Path orderByAttr = root.get(params.getSort().getValue());
		Order orderBy = builder.asc(orderByAttr);
		if(params.getOrder().equals(SortOrder.DESC))
			orderBy = builder.desc(orderByAttr);

		return orderBy;
	}





	private BundleDTO toBundleDTO(BundleEntity entity) {
		BundleDTO dto = new BundleDTO();

		dto.setId(entity.getId());
		dto.setImageUrl( imgService.getProductCoverImage( entity.getId() ));
		dto.setName(entity.getName());
		dto.setPname(entity.getPname());

		List<StocksEntity> bundleStock = 	entity.getProductVariants()
				.stream()
				.flatMap( var -> var.getStocks().stream())
				.collect(Collectors.toList());

		if(bundleStock.size() != 1) {
			throw new IllegalStateException(
					String.format("Bundle with id[%d] doesn't have a single price!", entity.getId()));
		}

		bundleStock.stream()
				.findFirst()
				.map(StocksEntity::getPrice)
				.ifPresent(dto::setPrice);

		List<Long> productIdList = bundleRepository.getBundleItemsProductIds(entity.getId());

		Map<Long, String> 	productCoverImages = imgService.getProductsCoverImages(productIdList);
		List<ProductBaseInfo> productlist = productRepository.findByIdInOrderByNameAsc(productIdList)
															.stream()
															.map(prod -> getProductRepresentation(prod, productCoverImages))
															.map(this::toProductBaseInfo)
															.collect(Collectors.toList());
		dto.setProducts( productlist );

		return dto;
	}



	private ProductBaseInfo toProductBaseInfo(ProductRepresentationObject source) {
		ProductBaseInfo baseInfo = new ProductBaseInfo();
		try {
			BeanUtils.copyProperties(baseInfo, source);
		} catch (IllegalAccessException | InvocationTargetException e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
			throw new RuntimeException(
					String.format( "Failed to copy data from class of type [%s] to a class of type [%s]"
							, source.getClass().getName()
							, baseInfo.getClass().getName() )
			);
		}

		return baseInfo;
	}




	public void updateBundleElement(BundleElementUpdateDTO element) throws BusinessException {
		validateBundleElementUpdateReq(element);

		if(element.getOperation().equals( Operation.DELETE)) {
			deleteBundleElement(element);
		}else if(element.getOperation().equals( Operation.ADD)){
			addBundleElement(element);
		}


	}




	private void deleteBundleElement(BundleElementUpdateDTO element) {
		BundleEntity bundle = bundleRepository.getOne(element.getBundleId());

		StocksEntity item = stockRepository.getOne(element.getStockId());

		bundle.getItems().remove(item);
		bundleRepository.save(bundle);
	}




	private void addBundleElement(BundleElementUpdateDTO element) {
		BundleEntity bundle = bundleRepository.getOne(element.getBundleId());

		StocksEntity item = stockRepository.getOne(element.getStockId());

		bundle.getItems().add(item);
		bundleRepository.save(bundle);
	}




	private void validateBundleElementUpdateReq(BundleElementUpdateDTO element) throws BusinessException {
		String missingParam = null;
		if(element.getOperation() == null) {
			missingParam = "operation";
		}else if(element.getBundleId() == null) {
			missingParam = "bundle_id";
		}else if(element.getStockId() == null) {
			missingParam = "stock_id";
		}

		if(missingParam != null) {
			throw new BusinessException(
					"Required parameters missing!"
					, "MISSING PARAM:" + missingParam
					, HttpStatus.NOT_ACCEPTABLE);
		}


		Operation opr = element.getOperation();
		if( !( opr.equals(Operation.ADD)
				||opr.equals(Operation.DELETE) ) ) {
			throw new BusinessException(
					String.format("Invalid Operation  [%s]", opr.getValue())
					, "INVALID PARAM:operation"
					, HttpStatus.NOT_ACCEPTABLE);
		}


		if(!bundleRepository.existsById(element.getBundleId())) {
			throw new BusinessException(
					String.format("No bundle exists with id[%d]", element.getBundleId())
					, "INVALID PARAM:bundle_id"
					, HttpStatus.NOT_ACCEPTABLE);
		}


		if(opr.equals(Operation.ADD) && !stockRepository.existsById(element.getStockId())) {
			throw new BusinessException(
					String.format("No stock item exists with id[%d]", element.getStockId())
					, "INVALID PARAM:stock_id"
					, HttpStatus.NOT_ACCEPTABLE);
		}

		BundleEntity bundle = bundleRepository.findById(element.getBundleId()).get();
		StocksEntity item = stockRepository.findById( element.getStockId() ).get();

		validateUserOrganization(bundle, item);
	}




	private void validateUserOrganization(BundleEntity bundle, StocksEntity item) throws BusinessException {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		BaseUserEntity user =  empRepo.getOneByEmail(auth.getName());

		Long userOrgId = user.getOrganizationId();
		Long bundleOrgId = bundle.getOrganizationId();
		Long itemOrgId = item.getOrganizationEntity().getId();

		boolean areEqual = EntityUtils.areEqual(userOrgId, bundleOrgId, itemOrgId);

		if(!areEqual) {
			throw new BusinessException(
					String.format("User who belongs to organization of id[%d] is not allowed "
							+ "to add stock item from organiztion of id[%d] to "
							+ "a bundle from organiztion of id[%d]", userOrgId, itemOrgId, bundleOrgId)
					, "INVALID PARAM:bundle_id/stock_id"
					, HttpStatus.FORBIDDEN);
		}
	}


	public VariantUpdateResponse updateVariant(VariantUpdateDTO variant) throws BusinessException {
		BaseUserEntity user =  securityService.getCurrentUser();
		Long orgId = user.getOrganizationId();

		validateVariant(variant, orgId);

		ProductVariantsEntity entity = saveVariantToDb(variant);
		return new VariantUpdateResponse(entity.getId());
	}




	private void validateVariant(VariantUpdateDTO variant, Long orgId) throws BusinessException {
		if(!variant.areRequiredAlwaysPropertiesPresent()) {
			throw new BusinessException(
					"Missing required parameters !"
					, "MISSING PARAM"
					, HttpStatus.NOT_ACCEPTABLE);
		}

		if( !productRepository.existsById( variant.getProductId() )) {
			throw new BusinessException(
					String.format("Invalid parameters [product_id], no product exists with id[%d]!", variant.getProductId())
					, "INVALID PARAM:features"
					, HttpStatus.NOT_ACCEPTABLE);
		}


		Operation opr = variant.getOperation();
		validateOperation(opr);

		if( opr.equals(Operation.CREATE) ) {
			validateVariantForCreate(variant, orgId);
		}else if( opr.equals(Operation.UPDATE) ) {
			validateVariantForUpdate(variant, orgId);
		}


	}




	private void validateVariantForUpdate(VariantUpdateDTO variant, Long userOrgId) throws BusinessException {
		if(!variant.areRequiredForUpdatePropertiesProvided()) {
			throw new BusinessException(
					"Missing required parameters !"
					, "MISSING PARAM"
					, HttpStatus.NOT_ACCEPTABLE);
		}

		validateUserCanUpdateVariant(variant, userOrgId);

		validateFeatures(variant, userOrgId);
	}




	private void validateUserCanUpdateVariant(VariantUpdateDTO variant, Long userOrgId) throws BusinessException {
		Long id = variant.getVariantId();
		Optional<ProductVariantsEntity> variantOptional= productVariantsRepository.findById( id );

		if( !variantOptional.isPresent()) {
			throw new BusinessException(
					String.format("Invalid parameters [variant_id], no product variant exists with id [%d]!", id)
					, "INVALID PARAM:variant_id"
					, HttpStatus.NOT_ACCEPTABLE);
		}

		Long variantOrgId = variantOptional.map(ProductVariantsEntity::getProductEntity)
				.map(ProductEntity::getOrganizationId)
				.orElseThrow(
						() -> new BusinessException(
								String.format("Product variant of id[%d], Doesn't follow any organization!", id)
								, "INTERNAL SERVER ERROR"
								, HttpStatus.INTERNAL_SERVER_ERROR)
				);

		if(!java.util.Objects.equals(variantOrgId, userOrgId)) {
			throw new BusinessException(
					String.format("Product variant of id[%d], can't be changed a user from organization with id[%d]!", id , userOrgId)
					, "INVALID PARAM:variant_id"
					, HttpStatus.FORBIDDEN);
		}
	}




	private void validateVariantForCreate(VariantUpdateDTO variant, Long userOrgId) throws BusinessException {
		if(!variant.areRequiredForCreatePropertiesProvided()) {
			throw new BusinessException(
					"Missing required parameters !"
					, "MISSING PARAM"
					, HttpStatus.NOT_ACCEPTABLE);
		}

		validateFeatures(variant, userOrgId);

	}




	private void validateFeatures(VariantUpdateDTO variant, Long userOrgId) throws BusinessException {
		String features = variant.getFeatures();
		if(variant.isUpdated("features") && StringUtils.isBlankOrNull( features )) {
			throw new BusinessException(
					"Invalid parameters [features], the product variant features can't be null nor Empty!"
					, "INVALID PARAM:features"
					, HttpStatus.NOT_ACCEPTABLE);
		}

		if(!isJSONValid( features )) {
			throw new BusinessException(
					String.format("Invalid parameters [features], the product variant features should be a valid json string! The given value was [%s]" ,features )
					, "INVALID PARAM:features"
					, HttpStatus.NOT_ACCEPTABLE);
		}

		if(hasInvalidFeatureKeys(features ,userOrgId)) {
			throw new BusinessException(
					String.format("Invalid parameter [features], a feature key doesnot exists or doesn't belong to organization with id[%d]" ,userOrgId )
					, "INVALID PARAM:features"
					, HttpStatus.NOT_ACCEPTABLE);
		}
	}




	private boolean hasInvalidFeatureKeys(String features, Long userOrgId) {
		JSONObject featuresJson = new JSONObject(features);
		return featuresJson.keySet()
				.stream()
				.map(Integer::valueOf)
				.map(productFeaturesRepository::findById)
				.anyMatch(opt -> isInvalidFeatureKey(userOrgId, opt));
	}




	private boolean isInvalidFeatureKey(Long userOrgId, Optional<ProductFeaturesEntity> opt) {
		return !opt.isPresent()
				|| opt.get().getOrganization() == null
				|| !Objects.equals(opt.get().getOrganization().getId(), userOrgId);
	}




	private ProductVariantsEntity saveVariantToDb(VariantUpdateDTO variant) {
		ProductVariantsEntity entity = new ProductVariantsEntity();

		Operation opr = variant.getOperation();

		if( opr.equals( UPDATE)) {
			entity = productVariantsRepository.findById( variant.getVariantId()).get();
		}


		if(variant.isUpdated("productId")){
			ProductEntity product = productRepository.findById( variant.getProductId() ).get();
			entity.setProductEntity(product);
		}

		if(variant.isUpdated("name")){
			entity.setName( variant.getName());
		}


		if(variant.isUpdated("description")) {
			entity.setDescription( variant.getDescription() );
		}

		if(variant.isUpdated("barcode")) {
			entity.setBarcode( variant.getBarcode() );
		}

		if(variant.isUpdated("features")) {
			entity.setFeatureSpec( variant.getFeatures() );
		}

		String pname = getPname(variant, opr);
		if(!isBlankOrNull(pname)) {
			entity.setPname(pname);
		}

		if(!isBlankOrNull(variant.getExtraAttr())) {
			saveExtraAttributesIntoEntity(variant, entity);
		}

		entity = productVariantsRepository.save(entity);

		return entity;
	}




	private void saveExtraAttributesIntoEntity(VariantUpdateDTO variant, ProductVariantsEntity entity) {
		try {
			JSONObject  extraAttrJson = new JSONObject(variant.getExtraAttr());

			extraAttrJson
			.keySet()
			.stream()
			.map(attrName -> createVariantExtraAttribute(attrName, extraAttrJson.getString(attrName)))
			.forEach(entity::addExtraAttribute);
		}catch(Throwable t) {
			throw new RuntimeBusinessException(
					ERR_INVALID_EXTRA_ATTR_STRING
					, "INVLAID: extra_attr"
					, NOT_ACCEPTABLE);
		}
	}




	private ProductExtraAttributesEntity createVariantExtraAttribute(String name, String value) {
		ExtraAttributesEntity extraAttrEntity = getExtraAttributeOrCreateIt(name);

		ProductExtraAttributesEntity variantExtraAttr = new ProductExtraAttributesEntity();
		variantExtraAttr.setExtraAttribute(extraAttrEntity);
		variantExtraAttr.setValue(value);

		return variantExtraAttr;
	}



	private ExtraAttributesEntity getExtraAttributeOrCreateIt(String name) {
		Long orgId = securityService.getCurrentUserOrganizationId();
		return extraAttrRepo
				.findByNameAndOrganizationId(name, orgId)
				.orElseGet(() -> createNewExtraAttribute(name));
	}




	private ExtraAttributesEntity createNewExtraAttribute(String name) {
		Long orgId = securityService.getCurrentUserOrganizationId();
		ExtraAttributesEntity newAttr = new ExtraAttributesEntity();
		newAttr.setName(name);
		newAttr.setOrganizationId(orgId);

		return extraAttrRepo.save(newAttr);
	}




	private String getPname(VariantUpdateDTO variantDto, Operation opr) {
		if(opr.equals( CREATE )){
			return getDefaultVariantPName(variantDto);
		}else if(variantDto.isUpdated("pname") && !StringUtils.isBlankOrNull( variantDto.getPname()) ) {
			return variantDto.getPname();
		}else {
			return "";
		}
	}




	private String getDefaultVariantPName(VariantUpdateDTO variant) {
		JSONObject json = new JSONObject(variant.getFeatures());

		StringBuilder pname = new StringBuilder();
		for(String key: json.keySet()) {
			String featureName = getProductFeatureName(key);
			String value = json.get(key).toString();

			if(pname.length() != 0)
				pname.append("-");

			String toAppend = featureName + "-"+value;
			pname.append(encodeUrl(toAppend));
		}

		String pnameStr = pname.length() == 0? variant.getName() : pname.toString();
		return encodeUrl(pnameStr);
	}




	private String getProductFeatureName(String idAsStr) {
		return ofNullable(idAsStr)
				.map(Integer::valueOf)
				.map(productFeaturesRepository::findById)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.map(ProductFeaturesEntity::getName)
				.orElse("");
	}



	private void validateOperation(Operation opr) throws BusinessException {
		if(opr == null) {
			throw new BusinessException(
					"Missing required parameters [operation]!"
					, "MISSING PARAM:operation"
					, HttpStatus.NOT_ACCEPTABLE);
		}

		if(!opr.equals(Operation.CREATE) &&
				!opr.equals(Operation.UPDATE)) {
			throw new BusinessException(
					String.format("Invalid parameters [operation], unsupported operation [%s]!", opr.getValue())
					, "INVALID PARAM:operation"
					, HttpStatus.NOT_ACCEPTABLE);
		}
	}





	private boolean isJSONValid(String test) {
		try {
			new JSONObject(test);
		} catch (JSONException ex) {
			try {
				new JSONArray(test);
			} catch (JSONException ex1) {
				return false;
			}
		}
		return true;
	}

	private ProductRepresentationObject getProductRepresentation(ProductEntity product) {
		ProductRepresentationObject productRep = new ProductRepresentationObject();
		setProductProperties(productRep, product);
		productRep.setMultipleVariants( product.getProductVariants().size() > 1);

		List<TagsRepresentationObject> productTags = getProductTagsDTOList(product.getId());

		productRep.setTags(productTags);

		Optional<StocksEntity> defaultStockOpt = getDefaultProductStock(product);
		Boolean stockExists = defaultStockOpt.isPresent();

		if(stockExists) {
			StocksEntity defaultStock = defaultStockOpt.get();
			productRep.setPrice( defaultStock.getPrice() );
			productRep.setDiscount( defaultStock.getDiscount() );
			productRep.setStockId( defaultStock.getId());
			productRep.setDefaultVariantFeatures( defaultStock.getProductVariantsEntity().getFeatureSpec());
			if (defaultStock.getCurrency() != null) {
				productRep.setCurrency( defaultStock.getCurrency().ordinal() );
			}
		}
		productRep.setHidden(!stockExists);

		return productRep;
	}


	private void setProductProperties(ProductRepresentationObject productRep, ProductEntity product) {
		productRep.setId(product.getId());
		productRep.setName(product.getName());
		productRep.setPname(product.getPname());
		productRep.setBrandId(product.getBrandId());
		productRep.setCategoryId(product.getCategoryId());
		productRep.setBarcode(product.getBarcode());
		productRep.setCreationDate(Optional.ofNullable(product.getCreationDate().toString()).orElse(null));
		productRep.setUpdateDate(Optional.ofNullable(product.getUpdateDate().toString()).orElse(null));

	}

	private void setProductStockProperties(ProductRepresentationObject productRep, StocksEntity stock) {
		productRep.setPrice(stock.getPrice());
		productRep.setDiscount(stock.getDiscount());
		productRep.setStockId(stock.getId());
		productRep.setDefaultVariantFeatures(stock.getProductVariantsEntity().getFeatureSpec());
		if (stock.getCurrency() != null) {
			productRep.setCurrency( stock.getCurrency().ordinal() );
		}
	}



	private ProductRepresentationObject getProductRepresentation(ProductEntity product, Map<Long, String> productCoverImgs) {
		ProductRepresentationObject rep = getProductRepresentation(product);
		setAdditionalInfo(rep, productCoverImgs);
		return rep;
	}




	private ProductDetailsDTO toProductDetailsDTO(ProductEntity product) throws IllegalAccessException, InvocationTargetException {
		ProductDetailsDTO dto = new ProductDetailsDTO();
		ProductRepresentationObject representationObj = getProductRepresentation(product);
		BeanUtils.copyProperties( dto , representationObj);
		dto.setDescription( product.getDescription() );
		dto.setProductType( product.getProductType() );
		dto.setImageUrl(imgService.getProductCoverImage( product.getId() ));

		return dto;
	}




	public boolean updateProductTags(ProductTagDTO productTagDTO) throws BusinessException {
		validateProductTagDTO(productTagDTO);

		List<Long> productIds = productTagDTO.getProductIds();
		List<Long> tagIds = productTagDTO.getTagIds();
		Map<Long, ProductEntity> productsMap = validateAndGetProductMap(productIds);
		Map<Long, TagsEntity> tagsMap = validateAndGetTagMap(tagIds);

		List<Pair> productTagsList = new ArrayList<>();
		if(noneIsNull(productIds, tagIds) && nonIsEmpty(productIds, tagIds)) {
			productTagsList = productRepository.getProductTags(productIds, tagIds);
		}


		for(Long productId : productTagDTO.getProductIds()) {
			for(Long tagId : productTagDTO.getTagIds()) {
				if( !productTagsList.contains(new Pair(productId, tagId))) {
					productsMap.get(productId).insertProductTag(tagsMap.get(tagId));
				}
			}
			productRepository.save(productsMap.get(productId));
		}
		return true;
	}




	public boolean deleteProductTags(ProductTagDTO productTagDTO) throws BusinessException {
		validateProductTagDTO(productTagDTO);

		Map<Long, ProductEntity> productsMap = validateAndGetProductMap(productTagDTO.getProductIds());
		Map<Long, TagsEntity> tagsMap = validateAndGetTagMap(productTagDTO.getTagIds());

		List<Pair> productTagsList = productRepository.getProductTags(productTagDTO.getProductIds(), productTagDTO.getTagIds());

		for(Long productId : productTagDTO.getProductIds()) {
			for(Long tagId : productTagDTO.getTagIds()) {
				if( productTagsList.contains(new Pair(productId, tagId)))
					productsMap.get(productId).removeProductTag(tagsMap.get(tagId));
				else
					throw new BusinessException("INVALID PARAM", "Link between product "+ productId +" and tag "+ tagId +" doesn't exist!", HttpStatus.NOT_ACCEPTABLE);
			}
			productRepository.save(productsMap.get(productId));
		}
		return true;
	}

	private void validateProductTagDTO(ProductTagDTO productTagDTO) throws BusinessException {
		if(productTagDTO.getProductIds() == null)
			throw new BusinessException("Provided products_ids can't be empty", "MISSING PARAM:products_ids", HttpStatus.NOT_ACCEPTABLE);

		if(productTagDTO.getTagIds() == null)
			throw new BusinessException("Provided tags_ids can't be empty", "MISSING PARAM:tags_ids", HttpStatus.NOT_ACCEPTABLE);
	}






	private Map<Long, ProductEntity> validateAndGetProductMap(List<Long> productIds) throws BusinessException {

		Map<Long, ProductEntity> productsMap = new HashMap<>();

		for(ProductEntity entity : productRepository.findByIdIn(productIds)) {
			productsMap.put(entity.getId(), entity);
		}

		for(Long productId : productIds) {
			if (productsMap.get(productId) == null)
				throw new BusinessException(
						format("Provided product_id(%d) does't match any existing product", productId),
						"INVALID PARAM:product_id", HttpStatus.NOT_ACCEPTABLE);
		}

		return productsMap;
	}






	private Map<Long, TagsEntity> validateAndGetTagMap(List<Long> tagIds) throws BusinessException {

		Map<Long, TagsEntity> tagsMap = new HashMap<>();
		Long orgId = securityService.getCurrentUserOrganizationId();

		for(TagsEntity entity : orgTagRepo.findByIdInAndOrganizationEntity_Id(tagIds, orgId)) {
			tagsMap.put(entity.getId(), entity);
		}

		for(Long tagId : tagIds) {
			if (tagsMap.get(tagId) == null)
				throw new BusinessException(
						format("Provided tag_id(%d) doesn't match any existing tag for organization(%d)", tagId, orgId),
						"INVALID PARAM:tag_id", HttpStatus.NOT_ACCEPTABLE);
		}

		return tagsMap;
	}

}
