package com.nasnav.service;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.nasnav.commons.utils.CollectionUtils.divideToBatches;
import static com.nasnav.commons.utils.CollectionUtils.mapInBatches;
import static com.nasnav.commons.utils.CollectionUtils.processInBatches;
import static com.nasnav.commons.utils.EntityUtils.anyIsNull;
import static com.nasnav.commons.utils.EntityUtils.areEqual;
import static com.nasnav.commons.utils.EntityUtils.isNullOrEmpty;
import static com.nasnav.commons.utils.EntityUtils.noneIsNull;
import static com.nasnav.commons.utils.StringUtils.encodeUrl;
import static com.nasnav.commons.utils.StringUtils.isBlankOrNull;
import static com.nasnav.constatnts.EntityConstants.Operation.CREATE;
import static com.nasnav.constatnts.EntityConstants.Operation.UPDATE;
import static com.nasnav.constatnts.error.product.ProductSrvErrorMessages.ERR_CANNOT_DELETE_BUNDLE_ITEM;
import static com.nasnav.constatnts.error.product.ProductSrvErrorMessages.ERR_CANNOT_DELETE_PRODUCT_BY_OTHER_ORG_USER;
import static com.nasnav.constatnts.error.product.ProductSrvErrorMessages.ERR_CANNOT_DELETE_PRODUCT_USED_IN_NEW_ORDERS;
import static com.nasnav.constatnts.error.product.ProductSrvErrorMessages.ERR_INVALID_EXTRA_ATTR_STRING;
import static com.nasnav.constatnts.error.product.ProductSrvErrorMessages.ERR_PRODUCT_DELETE_FAILED;
import static com.nasnav.constatnts.error.product.ProductSrvErrorMessages.ERR_PRODUCT_HAS_NO_VARIANTS;
import static com.nasnav.constatnts.error.product.ProductSrvErrorMessages.ERR_PRODUCT_NOT_EXISTS;
import static com.nasnav.constatnts.error.product.ProductSrvErrorMessages.ERR_PRODUCT_READ_FAIL;
import static com.nasnav.constatnts.error.product.ProductSrvErrorMessages.ERR_PRODUCT_STILL_USED;
import static com.nasnav.enumerations.OrderStatus.NEW;
import static com.nasnav.enumerations.Settings.HIDE_EMPTY_STOCKS;
import static com.nasnav.enumerations.Settings.SHOW_FREE_PRODUCTS;
import static com.nasnav.exceptions.ErrorCodes.GEN$0002;
import static com.nasnav.exceptions.ErrorCodes.P$BRA$0001;
import static com.nasnav.exceptions.ErrorCodes.P$BRA$0002;
import static com.nasnav.exceptions.ErrorCodes.P$PRO$0001;
import static com.nasnav.exceptions.ErrorCodes.P$PRO$0002;
import static com.nasnav.exceptions.ErrorCodes.P$PRO$0003;
import static com.nasnav.exceptions.ErrorCodes.P$PRO$0004;
import static com.nasnav.exceptions.ErrorCodes.P$PRO$0005;
import static com.nasnav.exceptions.ErrorCodes.P$PRO$0006;
import static com.nasnav.exceptions.ErrorCodes.P$PRO$0008;
import static com.nasnav.exceptions.ErrorCodes.P$VAR$0001;
import static com.nasnav.exceptions.ErrorCodes.P$VAR$0002;
import static com.nasnav.exceptions.ErrorCodes.P$VAR$003;
import static com.nasnav.exceptions.ErrorCodes.S$0006;
import static com.nasnav.persistence.ProductTypes.BUNDLE;
import static com.nasnav.persistence.ProductTypes.COLLECTION;
import static com.querydsl.core.types.dsl.Expressions.cases;
import static com.querydsl.core.types.dsl.Expressions.constant;
import static java.lang.String.format;
import static java.math.BigDecimal.ZERO;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static java.util.Collections.singletonList;
import static java.util.Comparator.comparing;
import static java.util.Objects.nonNull;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.springframework.beans.BeanUtils.copyProperties;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import com.nasnav.model.querydsl.sql.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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
import com.nasnav.dao.OrdersRepository;
import com.nasnav.dao.Product360ShopsRepository;
import com.nasnav.dao.ProductCollectionRepository;
import com.nasnav.dao.ProductFeaturesRepository;
import com.nasnav.dao.ProductImagesRepository;
import com.nasnav.dao.ProductImgsCustomRepository;
import com.nasnav.dao.ProductRepository;
import com.nasnav.dao.ProductVariantsRepository;
import com.nasnav.dao.ProductsCustomRepository;
import com.nasnav.dao.ShopsRepository;
import com.nasnav.dao.StockRepository;
import com.nasnav.dao.TagsRepository;
import com.nasnav.dto.BundleDTO;
import com.nasnav.dto.BundleElementUpdateDTO;
import com.nasnav.dto.ExtraAttributeDTO;
import com.nasnav.dto.Organization_BrandRepresentationObject;
import com.nasnav.dto.Pair;
import com.nasnav.dto.Prices;
import com.nasnav.dto.ProductBaseInfo;
import com.nasnav.dto.ProductDetailsDTO;
import com.nasnav.dto.ProductImageDTO;
import com.nasnav.dto.ProductImageUpdateDTO;
import com.nasnav.dto.ProductRepresentationObject;
import com.nasnav.dto.ProductSortOptions;
import com.nasnav.dto.ProductTagDTO;
import com.nasnav.dto.ProductUpdateDTO;
import com.nasnav.dto.ProductsFiltersResponse;
import com.nasnav.dto.ProductsResponse;
import com.nasnav.dto.StockDTO;
import com.nasnav.dto.TagsRepresentationObject;
import com.nasnav.dto.VariantDTO;
import com.nasnav.dto.VariantFeatureDTO;
import com.nasnav.dto.VariantUpdateDTO;
import com.nasnav.dto.request.product.CollectionItemDTO;
import com.nasnav.dto.request.product.Product360ShopsDTO;
import com.nasnav.dto.response.navbox.VariantsResponse;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.exceptions.ErrorCodes;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.BaseUserEntity;
import com.nasnav.persistence.BundleEntity;
import com.nasnav.persistence.ExtraAttributesEntity;
import com.nasnav.persistence.ProductCollectionEntity;
import com.nasnav.persistence.ProductEntity;
import com.nasnav.persistence.ProductExtraAttributesEntity;
import com.nasnav.persistence.ProductFeaturesEntity;
import com.nasnav.persistence.ProductImagesEntity;
import com.nasnav.persistence.ProductVariantsEntity;
import com.nasnav.persistence.Shop360ProductsEntity;
import com.nasnav.persistence.ShopsEntity;
import com.nasnav.persistence.StocksEntity;
import com.nasnav.persistence.TagsEntity;
import com.nasnav.persistence.dto.query.result.products.BrandBasicData;
import com.nasnav.persistence.dto.query.result.products.ProductTagsBasicData;
import com.nasnav.request.BundleSearchParam;
import com.nasnav.request.ProductSearchParam;
import com.nasnav.response.BundleResponse;
import com.nasnav.response.ProductImageDeleteResponse;
import com.nasnav.response.ProductImageUpdateResponse;
import com.nasnav.response.ProductUpdateResponse;
import com.nasnav.response.ProductsDeleteResponse;
import com.nasnav.response.VariantUpdateResponse;
import com.nasnav.service.helpers.CachingHelper;
import com.nasnav.service.model.ProductTagPair;
import com.nasnav.service.model.VariantBasicData;
import com.nasnav.service.model.VariantCache;
import com.nasnav.service.model.VariantIdentifier;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.SubQueryExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.sql.SQLExpressions;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.SQLQueryFactory;

import lombok.AllArgsConstructor;
import lombok.Data;

@Service
public class ProductService {

	private Logger logger = LogManager.getLogger();

	//	@Value("${products.default.start}")
	private Integer defaultStart = 0;
	//	@Value("${products.default.count}")
	private Integer defaultCount = 10;
	//	@Value("${products.default.sort.attribute}")
	private ProductSortOptions defaultSortAttribute = ProductSortOptions.UPDATE_DATE;
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
	private OrdersRepository ordersRepository;

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
	private ProductImgsCustomRepository productImgsCustomRepo;

	@Autowired
	private ProductCollectionRepository productCollectionRepo;

	@Autowired
	private JdbcTemplate template;
	
	@Autowired
	private CachingHelper cachingHelper;
	
	@Autowired
	private SQLQueryFactory queryFactory;
	
	@Autowired
	private ProductsCustomRepository productsCustomRepo;

	@Autowired
	private ShopsRepository shopsRepo;

	@Autowired
	private Product360ShopsRepository product360ShopsRepo;
	
	@Autowired
	private OrganizationService orgService;

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



	@Transactional
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

		List<ProductImageDTO> productsAndVariantsImages = imgService.getProductsAndVariantsImages(asList(product.getId()),
				productVariants.stream()
						.map(ProductVariantsEntity::getId)
						.collect(toList()));

		List<VariantDTO> variantsDTOList = getVariantsList(productVariants, product.getId(), shopId, productsAndVariantsImages);
		List<TagsRepresentationObject> tagsDTOList = getProductTagsDTOList(product.getId());
		List<Long> product360Shops = product360ShopsRepo.findShopsByProductId(product.getId());
		ProductDetailsDTO productDTO = null;
		try {
			productDTO = toProductDetailsDTO(product);
			productDTO.setVariants(variantsDTOList);
			productDTO.setShops(product360Shops);
			if (variantsDTOList != null && variantsDTOList.size() > 1)
				productDTO.setMultipleVariants(true);
				productDTO.setVariantFeatures( getVariantFeatures(productVariants) );
				productDTO.setBundleItems( getBundleItems(product));
				productDTO.setImages( getProductImages(productsAndVariantsImages) );
				productDTO.setTags(tagsDTOList);
		} catch (IllegalAccessException | InvocationTargetException e) {
			logger.error(e,e);
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
				.collect(toList()))
				.stream()
				.map(tag ->(TagsRepresentationObject) tag.getRepresentation())
				.collect(toList());
	}

	private Map<Long,List<TagsRepresentationObject>> getProductsTagsDTOList(List<Long> productsIds) {
		Map<Long,List<TagsRepresentationObject>> result = new HashMap<>();

		List<Pair> productsTags = productRepository.getTagsByProductIdIn(productsIds);
		List<TagsEntity> productsTagsDTOs =  
				orgTagRepo
					.findByIdIn(productsTags.stream()
					.map(p -> p.getSecond())
					.distinct()
					.collect(toList()));
		for (Long productId: productsIds) {
			List<Long> productTagsIds = 
					productsTags
					.stream()
					.filter(pair -> pair.getFirst().equals(productId))
					.map(pair -> pair.getSecond())
					.collect(toList());
			List<TagsRepresentationObject> productTagsDTOs = 
					productsTagsDTOs
					.stream()
					.filter(tag -> productTagsIds.contains(tag.getId()))
					.map(tag -> (TagsRepresentationObject)tag.getRepresentation())
				   	.collect(toList());
			result.put(productId, productTagsDTOs);
		}

		return result;
	}

	
	
	
	private List<Long> filterProductsWithMultipleVariants(List<Long> productsIds) {
		QProducts product = QProducts.products;
		QProductVariants variant = QProductVariants.productVariants;

		SQLQuery<Long> query = 
				queryFactory
				.select(product.id)
				.from(variant)
				.innerJoin(product).on(product.id.eq(variant.productId))
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
		if (bundleProductsIdList.isEmpty()) {
			return new ArrayList<>();
		}
		List<ProductEntity> bundleProducts = this.getProductsByIds(bundleProductsIdList , "asc", "name");
		ProductsResponse response = this.getProductsResponse(bundleProducts,"asc" , "name" ,  (long)bundleProducts.size() );
		List<ProductRepresentationObject> productRepList = response.getProducts();
		return productRepList;
	}


	private List<VariantDTO> getVariantsList(List<ProductVariantsEntity> productVariants, Long productId, Long shopId,
											 List<ProductImageDTO> variantsImages) throws BusinessException{

		return productVariants.stream()
				.map(variant -> createVariantDto(shopId, variant, variantsImages))
				.filter( variant -> !variant.getStocks().isEmpty())
				.collect(toList());
	}




	private VariantDTO createVariantDto(Long shopId, ProductVariantsEntity variant, List<ProductImageDTO> variantsImages)  {
		VariantDTO variantObj = new VariantDTO();
		variantObj.setId(variant.getId());
		variantObj.setName(variant.getName());
		variantObj.setDescription(variant.getDescription());
		variantObj.setBarcode( variant.getBarcode() );
		variantObj.setStocks( getStockList(variant, shopId) );
		variantObj.setVariantFeatures( getVariantFeaturesValuesWithNullDefault(variant) );
		variantObj.setImages( getProductVariantImages(variant.getId(), variantsImages) );
		variantObj.setExtraAttributes( getExtraAttributesList(variant));
		variantObj.setSku(variant.getSku());
		variantObj.setProductCode(variant.getProductCode());
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


	
	
	
	
	private Map<String,String> getVariantFeaturesValuesWithNullDefault(ProductVariantsEntity variant) {
		Map<String,String> features = getVariantFeaturesValues(variant);
		return features.isEmpty()? null : features;
	}
	
	
	


	private Map<String,String> getVariantFeaturesValues(ProductVariantsEntity variant) {
		if(variant == null || !hasFeatures(variant)) {
			return emptyMap();
		}
		return parseVariantFeatures(variant.getFeatureSpec(), 1);
	}



	public Map<String, String> parseVariantFeatures(String variantFeatures, Integer returnedName) {
		String normalizedStr = ofNullable(variantFeatures).orElse("{}");
		JacksonJsonParser parser = new JacksonJsonParser();
		Map<String, Object> keyValueMap =  parser.parseMap(normalizedStr);
		return keyValueMap.entrySet()
				.stream()
				.map(e -> getVariantFeatureMapEntry(e, returnedName))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.collect(toMap(Map.Entry::getKey , Map.Entry::getValue));
	}





	private Optional<Map.Entry<String,String>> getVariantFeatureMapEntry(Map.Entry<String,Object> entry, Integer returnedName) {
		if(anyIsNull(entry, entry.getKey())) {
			return empty();
		}

		Integer id = Integer.parseInt(entry.getKey());
		return	productFeaturesRepository
					.findById(id)
					.map(feature -> createFeatureKeyValuePair(entry, feature, returnedName));
	}



	private SimpleEntry<String,String> createFeatureKeyValuePair(Map.Entry<String, Object> entry,
			ProductFeaturesEntity featureOptional, Integer returnedName) {
		if (returnedName.equals(0))
			return new AbstractMap.SimpleEntry<>(
					featureOptional.getName()
					, entry.getValue().toString());

		return new AbstractMap.SimpleEntry<>(
				featureOptional.getPname()
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
				.collect(toList());
	}







	private boolean hasFeatures(ProductVariantsEntity variant) {
		return variant.getFeatureSpec() != null && !variant.getFeatureSpec().isEmpty();
	}

	private List<ProductImageDTO> getProductImages(List<ProductImageDTO> productImages) {
		return productImages.stream()
							.filter(i -> i.getVariantId() == null)
							.map(i -> new ProductImageDTO(i.getId(), i.getImagePath(), i.getPriority()))
							.collect(toList());
	}



	private List<ProductImageDTO> getProductVariantImages(Long variantId, List<ProductImageDTO> variantsImages) {
		return variantsImages.stream()
							.filter(i -> i.getVariantId() != null)
							.filter(i -> Objects.equals(i.getVariantId(),variantId))
							.map(i -> new ProductImageDTO(i.getId(), i.getImagePath(), i.getPriority()))
							.collect(toList());
	}




	private List<StockDTO> getStockList(ProductVariantsEntity variant,Long shopId)  {
		List<StocksEntity> stocks = stockService.getVariantStockForShop(variant, shopId);
		return	stocks
				.stream()
				.filter(stock -> stock != null)
				.filter(stock -> Objects.equals(stock.getShopsEntity().getRemoved(), 0))
				.map(StockDTO::new)
				.collect(toList());
	}

	@Transactional
	public ProductsResponse getProducts(ProductSearchParam requestParams) throws BusinessException, InvocationTargetException, IllegalAccessException {
		ProductSearchParam params = getProductSearchParams(requestParams);

		SQLQuery<?> countStocks = getProductsQuery(params, true);
		String countQuery = countStocks.select(SQLExpressions.countAll).getSQL().getSQL();
		Long productsCount = template.queryForObject(countQuery, Long.class); 

		SQLQuery<?> stocks = getProductsQuery(params, false);

		stocks.select((Expressions.template(ProductRepresentationObject.class,"*")))
				 .limit(params.count).offset(params.start);

		List<ProductRepresentationObject> result = 
				template.query(stocks.getSQL().getSQL(),
						new BeanPropertyRowMapper<>(ProductRepresentationObject.class));

		return getProductResponseFromStocks(result, productsCount);
	}





	private SQLQuery<?> getProductsQuery(ProductSearchParam params, boolean count) {
		QStocks stock = QStocks.stocks;
		QProducts product = QProducts.products;
		QProductVariants variant = QProductVariants.productVariants;
		QShops shop = QShops.shops;

		BooleanBuilder predicate = getQueryPredicate(params, product, stock, shop, variant);

		OrderSpecifier<?> order = getProductQueryOrder(params, product, stock);

		SQLQuery<?> fromProductsClause = productsCustomRepo.getProductsBaseQuery(predicate, params);

		SQLQuery<?> fromCollectionsClause = productsCustomRepo.getCollectionsBaseQuery(predicate, params);

		SubQueryExpression productsQuery = getProductsQuery(stock, product, variant, fromProductsClause);

		SubQueryExpression collectionsQuery = getProductsQuery(stock, product, variant, fromCollectionsClause);

		SQLQuery<?> subQuery = new SQLQuery();

		SQLQuery<?> stocks =
				queryFactory
						.from(subQuery.union(productsQuery,collectionsQuery).as("total_products"))
						.where(Expressions.numberPath(Long.class, "row_num").eq(1L));

		if (order != null && !count)
			stocks.orderBy(order);

		return stocks;
	}


	private SubQueryExpression<?> getProductsQuery(QStocks stock, QProducts product, QProductVariants variant, SQLQuery fromClause) {
		SQLQuery<?> productsQuery = fromClause.select(
											stock.id.as("stock_id"),
											stock.quantity.as("quantity"),
											stock.price.as("price"),
											cases()
													.when(stock.price.ne(ZERO))
													.then(stock.discount.divide(stock.price).multiply(constant(100)))
													.otherwise(ZERO)
													.as("discount"),
											stock.currency,
											product.organizationId.as("organization_id"),
											stock.shopId.as("shop_id"),
											variant.barcode.as("variant_barcode"),
											variant.featureSpec,
											variant.productCode,
											variant.sku,
											product.id,
											product.barcode,
											product.brandId.as("brand_id"),
											product.categoryId.as("category_id"),
											product.description.as("description"),
											product.name.as("name"),
											product.pName.as("pname"),
											product.search_360.as("has_360_view"),
											product.createdAt.as("creation_date"),
											product.updatedAt.as("update_date"),
											product.productType,
											product.priority,
											SQLExpressions.rowNumber()
													.over()
													.partitionBy(product.id)
													.orderBy(stock.price).as("row_num"));

		return productsQuery;
	}
	
	
	public ProductsFiltersResponse getProductAvailableFilters(ProductSearchParam param) throws BusinessException, IllegalAccessException, InvocationTargetException, SQLException {
		ProductSearchParam finalParams = getProductSearchParams(param);

		QStocks stock = QStocks.stocks;
		QProducts product = QProducts.products;
		QShops shop = QShops.shops;
		QProductVariants variant = QProductVariants.productVariants;

		BooleanBuilder predicate = getQueryPredicate(finalParams, product, stock, shop, variant);

		SQLQuery<?> fromProductsClause = productsCustomRepo.getProductsBaseQuery(predicate, finalParams);
		SQLQuery<?> fromCollectionsClause = productsCustomRepo.getCollectionsBaseQuery(predicate, finalParams);

		Prices prices = getProductPrices(predicate, finalParams, stock);

		List<Organization_BrandRepresentationObject> brands = getProductBrands(predicate, finalParams, product);

		List<TagsRepresentationObject> tags = getProductTags(predicate, finalParams);

        Map<String, List<String>> variantsFeatures = getProductVariantFeatures(fromProductsClause);

		ProductsFiltersResponse response = new ProductsFiltersResponse(prices, brands, tags, variantsFeatures);

		return response;
	}


	private Prices getProductPrices(BooleanBuilder predicate, ProductSearchParam finalParams, QStocks stock) {
		SQLQuery<?> finalProductsQuery = productsCustomRepo.getProductsBaseQuery(predicate, finalParams);
		SQLQuery<?> finalCollectionsQuery = productsCustomRepo.getCollectionsBaseQuery(predicate, finalParams);

		SubQueryExpression products = finalProductsQuery.select(stock.price.min().as("minPrice"), stock.price.max().as("maxPrice"));
		SubQueryExpression collections = finalCollectionsQuery.select(stock.price.min().as("minPrice"), stock.price.max().as("maxPrice"));

		SQLQuery<?> sqlQuery = new SQLQuery<>();
		SQLQuery<?> query = queryFactory
				.select(SQLExpressions.min(Expressions.numberPath(BigDecimal.class, "minPrice")).as("minPrice"),
						SQLExpressions.min(Expressions.numberPath(BigDecimal.class, "maxPrice")).as("maxPrice"))
				.from(sqlQuery.union(products, collections).as("total"));

		return template.queryForObject(query.getSQL().getSQL() , new BeanPropertyRowMapper<>(Prices.class));
	}



	private List<Organization_BrandRepresentationObject> getProductBrands(BooleanBuilder predicate, ProductSearchParam finalParams, QProducts product) {
		QBrands brand = QBrands.brands;

		SQLQuery<?> finalProductsQuery = productsCustomRepo.getProductsBaseQuery(predicate, finalParams);
		SQLQuery<?> finalCollectionsQuery = productsCustomRepo.getCollectionsBaseQuery(predicate, finalParams);

		SubQueryExpression products = queryFactory
				.select(brand.id, brand.name)
				.from(brand)
				.where(brand.id.in(finalProductsQuery.select(product.brandId)));
		SubQueryExpression collections = queryFactory
				.select(brand.id, brand.name)
				.from(brand)
				.where(brand.id.in(finalCollectionsQuery.select(product.brandId)));

		SQLQuery<?> sqlQuery = new SQLQuery<>();
		SQLQuery<?> query = queryFactory
				.select(Expressions.numberPath(Long.class, "id"),Expressions.stringPath("name"))
				.from(sqlQuery.union(products, collections).as("total"));

		return template.query(query.getSQL().getSQL(),
				new BeanPropertyRowMapper<>(Organization_BrandRepresentationObject.class));
	}


	private List<TagsRepresentationObject> getProductTags(BooleanBuilder predicate, ProductSearchParam finalParams) {
		QTags tag = QTags.tags;
		QProducts product = QProducts.products;
		QProductTags productTag = QProductTags.productTags;

		SQLQuery<?> finalProductsQuery = productsCustomRepo.getProductsBaseQuery(predicate, finalParams);
		SQLQuery<?> finalCollectionsQuery = productsCustomRepo.getCollectionsBaseQuery(predicate, finalParams);

		SQLQuery<?> sqlQuery = new SQLQuery<>();
		SubQueryExpression union = sqlQuery.union(finalProductsQuery.select(product.id),
												  finalCollectionsQuery.select(product.id));
		SQLQuery<?> tags = queryFactory
				.select(tag.id, tag.name, tag.alias, tag.metadata, tag.pName)
				.from(tag)
				.where(tag.id.in(queryFactory
									.select(productTag.tagId)
									.from(productTag)
									.where(productTag.productId.in(union))));

		return template.query(tags.getSQL().getSQL(),
				new BeanPropertyRowMapper<>(TagsRepresentationObject.class));

	}

	
	private Map<String, List<String>> getProductVariantFeatures(SQLQuery<?> baseQuery) throws SQLException {
		QProductVariants variant = QProductVariants.productVariants;
		QProductFeatures feature = QProductFeatures.productFeatures;

		SQLQuery<?> featuresVal =
				queryFactory
				.select(
						Expressions.numberTemplate(Long.class, "(json_each(text_to_json(feature_spec))).key::int8").as("id"),
						Expressions.stringTemplate("(json_each(text_to_json(feature_spec))).value::varchar").as("feature_value"))
				.from(baseQuery
						.select(variant.featureSpec).as("product_variants"))
				.where(variant.featureSpec.isNotNull()
						.and(variant.featureSpec.ne("{}")));

		SQLQuery<?> query = queryFactory.select(Expressions.numberTemplate(Long.class, "features_val.id"),
										Expressions.stringTemplate("name"),
										Expressions.stringTemplate("p_name"),
										Expressions.stringTemplate("features_val.feature_value").as("value")).distinct()
								.from(featuresVal.as("features_val")).leftJoin(feature)
								.on(feature.id.eq(Expressions.numberTemplate(Long.class, "features_val.id")));

		List<com.nasnav.dto.response.navbox.VariantFeatureDTO> variantsList =
						template.query(query.getSQL().getSQL(),
							new BeanPropertyRowMapper<>(com.nasnav.dto.response.navbox.VariantFeatureDTO.class));


		return variantsList
				.stream()
				.collect(
						groupingBy(com.nasnav.dto.response.navbox.VariantFeatureDTO::getName
									, mapping(d -> d.getValue().replace("\"", ""), toList())));
	}


	private OrderSpecifier<?> getProductQueryOrder(ProductSearchParam params, QProducts product, QStocks stock) {
		if (params.getOrder().equals(SortOrder.DESC))
			switch (params.getSort()) {
				case ID : return product.id.as("id").desc();
				case NAME: return product.name.as("name").desc();
				case P_NAME: return product.pName.as("pname").desc();
				case CREATION_DATE: return product.createdAt.as("creation_date").desc();
				case UPDATE_DATE: return product.updatedAt.as("update_date").desc();
				case PRICE: return stock.price.as("price").desc();
				case PRIORITY: return product.priority.as("priority").desc();
			}
		else if (params.getOrder().equals(SortOrder.ASC))
			switch (params.getSort()) {
				case ID : return product.id.as("id").asc();
				case NAME: return product.name.as("name").asc();
				case P_NAME: return product.pName.as("pname").asc();
				case CREATION_DATE: return product.createdAt.as("creation_date").asc();
				case UPDATE_DATE: return product.updatedAt.as("update_date").asc();
				case PRICE: return stock.price.as("price").asc();
				case PRIORITY: return product.priority.as("priority").asc();
			}
		return product.updatedAt.desc();
	}

	
	
	
	private BooleanBuilder getQueryPredicate(ProductSearchParam params, QProducts product
								, QStocks stock, QShops shop, QProductVariants variant) {
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
						   .or(product.description.likeIgnoreCase("%" + params.name + "%") )
						   .or(variant.productCode.likeIgnoreCase("%" + params.name + "%") )
						   .or(variant.sku.likeIgnoreCase("%" + params.name + "%") ));

		if(params.shop_id != null && params.org_id == null)
			predicate.and( stock.shopId.eq(params.shop_id) );

		if(params.product_type != null)
			predicate.and( product.productType.in(params.product_type));
		
		if(params.hide_empty_stocks) {
			predicate.and( stock.quantity.gt(0));
		}
		
		if(!params.show_free_products) {
			predicate.and( stock.price.gt(ZERO));
		}

		predicate.and( shop.removed.eq(0) );

		return predicate;
	}

	

	ProductSearchParam getProductSearchParams(ProductSearchParam oldParams) throws BusinessException, InvocationTargetException, IllegalAccessException {
		ProductSearchParam params = new ProductSearchParam();
		copyProperties(oldParams, params);

		if (oldParams.order != null)
			params.setOrder(oldParams.order.getValue());

		if (oldParams.sort != null)
			params.setSort(oldParams.sort);

		if (params.sort != null && ProductSortOptions.getProductSortOptions(params.sort.getValue()) == null)
			throw new BusinessException("Sort is limited to id, name, pname, price", "", BAD_REQUEST);

		if (params.order != null && !params.order.getValue().equals("asc") && !params.order.getValue().equals("desc"))
			throw new BusinessException("Order is limited to asc and desc only", "", BAD_REQUEST);

		if (params.start != null && params.start < 0)
			throw new BusinessException("Start can be zero or more", "", BAD_REQUEST);

		if (params.count != null && params.count < 1)
			throw new BusinessException("Count can be One or more", "", BAD_REQUEST);

		if (params.org_id == null && params.shop_id == null)
			throw new BusinessException("Shop Id or Organization Id shall be provided", "", BAD_REQUEST);

		if (params.minPrice != null && params.minPrice.compareTo(ZERO) < 0)
			params.minPrice = ZERO;

		if (params.start == null)
			params.start = defaultStart;

		params.count = getProductCountParam(params.count);

		if (params.sort == null)
			params.setSort(defaultSortAttribute);

		if (params.order == null)
			params.setOrder(defaultOrder);

		Map<String,String> orgSettings = orgService.getOrganizationSettings(params.org_id);
		
		params.show_free_products = isShowFreeProductsAllowed(orgSettings);
		
		params.hide_empty_stocks = isHideEmptyStocksAllowed(params, orgSettings);
		
		return params;
	}



	private Boolean isHideEmptyStocksAllowed(ProductSearchParam params, Map<String,String> orgSettings) {
		boolean hideStocksSetting = 
				ofNullable(orgSettings)
				.map(settings -> settings.get(HIDE_EMPTY_STOCKS.name()))
				.map(Boolean::parseBoolean)
				.orElse(false);
		return ofNullable(params.hide_empty_stocks)
				.orElse(hideStocksSetting);
	}



	private Boolean isShowFreeProductsAllowed(Map<String, String> orgSettings) {
		return ofNullable(orgSettings)
		.map(settings -> settings.get(SHOW_FREE_PRODUCTS.name()))
		.map(Boolean::parseBoolean)
		.orElse(false);
	}

	Integer getProductCountParam(Integer count) {
		return count == null ? defaultCount : count < 1000 ? count : 1000;
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
				.collect(toList());

		List<Long> variantsIdList = products.stream()
				.map(ProductEntity::getProductVariants)
				.flatMap(Set::stream)
				.map(ProductVariantsEntity::getId)
				.collect(toList());

		Map<Long, String> productCoverImages = imgService.getProductsImagesMap(productIdList, variantsIdList);

		List<ProductRepresentationObject> productsRep =
				products.stream()
					  .map(prod -> getProductRepresentation(prod, productCoverImages))
					  .collect(toList());

		if (ProductSortOptions.getProductSortOptions(sort) == ProductSortOptions.PRICE)
			sortByPrice(productsRep, order);

		return new ProductsResponse(productsCount, productsRep);

	}

	private ProductsResponse getProductResponseFromStocks(List<ProductRepresentationObject> stocks,
														  Long productsCount) {
		if(stocks != null && !stocks.isEmpty()) {
			List<Long> stocksIds = stocks.stream()
					.map(ProductRepresentationObject::getStockId)
					.collect(toList());

			List<Long> productIdList = stocks.stream()
					.map(ProductRepresentationObject::getId)
					.collect(toList());

			List<Long> variantsIds = productVariantsRepository.getVariantsIdsByStocksIds(stocksIds);

			Map<Long, Prices> productsPricesMap =
					mapInBatches(productIdList, 500, stockRepository::getProductsPrices)
					.stream()
					.collect(toMap(Prices::getId, p -> new Prices(p.getMinPrice(), p.getMaxPrice())));

			Map<Long, Prices> collectionsPricesMap =
					mapInBatches(productIdList, 500, stockRepository::getCollectionsPrices)
							.stream()
							.collect(toMap(Prices::getId, p -> new Prices(p.getMinPrice(), p.getMaxPrice())));

			Map<Long, String> productCoverImages = imgService.getProductsImagesMap(productIdList, variantsIds);

			Map<Long, List<TagsRepresentationObject>> productsTags = getProductsTagsDTOList(productIdList);

			List<Long> productsVariantsCountFlag = filterProductsWithMultipleVariants(productIdList);

			Map<Long, List<Long>> product360Shops = getProducts360ShopsList(productIdList);

			stocks.stream()
					.map(s -> setAdditionalInfo(s, productCoverImages))
					.map(s -> setProductTags(s, productsTags))
					.map(s -> setProductMultipleVariants(s, productsVariantsCountFlag))
					.map(s -> setProductPrices(s, productsPricesMap))
					.map(s -> setCollectionPrices(s, collectionsPricesMap))
					.map(s -> setProductShops(s, product360Shops))
					.collect(toList());
		}

		return new ProductsResponse(productsCount, stocks);

	}


	private ProductRepresentationObject setProductShops(ProductRepresentationObject product, Map<Long, List<Long>> shops) {
		List<Long> shopsList = shops.get(product.getId()) != null ? shops.get(product.getId()) : new ArrayList<Long>();
		product.setShops(shopsList);
		return product;
	}


	private Map<Long, List<Long>> getProducts360ShopsList(List<Long> productIdList) {
		return product360ShopsRepo.findByProductEntity_IdInAndPublished(productIdList, (short)2)
				.stream()
				.collect(groupingBy(ps -> ps.getProductEntity().getId(),
						 mapping(ps -> ps.getShopEntity().getId(),
								toList())));
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


	private ProductRepresentationObject setProductPrices(ProductRepresentationObject product,
													   Map<Long, Prices> pricesMap) {
		Prices prices = pricesMap.get(product.getId());
		if (product.getProductType().intValue() == 0)
			product.setPrices(prices);
		return product;
	}


	private ProductRepresentationObject setCollectionPrices(ProductRepresentationObject product,
														 Map<Long, Prices> pricesMap) {
		Prices prices = pricesMap.get(product.getId());
		if (product.getProductType().intValue() == 2)
			product.setPrices(prices);
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
		Optional<String> imgUrl = ofNullable(product.getId()).map(productCoverImgs::get);
		if(imgUrl.isPresent()){
			product.setImageUrl(imgUrl.get());
		}else{
			product.setHidden(true);
		}
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





	public ProductUpdateResponse updateProduct(String productJson, Boolean isBundle, Boolean isCollection) {
		Long id = updateProductBatch(asList(productJson), isBundle, isCollection)
					.stream()
					.findFirst()
					.orElse(null);
		return new ProductUpdateResponse(id);
	}
	
	
	
	
	public List<Long> updateProductBatch(List<String> productJsonList, Boolean isBundle, Boolean isCollection){
		ProductUpdateCache cache = createProductUpdateCache(productJsonList, isBundle);
		List<ProductEntity> entities = prepareProductEntities(productJsonList, isBundle, isCollection, cache);
		Iterable<ProductEntity> saved = productRepository.saveAll(entities); 
		return getProductIds(saved);
	}



	private List<Long> getProductIds(Iterable<ProductEntity> saved) {
		return StreamSupport
				.stream(saved.spliterator(), false)
				.map(ProductEntity::getId)
				.collect(toList());
	}



	private List<ProductEntity> prepareProductEntities(List<String> productJsonList, Boolean isBundle, Boolean isCollection,
			ProductUpdateCache cache) {
		return productJsonList
				.stream()
				.map(json -> prepareProductEntity(json, isBundle, isCollection, cache))
				.collect(toList());
	}
	
	
	
	
	private ProductEntity prepareProductEntity(String productJson, Boolean isBundle, Boolean isCollection, ProductUpdateCache cache) {
		ObjectMapper mapper = createObjectMapper();
		JsonNode rootNode;
		try {
			rootNode = mapper.readTree(productJson);
		} catch (IOException e) {
			logger.error(e,e);
			throw new RuntimeBusinessException(format("Failed to deserialize JSON string [%s]", productJson), "INTERNAL SERVER ERROR", INTERNAL_SERVER_ERROR);
		}

		validateProductDto(rootNode, cache);

		return prepareProdcutEntity(rootNode, isBundle, isCollection, cache);
	}




	private ProductUpdateCache createProductUpdateCache(List<String> productJsonList, Boolean isBundle) {
		List<JSONObject> productsJson = productJsonList.stream().map(JSONObject::new).collect(toList());
		Map<Long, ProductEntity> products = createProductCache(productsJson);		
		Map<Long, BrandBasicData> brands = createBrandsCache(productsJson);
		
		return new ProductUpdateCache(products, brands);
	}



	private Map<Long, ProductEntity> createProductCache(List<JSONObject> productsJson) {
		Set<Long> productIds = extractProductIds(productsJson);
		
		return mapInBatches(productIds, 500, productRepository::findByIdIn)
				.stream()
				.collect(toMap(ProductEntity::getId, product -> product , (prod1, prod2) -> prod2));
	}



	private Set<Long> extractProductIds(List<JSONObject> productsJson) {
		return productsJson
				.stream()
				.filter(json -> json.has("product_id"))
				.filter(json -> !json.isNull("product_id"))
				.map(json -> json.getLong("product_id"))
				.filter(Objects::nonNull)
				.collect(toSet());
	}



	private Map<Long, BrandBasicData> createBrandsCache(List<JSONObject> productsJson) {
		List<Long> brandIds = extractBrandIds(productsJson);
		
		return mapInBatches(brandIds, 500, brandRepo::findByIdIn)
				.stream()
				.distinct()
				.collect(toMap(BrandBasicData::getId, brand -> brand));
	}



	private List<Long> extractBrandIds(List<JSONObject> productsJson) {
		return productsJson
				.stream()
				.filter(json -> json.has("brand_id"))
				.filter(json -> !json.isNull("brand_id"))
				.map(json -> json.getLong("brand_id"))
				.filter(Objects::nonNull)
				.collect(toList());
	}



	private ProductEntity prepareProdcutEntity(JsonNode productJsonNode, Boolean isBundle, Boolean isCollection, ProductUpdateCache cache){

		Long id = productJsonNode.path("product_id").asLong();
		JsonNode operationNode = productJsonNode.path("operation");
		Operation operation = Operation.valueOf(operationNode.asText().toUpperCase());

		ProductEntity entity;

		if(CREATE.equals(operation)) {
			entity = new ProductEntity();
			if(isBundle) {
				entity.setProductType(BUNDLE);
			} else if (isCollection) {
				entity.setProductType(COLLECTION);
			}
		}
		else {
			entity = ofNullable(cache.getProducts())
					.map(map -> map.get(id))
					.orElseThrow(()-> new RuntimeBusinessException(NOT_ACCEPTABLE, P$PRO$0002, id));
		}

		updateProductEntityFromJson(entity, productJsonNode);

		return entity;
	}




	private void updateProductEntityFromJson(ProductEntity entity, JsonNode productJsonNode){
		ProductUpdateDTO productDto = new ProductUpdateDTO();
		Long orgId = securityService.getCurrentUserOrganizationId();
		try {
			copyProperties(entity, productDto);

			//readerForUpdating makes the reader update the properties that ONLY exists in JSON string
			//That's why we are parsing the JSON instead of spring (-_-)
			ObjectMapper mapper = createObjectMapper();
			productDto = mapper.readerForUpdating(productDto).readValue(productJsonNode.toString());

			productDto.setOrganizationId(orgId);

			if(isBlankOrNull(productDto.getPname())) {
				productDto.setPname(encodeUrl( productDto.getName() ));
			}

			copyProperties(productDto, entity);
		} catch (Exception e) {
			logger.error(e,e);
			throw new RuntimeBusinessException(INTERNAL_SERVER_ERROR, P$PRO$0006, productJsonNode.toString());
		}
	}




	private void validateProductDto(JsonNode productJsonNode, ProductUpdateCache cache) {
		JsonNode operationNode = productJsonNode.path("operation");

		if(operationNode.isMissingNode()) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, ErrorCodes.P$PRO$0007);
		}

		String operationStr = operationNode.asText().toUpperCase();
		Operation operation = Operation.valueOf(operationStr);

		if(operation.equals(UPDATE)) {
			validateProductDtoToUpdate(productJsonNode, cache);
		}else {
			validateProductDtoToCreate(productJsonNode, cache);
		}

	}




	private ObjectMapper createObjectMapper() {
		ObjectMapper mapper = new ObjectMapper().configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
		return mapper;
	}




	private void validateProductDtoToCreate(JsonNode productJson, ProductUpdateCache cache){

		checkCreateProuctReqParams(productJson);

		JsonNode brandId = productJson.path("brand_id");
		validateBrandId(brandId, cache);
	}




	private void validateProductDtoToUpdate(JsonNode productJson, ProductUpdateCache cache) {
		JsonNode id = productJson.path("product_id");
		JsonNode brandId = productJson.path("brand_id");

		if(id.isMissingNode()) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, P$PRO$0001);
		}
			
		if(!id.isNull() && !cache.getProducts().containsKey(id.asLong())) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, P$PRO$0002, id.asLong());
		}

		if(!brandId.isMissingNode() ) {
			validateBrandId(brandId, cache);
		}
	}




	private void checkCreateProuctReqParams(JsonNode productJson){
		JsonNode name = productJson.path("name");
		JsonNode brandId = productJson.path("brand_id");

		if(name.isMissingNode()) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, P$PRO$0003);
		}

		if( name.isNull() ) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, P$PRO$0004);
		}

		if(brandId.isMissingNode()) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, P$PRO$0005);
		}
	}






	private void validateBrandId(JsonNode brandId, ProductUpdateCache cache) {
		Long orgId = securityService.getCurrentUserOrganizationId();
		
		if(brandId.isMissingNode() || brandId.isNull()) //brand_id is optional and can be null
		{
			return;
		}			

		long id = brandId.asLong();
		if(!cache.getBrands().containsKey(id)) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, P$BRA$0001 , id);
		}
			
		Long brandOrgId = ofNullable(cache.getBrands())
							.map(map -> map.get(id))
							.map(BrandBasicData::getOrgId)
							.orElse(-2L); 

		if( !Objects.equals(brandOrgId, orgId)) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, P$BRA$0002 , id, orgId);
		}
	}



	public ProductsDeleteResponse deleteProduct(List<Long> productIds) throws BusinessException {

		for(Long productId : productIds) {
			if(!productRepository.existsById(productId)) {
				return new ProductsDeleteResponse(true, singletonList(productId)); //if the product doesn't exists, then..mission accomplished!
			}

			validateProductToDelete(productId);

			try {
				transactions.deleteProduct(productId);
			} catch (DataIntegrityViolationException e) {
				logger.error(e,e);
				throw new BusinessException(format(ERR_PRODUCT_STILL_USED, productId), "INVAILID PARAM:product_id", NOT_ACCEPTABLE);
			} catch (Throwable e) {
				logger.error(e,e);
				throw new BusinessException(format(ERR_PRODUCT_DELETE_FAILED, productId), "INVAILID PARAM:product_id", INTERNAL_SERVER_ERROR);
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
								.collect(joining(","));

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



	@Transactional
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
						format("Product variant with id [%d] doesnot exists!", variantId)
						, "INVALID PARAM:variant_id"
						, NOT_ACCEPTABLE);


			if(variantNotForProduct(variant, productId))
				throw new BusinessException(
						format("Product variant with id [%d] doesnot belong to product with id [%d]!", variantId, productId)
						, "INVALID PARAM:variant_id"
						, NOT_ACCEPTABLE);
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
				.collect(toList());

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
				.collect(toList());

		if(bundleStock.size() != 1) {
			throw new IllegalStateException(
					String.format("Bundle with id[%d] doesn't have a single price!", entity.getId()));
		}

		bundleStock.stream()
				.findFirst()
				.map(StocksEntity::getPrice)
				.ifPresent(dto::setPrice);

		List<Long> productIdList = bundleRepository.getBundleItemsProductIds(entity.getId());
		List<ProductBaseInfo> productlist = emptyList();
		if(!productIdList.isEmpty()) {
			Map<Long, String> 	productCoverImages = imgService.getProductsImagesMap(productIdList, null);
			productlist = productRepository.findByIdInOrderByNameAsc(productIdList)
																.stream()
																.map(prod -> getProductRepresentation(prod, productCoverImages))
																.map(this::toProductBaseInfo)
																.collect(toList());
		}
		
		dto.setProducts( productlist );

		return dto;
	}



	private ProductBaseInfo toProductBaseInfo(ProductRepresentationObject source) {
		ProductBaseInfo baseInfo = new ProductBaseInfo();
		copyProperties(source, baseInfo);
		/*try {

		} catch (IllegalAccessException | InvocationTargetException e) {
			logger.error(e,e);
			throw new RuntimeException(
					String.format( "Failed to copy data from class of type [%s] to a class of type [%s]"
							, source.getClass().getName()
							, baseInfo.getClass().getName() )
			);
		}*/

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

		boolean areEqual = areEqual(userOrgId, bundleOrgId, itemOrgId);

		if(!areEqual) {
			throw new BusinessException(
					String.format("User who belongs to organization of id[%d] is not allowed "
							+ "to add stock item from organiztion of id[%d] to "
							+ "a bundle from organiztion of id[%d]", userOrgId, itemOrgId, bundleOrgId)
					, "INVALID PARAM:bundle_id/stock_id"
					, FORBIDDEN);
		}
	}


	
	public VariantUpdateResponse updateVariant(VariantUpdateDTO variant) throws BusinessException {
		Long variantId = updateVariantBatch(asList(variant)).stream().findFirst().orElse(-1L);
		return new VariantUpdateResponse(variantId);
	}
	
	
	
	
	public List<Long> updateVariantBatch(List<? extends VariantUpdateDTO> variants) throws BusinessException{
		VariantUpdateCache cache = createVariantUpdateCache(variants);
		
		validateVariants(variants, cache);

		List<ProductVariantsEntity> entities = saveVariantsToDb(variants, cache);
		return entities
				.stream()
				.map(ProductVariantsEntity::getId)
				.collect(toList());
	}




	private VariantUpdateCache createVariantUpdateCache(List<? extends VariantUpdateDTO> variants) {
		Long orgId = securityService.getCurrentUserOrganizationId();
		
		List<VariantIdentifier> variantIdentifiers = createVariantIdentifierList(variants);
		VariantCache variantCache = cachingHelper.createVariantCache(variantIdentifiers);		
		Set<ProductFeaturesEntity> orgFeatures = getOrgProductFeatures(orgId);
		Map<String, ExtraAttributesEntity> orgExtraAttributes = createExtraAttributeCache(orgId, variants);
		Map<Long, ProductVariantsEntity> variantsEntities = getVariatsEntities(variants);
		Set<Long> orgProductIds = productRepository.listProductIdByOrganizationId(orgId);
		
		return new VariantUpdateCache(variantCache, orgProductIds, orgFeatures, orgExtraAttributes, variantsEntities);
	}






	private Map<String, ExtraAttributesEntity> createExtraAttributeCache(Long orgId, List<? extends VariantUpdateDTO> variants) {
		Map<String, ExtraAttributesEntity> orgExtraAttributes = getOrganizationExtraAttr(orgId);		
		createAndAddNewExtraAttributes(variants, orgExtraAttributes);		
		return orgExtraAttributes;
	}




	private void createAndAddNewExtraAttributes(List<? extends VariantUpdateDTO> variants,
			Map<String, ExtraAttributesEntity> orgExtraAttributes) {
		variants
		.stream()
		.map(VariantUpdateDTO::getExtraAttr)
		.filter(Objects::nonNull)
		.map(JSONObject::new)
		.map(JSONObject::keySet)
		.flatMap(Set::stream)
		.distinct()
		.filter(extraAttrName -> !orgExtraAttributes.containsKey(extraAttrName))
		.map(this::createNewExtraAttribute)
		.forEach(entity -> orgExtraAttributes.put(entity.getName(), entity));
	}

	
	
	


	private Map<Long, ProductVariantsEntity> getVariatsEntities(List<? extends VariantUpdateDTO> variants) {
		Set<Long> variantIds = 
				variants
				.stream()
				.map(VariantUpdateDTO::getVariantId)
				.distinct()
				.collect(toSet());
		return mapInBatches(variantIds, 500, productVariantsRepository::findByIdIn)
				.stream()
				.collect(toMap(ProductVariantsEntity::getId, variant -> variant));
	}




	private Map<String, ExtraAttributesEntity> getOrganizationExtraAttr(Long orgId) {
		return extraAttrRepo
				.findByOrganizationId(orgId)
				.stream()
				.distinct()
				.collect(toMap(ExtraAttributesEntity::getName, attr -> attr));
	}




	private Set<ProductFeaturesEntity> getOrgProductFeatures(Long orgId) {
		return productFeaturesRepository
				.findByOrganizationId(orgId)
				.stream()
				.distinct()
				.collect(toSet());
	}




	private List<VariantIdentifier> createVariantIdentifierList(List<? extends VariantUpdateDTO> variants) {
		return variants
				.stream()
				.map(VariantUpdateDTO::getVariantId)
				.filter(Objects::nonNull)
				.map(this::createVariantIdentifier)
				.collect(toList());
	}

	
	
	
	private VariantIdentifier createVariantIdentifier(Long id) {
		VariantIdentifier variantIdentifier = new VariantIdentifier();
		variantIdentifier.setVariantId(String.valueOf(id));
		return variantIdentifier;
	}



	private void validateVariants(List<? extends VariantUpdateDTO> variants, VariantUpdateCache cache) throws BusinessException {
		for(VariantUpdateDTO variant: variants) {
			validateVariant(variant, cache);
		}
	}




	private List<ProductVariantsEntity> saveVariantsToDb(List<? extends VariantUpdateDTO> variants, VariantUpdateCache cache) {
		List<ProductVariantsEntity> entities = 
				variants
				.stream()
				.map(variant -> createVariantEntity(variant, cache))
				.collect(toList());
		return productVariantsRepository.saveAll(entities);
	}




	private void validateVariant(VariantUpdateDTO variant ,VariantUpdateCache cache) throws BusinessException {
		Set<Long> orgProductIds = cache.getOrgProductIdList();
		
		if(!variant.areRequiredAlwaysPropertiesPresent()) {
			throw new BusinessException(
					"Missing required parameters !"
					, "MISSING PARAM"
					, NOT_ACCEPTABLE);
		}

		Long orgId = securityService.getCurrentUserOrganizationId();
		if( !orgProductIds.contains( variant.getProductId() )) {
			throw new BusinessException(
					format("Invalid parameters [product_id], no product exists with id[%d] in organization[%d]!", variant.getProductId(), orgId)
					, "INVALID PARAM:features"
					, NOT_ACCEPTABLE);
		}

		Operation opr = variant.getOperation();
		validateOperation(opr);

		if( opr.equals(CREATE) ) {
			validateVariantForCreate(variant, cache);
		}else if( opr.equals(UPDATE) ) {
			validateVariantForUpdate(variant, cache);
		}
	}




	private void validateVariantForUpdate(VariantUpdateDTO variant, VariantUpdateCache cache) throws BusinessException {
		if(!variant.areRequiredForUpdatePropertiesProvided()) {
			throw new BusinessException(
					"Missing required parameters !"
					, "MISSING PARAM"
					, NOT_ACCEPTABLE);
		}

		validateVariantExists(variant, cache);
		validateUserCanUpdateVariant(variant, cache);
		if(variant.isUpdated("features") ) {
			validateFeatures(variant, cache);
		}
		
	}




	private void validateUserCanUpdateVariant(VariantUpdateDTO variant, VariantUpdateCache cache) throws BusinessException {
		String id = ofNullable(variant.getVariantId())
					.map(varId -> String.valueOf(varId))
					.orElse("");
		
		Long userOrgId = securityService.getCurrentUserOrganizationId();
		Long variantOrgId =	getVariantOrganizationId(id, cache);

		if(!Objects.equals(variantOrgId, userOrgId)) {
			throw new BusinessException(
					format("Product variant of id[%s], can't be changed a user from organization with id[%d]!", id , userOrgId)
					, "INVALID PARAM:variant_id"
					, FORBIDDEN);
		}
	}




	private Long getVariantOrganizationId(String id, VariantUpdateCache cache) throws BusinessException {
		return ofNullable(cache)
				.map(VariantUpdateCache::getVariantCache)
				.map(VariantCache::getIdToVariantMap)
				.map(map -> map.get(id))
				.map(VariantBasicData::getOrganizationId)
				.orElseThrow(
						() -> new BusinessException(
								format("Product variant of id[%s], Doesn't follow any organization!", id)
								, "INTERNAL SERVER ERROR"
								, INTERNAL_SERVER_ERROR)
				);
	}




	private void validateVariantExists(VariantUpdateDTO variant, VariantUpdateCache cache) throws BusinessException {
		String id = ofNullable(variant.getVariantId())
					.map(varId -> String.valueOf(varId))
					.orElse("");
		
		if( !cache.getVariantCache().getIdToVariantMap().containsKey(id)) {
			throw new BusinessException(
					format("Invalid parameters [variant_id], no product variant exists with id [%s]!", id)
					, "INVALID PARAM:variant_id"
					, NOT_ACCEPTABLE);
		}
	}




	private void validateVariantForCreate(VariantUpdateDTO variant, VariantUpdateCache cache) throws BusinessException {
		if(!variant.areRequiredForCreatePropertiesProvided()) {
			throw new BusinessException(
					"Missing required parameters !"
					, "MISSING PARAM"
					, NOT_ACCEPTABLE);
		}

		validateFeatures(variant, cache);
	}




	private void validateFeatures(VariantUpdateDTO variant, VariantUpdateCache cache) throws BusinessException {
		Long userOrgId = securityService.getCurrentUserOrganizationId();
		String features = variant.getFeatures();
		if(isBlankOrNull( features )) {
			throw new BusinessException(
					"Invalid parameters [features], the product variant features can't be null nor Empty!"
					, "INVALID PARAM:features"
					, NOT_ACCEPTABLE);
		}

		if(!isJSONValid( features )) {
			throw new BusinessException(
					format("Invalid parameters [features], the product variant features should be a valid json string! The given value was [%s]" ,features )
					, "INVALID PARAM:features"
					, NOT_ACCEPTABLE);
		}
		
		if(hasInvalidFeatureKeys(features, cache.getOrganziationFeatures())) {
			throw new BusinessException(
					format("Invalid parameter [features], a feature key doesnot exists or doesn't belong to organization with id[%d]" ,userOrgId )
					, "INVALID PARAM:features"
					, NOT_ACCEPTABLE);
		}
	}




	private boolean hasInvalidFeatureKeys(String features, Set<ProductFeaturesEntity> orgFeatures) {
		Set<Integer> productFeatureIds = orgFeatures.stream().map(ProductFeaturesEntity::getId).collect(toSet());
		JSONObject featuresJson = new JSONObject(features);
		return featuresJson
				.keySet()
				.stream()
				.map(Integer::valueOf)
				.anyMatch(id -> !productFeatureIds.contains(id));
	}

	
	



	private ProductVariantsEntity createVariantEntity(VariantUpdateDTO variant, VariantUpdateCache cache) {
		ProductVariantsEntity entity = new ProductVariantsEntity();

		Operation opr = variant.getOperation();
		Long id = variant.getVariantId();
		
		if( opr.equals( UPDATE)) {
			entity = ofNullable(cache.getVariantsEntities())
					.map( entities -> entities.get(id))
					.orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE , P$VAR$0001, id.toString()) );
		}

		if(variant.isUpdated("productId")){
			ProductEntity product = new ProductEntity();
			product.setId(variant.getProductId());
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
		
		if(variant.isUpdated("sku")) {
			entity.setSku(variant.getSku());
		}
		
		if(variant.isUpdated("productCode")) {
			entity.setProductCode(variant.getProductCode());
		}

		String pname = getPname(variant, opr);
		if(!isBlankOrNull(pname)) {
			entity.setPname(pname);
		}

		if(!isBlankOrNull(variant.getExtraAttr())) {
			saveExtraAttributesIntoEntity(variant, entity, cache);
		}

		return entity;
	}




	private void saveExtraAttributesIntoEntity(VariantUpdateDTO variant, ProductVariantsEntity entity, VariantUpdateCache cache) {
		try {
			JSONObject  extraAttrJson = new JSONObject(variant.getExtraAttr());

			extraAttrJson
			.keySet()
			.stream()
			.map(attrName -> createVariantExtraAttribute(attrName, extraAttrJson.getString(attrName), cache))
			.forEach(entity::addExtraAttribute);
		}catch(Throwable t) {
			throw new RuntimeBusinessException(
					ERR_INVALID_EXTRA_ATTR_STRING
					, "INVLAID: extra_attr"
					, NOT_ACCEPTABLE);
		}
	}




	private ProductExtraAttributesEntity createVariantExtraAttribute(String name, String value, VariantUpdateCache cache) {
		ExtraAttributesEntity extraAttrEntity = getExtraAttribute(name, cache);

		ProductExtraAttributesEntity variantExtraAttr = new ProductExtraAttributesEntity();
		variantExtraAttr.setExtraAttribute(extraAttrEntity);
		variantExtraAttr.setValue(value);

		return variantExtraAttr;
	}



	private ExtraAttributesEntity getExtraAttribute(String name, VariantUpdateCache cache) {
		Long orgId = securityService.getCurrentUserOrganizationId();
		return ofNullable(cache.getOrgExtraAttributes().get(name))
				.orElseThrow(() -> new RuntimeBusinessException(INTERNAL_SERVER_ERROR, P$VAR$0002, name, orgId));
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
					, NOT_ACCEPTABLE);
		}

		if(!opr.equals(CREATE) &&
				!opr.equals(UPDATE)) {
			throw new BusinessException(
					format("Invalid parameters [operation], unsupported operation [%s]!", opr.getValue())
					, "INVALID PARAM:operation"
					, NOT_ACCEPTABLE);
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
		productRep.setHas_360_view(product.getSearch360());
	}

	


	private ProductRepresentationObject getProductRepresentation(ProductEntity product, Map<Long, String> productCoverImgs) {
		ProductRepresentationObject rep = getProductRepresentation(product);
		setAdditionalInfo(rep, productCoverImgs);
		return rep;
	}




	private ProductDetailsDTO toProductDetailsDTO(ProductEntity product) throws IllegalAccessException, InvocationTargetException {
		ProductDetailsDTO dto = new ProductDetailsDTO();
		ProductRepresentationObject representationObj = getProductRepresentation(product);
		copyProperties(representationObj, dto);
		dto.setDescription( product.getDescription() );
		dto.setProductType( product.getProductType() );
		dto.setImageUrl(imgService.getProductCoverImage( product.getId() ));

		return dto;
	}




	public boolean updateProductTags(ProductTagDTO productTagDTO) throws BusinessException {
		validateProductTagDTO(productTagDTO.getProductIds(), productTagDTO.getTagIds());

		List<Long> productIds = productTagDTO.getProductIds();
		List<Long> tagIds = productTagDTO.getTagIds();
		
		Set<ProductTagPair> newProductTags = createProductTagPairs(productIds, tagIds);
		
		addTagsToProducts(newProductTags);
		
		return true;
	}



	private Set<ProductTagPair> createProductTagPairs(List<Long> productIds, List<Long> tagIds) {
		return productIds
				.parallelStream()
				.flatMap(id -> getProductTagPairs(id, tagIds))
				.distinct()				
				.collect(toSet());
	}



	public void addTagsToProducts(Set<ProductTagPair> newProductTags) {
		Set<Long> prodIds = getProductIds(newProductTags);
		Set<Long> tgIds = getTagIds(newProductTags);
		
		validateProductIdsExists(prodIds);
		validateTagIdsExists(tgIds);

		Set<ProductTagPair> existingProductTags = getExistingProductTags(prodIds);
		
		batchInsertProductTagsToDB(newProductTags, existingProductTags);
	}


	
	

	private void batchInsertProductTagsToDB(Set<ProductTagPair> newProductTags, Set<ProductTagPair> existingProductTags) {
		
		Set<ProductTagPair> validProductTags =
			ofNullable(newProductTags)
			.orElse(emptySet())
			.stream()
			.filter(pair -> !existingProductTags.contains(pair))
			.filter(this::isValidProducTagPair)
			.collect(toSet());
		
		productsCustomRepo.batchInsertProductTags(validProductTags);
	}



	
	
	
	
	
	private boolean isValidProducTagPair(ProductTagPair pair) {
		return noneIsNull(pair, pair.getProductId(), pair.getTagId());
	}



	
	


	private Set<Long> getProductIds(Set<ProductTagPair> newProductTags) {
		return newProductTags
				.parallelStream()
				.map(ProductTagPair::getProductId)
				.collect(toSet());
	}



	private Set<Long> getTagIds(Set<ProductTagPair> newProductTags) {
		return newProductTags
				.parallelStream()
				.map(ProductTagPair::getTagId)
				.collect(toSet());
	}



	private Set<ProductTagPair> getExistingProductTags(Set<Long> prodIds) {
		return ofNullable(prodIds)
				.filter(EntityUtils::noneIsEmpty)
				.map(ids -> mapInBatches(ids, 500, orgTagRepo::getTagsByProductIdIn))
				.orElse(emptyList())
				.parallelStream()
				.map(this::toProductTagPair)
				.collect(toSet());
	}

	
	
	
	private ProductTagPair toProductTagPair(ProductTagsBasicData basicData) {
		return new ProductTagPair(basicData.getProductId(), basicData.getTagId());
	}
	
	
	
	
	private Stream<ProductTagPair> getProductTagPairs(Long productId, List<Long> tagIds){
		return ofNullable(tagIds)
				.orElse(emptyList())
				.parallelStream()
				.map(tagId -> new ProductTagPair(productId, tagId));
	}



	private void validateTagIdsExists(Set<Long> tagIds) {
		Long orgId = securityService.getCurrentUserOrganizationId();
		Collection<Long> batch = mapInBatches(tagIds, 500, t -> orgTagRepo.getExistingTagIds(tagIds, orgId));
		Set<Long> existingIds = new HashSet<>(batch);
		tagIds
		.stream()
		.filter(id -> !existingIds.contains(id))
		.findFirst()
		.ifPresent(nonExistingId -> 
					{throw new RuntimeBusinessException(
								format("Provided tag(%d) does't match any tag", nonExistingId)
								, "INVALID PARAM:product_id"
								, NOT_ACCEPTABLE);});
	}



	private void validateProductIdsExists(Set<Long> productIds) {
		Long orgId = securityService.getCurrentUserOrganizationId();
		Collection<Long> batch =  mapInBatches(productIds, 500, p -> productRepository.getExistingProductIds(productIds, orgId));
		Set<Long> existingIds = new HashSet<>(batch);
		productIds
		.stream()
		.filter(id -> !existingIds.contains(id))
		.findFirst()
		.ifPresent(nonExistingId -> 
					{throw new RuntimeBusinessException(
								format("Provided product_id(%d) does't match any existing product", nonExistingId)
								, "INVALID PARAM:product_id"
								, NOT_ACCEPTABLE);});
	}



	public boolean deleteProductTags(List<Long> productIds, List<Long> tagIds) throws BusinessException {
		validateProductTagDTO(productIds, tagIds);

		Map<Long, ProductEntity> productsMap = validateAndGetProductMap(productIds);
		Map<Long, TagsEntity> tagsMap = validateAndGetTagMap(tagIds);

		List<Pair> productTagsList = productRepository.getProductTags(productIds, tagIds);

		for(Long productId : productIds) {
			for(Long tagId : tagIds) {
				if( productTagsList.contains(new Pair(productId, tagId)))
					productsMap.get(productId).removeProductTag(tagsMap.get(tagId));
				else
					throw new BusinessException("INVALID PARAM", "Link between product "+ productId +" and tag "+ tagId +" doesn't exist!", HttpStatus.NOT_ACCEPTABLE);
			}
			productRepository.save(productsMap.get(productId));
		}
		return true;
	}


	private void validateProductTagDTO(List<Long> productIds, List<Long> tagIds) throws BusinessException {
		if(isBlankOrNull(productIds))
			throw new BusinessException("Provided products_ids can't be empty", "MISSING PARAM:products_ids", NOT_ACCEPTABLE);

		if(isBlankOrNull(tagIds))
			throw new BusinessException("Provided tags_ids can't be empty", "MISSING PARAM:tags_ids", NOT_ACCEPTABLE);
	}






	private Map<Long, ProductEntity> validateAndGetProductMap(List<Long> productIds) throws BusinessException {

		Map<Long, ProductEntity> productsMap = 
				mapInBatches(productIds, 500, productRepository::findByIdIn)
				.stream()
				.collect(toMap(ProductEntity::getId, entity -> entity));

		for(Long productId : productIds) {
			if (productsMap.get(productId) == null)
				throw new BusinessException(
						format("Provided product_id(%d) does't match any existing product", productId)
						, "INVALID PARAM:product_id"
						, NOT_ACCEPTABLE);
		}

		return productsMap;
	}






	private Map<Long, TagsEntity> validateAndGetTagMap(List<Long> tagIds) throws BusinessException {
		Long orgId = securityService.getCurrentUserOrganizationId();

		Map<Long, TagsEntity> tagsMap =
				mapInBatches(tagIds, 500, tgs -> orgTagRepo.findByIdInAndOrganizationEntity_Id(tgs, orgId))
				.stream()
				.collect(toMap(TagsEntity::getId, entity -> entity));

		for(Long tagId : tagIds) {
			if (tagsMap.get(tagId) == null)
				throw new BusinessException(
						format("Provided tag_id(%d) doesn't match any existing tag for organization(%d)", tagId, orgId)
						,"INVALID PARAM:tag_id", NOT_ACCEPTABLE);
		}
		return tagsMap;
	}




	public void deleteAllProducts(boolean isConfirmed) throws BusinessException {
		if(!isConfirmed) {
			throw new BusinessException("Unconfirmed Delete operation for all products!" , "UNCONFIRMED OPERATION", NOT_ACCEPTABLE);
		}
		Long orgId = securityService.getCurrentUserOrganizationId();
		
		basketRepo.deleteByOrganizationIdAndStatus( NEW.getValue(), orgId);
		ordersRepository.deleteByStatusAndOrgId( NEW.getValue(), orgId);
		productVariantsRepository.deleteAllByProductEntity_organizationId(orgId);
		productRepository.deleteAllByOrganizationId(orgId);
	}



	public void hideProducts(Boolean hide, List<Long> productsIds) {
		Long orgId = securityService.getCurrentUserOrganizationId();
		List<Long> productIdsList;
		if (isBlankOrNull(productsIds)) {
			productIdsList = productImgsCustomRepo.getProductsWithNoImages(orgId)
					.stream()
					.map(p -> p.getProductId())
					.collect(toList());
		} else {
			productIdsList = productsIds;
		}

		if (!(productIdsList == null || productIdsList.isEmpty())) {
			divideToBatches(productIdsList, 500)
				.forEach(batch -> productRepository.setProductsHidden(batch, hide, orgId));
		}
	}


	private void validateProduct360ShopsDTO(Product360ShopsDTO dto) {
		if (isNullOrEmpty(dto.getProductIds())){
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, P$PRO$0001);
		}
		if (isNullOrEmpty(dto.getShopIds())){
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, S$0006);
		}
		if (isBlankOrNull(dto.getInclude())){
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, GEN$0002, "include");
		}
	}



	public void deleteAllTagsForProducts(List<Long> products) {
		processInBatches(products, 500, productRepository::deleteAllTagsForProducts);
	}


	public void updateCollection(CollectionItemDTO elements) {
		validateCollectionItems(elements);
		updateCollectionItems(elements);
	}


	private void validateCollectionItems(CollectionItemDTO element) {
		if(!(element.getOperation().equals(Operation.DELETE) || element.getOperation().equals( Operation.ADD))) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, P$PRO$0008, "add, delete");
		}
		if (Objects.equals(element.getProductId(), null))
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, P$PRO$0001);
		if (Objects.equals(element.getVariantIds(), null) || element.getVariantIds().isEmpty())
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, P$VAR$003);
	}


	private void updateCollectionItems(CollectionItemDTO element) {
		Long orgId = securityService.getCurrentUserOrganizationId();

		ProductCollectionEntity entity = getCollectionByProductIdAndOrgId(element.getProductId(), orgId);

		List<ProductVariantsEntity> variantsEntities = productVariantsRepository.findByIdInAndProductEntity_OrganizationId(element.getVariantIds(), orgId);

		validateVariantsExistence(variantsEntities, element.getVariantIds());

		if(element.getOperation().equals( Operation.DELETE)) {
			entity.getVariants().removeAll(variantsEntities);
		}else if(element.getOperation().equals( Operation.ADD)){
			entity.getVariants().addAll(variantsEntities);
		}

		productCollectionRepo.save(entity);
	}


	private void validateVariantsExistence(List<ProductVariantsEntity> variantsEntities, List<Long> variantsIds) {
		List<Long> fetchedVariantsIds = variantsEntities.stream()
				.map(ProductVariantsEntity::getId)
				.collect(toList());

		if (fetchedVariantsIds.size() != variantsIds.size()) {
			variantsIds.removeAll(fetchedVariantsIds);
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, P$VAR$0001, variantsIds.toString());
		}
	}


	private ProductCollectionEntity getCollectionByProductIdAndOrgId(Long productId, Long orgId) {
		return productCollectionRepo.findByIdAndOrganizationId(productId, orgId)
								    .orElseThrow(() ->  new RuntimeBusinessException(NOT_ACCEPTABLE, P$PRO$0002, productId));
	}


	private ProductCollectionEntity getCollectionByProductId(Long productId) {
		return productCollectionRepo.findById(productId)
				.orElseThrow(() ->  new RuntimeBusinessException(NOT_ACCEPTABLE, P$PRO$0002, productId));
	}


	public ProductDetailsDTO getCollection(Long id) {
		ProductCollectionEntity entity = getCollectionByProductId(id);
		return toProductDetailsDTO(entity);
	}


	public List<ProductDetailsDTO> getCollections(Long orgId) {
		List<ProductCollectionEntity> collectionsEntities = productCollectionRepo.findByOrganizationId(orgId);

		List<ProductDetailsDTO> collections = collectionsEntities
				.stream()
				.filter(c -> c.getVariants().isEmpty())
				.map(c -> toProductDetailsDTO(c))
				.collect(toList());

		return collections;
	}


	private ProductDetailsDTO toProductDetailsDTO(ProductCollectionEntity entity) {
		ProductDetailsDTO dto = new ProductDetailsDTO();
		copyProperties(entity, dto, new String[] {"variants"});

		dto.setCreationDate(entity.getCreationDate().toString());
		dto.setUpdateDate(entity.getUpdateDate().toString());
		dto.setHas_360_view(entity.getSearch360());
		dto.setImageUrl(imgService.getProductCoverImage(entity.getId()));

		List<TagsRepresentationObject> tagsDTOList = getProductTagsDTOList(entity.getId());
		dto.setTags(tagsDTOList);

		List<ProductImageDTO> collectionImages = imgService.getProductsAndVariantsImages(asList(entity.getId()), null);
		dto.setImages(getProductImages(collectionImages));

		List<Long> collection360Shops = product360ShopsRepo.findShopsByProductId(entity.getId());
		dto.setShops(collection360Shops);

		if(!entity.getVariants().isEmpty()) {
			List<ProductVariantsEntity> variantsList = new ArrayList<>(entity.getVariants());
			List<Long> variantsIds = getVariantsIds(variantsList);

			List<ProductImageDTO> productsAndVariantsImages = imgService.getProductsAndVariantsImages(asList(entity.getId()), variantsIds);

			dto.setVariants(entity.getVariants()
					.stream()
					.map(v -> createVariantDto(null, v, productsAndVariantsImages))
					.collect(toList()));
			dto.setVariantFeatures( getVariantFeatures(variantsList) );
			dto.setImages(getProductImages(productsAndVariantsImages));
		}
		return dto;
	}


	private List<Long> getVariantsIds(List<ProductVariantsEntity> variants) {
		return variants
				.stream()
				.map(ProductVariantsEntity::getId)
				.collect(toList());
	}


	public VariantsResponse getVariants(Long orgId, String name, Integer start, Integer count) {
		if (isBlankOrNull(start) || start < 0) {
			start = 0;
		}

		if (isBlankOrNull(count) || count < 0) {
			count = 10;
		}

		Long total = productVariantsRepository.countByProductEntity_organizationIdAndNameContainingIgnoreCase(orgId, name);
		name = ofNullable(name.toLowerCase()).orElse("");
		List<ProductVariantsEntity> variantsEntities =
				productVariantsRepository.findByOrganizationId(orgId, name, PageRequest.of(start, count));

		List<Long> variantsIds = getVariantsIds(variantsEntities);
		List<ProductImageDTO> productsAndVariantsImages = variantsIds.isEmpty() ? new ArrayList<>() : imgService.getProductsAndVariantsImages(null,variantsIds) ;

		List<VariantDTO> variants =  variantsEntities
				.stream()
				.map(v -> createVariantDto(null, v, productsAndVariantsImages))
				.collect(toList());
		return new VariantsResponse(total, variants);
	}
}




@Data
@AllArgsConstructor
class VariantUpdateCache{
	private VariantCache variantCache;
	private Set<Long> orgProductIdList;
	private Set<ProductFeaturesEntity> organziationFeatures;
	private Map<String, ExtraAttributesEntity> orgExtraAttributes;
	private Map<Long, ProductVariantsEntity> variantsEntities;
}



@Data
@AllArgsConstructor
class ProductUpdateCache{
	private Map<Long, ProductEntity> products;
	private Map<Long, BrandBasicData> brands;
}




@Data
@AllArgsConstructor
class ProductAndBrandPair{
	private Long productId;
	private Long brandId;
}

