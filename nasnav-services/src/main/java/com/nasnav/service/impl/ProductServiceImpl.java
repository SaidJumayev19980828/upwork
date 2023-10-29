package com.nasnav.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.nasnav.commons.enums.SortOrder;
import com.nasnav.commons.utils.EntityUtils;
import com.nasnav.commons.utils.FunctionalUtils;
import com.nasnav.commons.utils.StringUtils;
import com.nasnav.constatnts.EntityConstants.Operation;
import com.nasnav.dao.*;
import com.nasnav.dto.*;
import com.nasnav.dto.request.product.CollectionItemDTO;
import com.nasnav.dto.request.product.Product360ShopsDTO;
import com.nasnav.dto.request.product.RelatedItemsDTO;
import com.nasnav.dto.response.navbox.VariantsResponse;
import com.nasnav.enumerations.ExtraAttributeType;
import com.nasnav.enumerations.ProductFeatureType;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.exceptions.ErrorCodes;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.dto.query.result.ProductRatingData;
import com.nasnav.querydsl.sql.*;
import com.nasnav.persistence.*;
import com.nasnav.persistence.dto.query.result.products.ProductTagsBasicData;
import com.nasnav.request.BundleSearchParam;
import com.nasnav.request.AllowedPromotionConstraints;
import com.nasnav.request.ProductSearchParam;
import com.nasnav.request.VariantSearchParam;
import com.nasnav.response.*;
import com.nasnav.service.*;
import com.nasnav.service.helpers.CachingHelper;
import com.nasnav.service.model.ProductTagPair;
import com.nasnav.service.model.VariantBasicData;
import com.nasnav.service.model.VariantCache;
import com.nasnav.service.model.VariantIdentifier;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.SubQueryExpression;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.sql.SQLExpressions;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.SQLQueryFactory;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import com.nasnav.enumerations.SeoEntityType;
import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.nasnav.commons.utils.CollectionUtils.*;
import static com.nasnav.commons.utils.EntityUtils.*;
import static com.nasnav.commons.utils.PagingUtils.getQueryPage;
import static com.nasnav.commons.utils.StringUtils.*;
import static com.nasnav.constatnts.EntityConstants.Operation.*;
import static com.nasnav.constatnts.error.product.ProductSrvErrorMessages.ERR_PRODUCT_HAS_NO_VARIANTS;
import static com.nasnav.enumerations.ExtraAttributeType.INVISIBLE;
import static com.nasnav.enumerations.ExtraAttributeType.getExtraAttributeType;
import static com.nasnav.enumerations.ProductFeatureType.STRING;
import static com.nasnav.enumerations.Settings.HIDE_EMPTY_STOCKS;
import static com.nasnav.enumerations.Settings.SHOW_FREE_PRODUCTS;
import static com.nasnav.exceptions.ErrorCodes.*;
import static com.nasnav.persistence.ProductTypes.*;
import static com.querydsl.core.types.dsl.Expressions.cases;
import static com.querydsl.core.types.dsl.Expressions.constant;
import static java.lang.String.format;
import static java.math.BigDecimal.ZERO;
import static java.math.BigDecimal.ONE;
import static java.util.Arrays.asList;
import static java.util.Collections.*;
import static java.util.Comparator.comparing;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.*;
import static org.springframework.beans.BeanUtils.copyProperties;
import static org.springframework.http.HttpStatus.*;

@Service
public class ProductServiceImpl implements ProductService {
	private static final Set<ProductSortOptions> REPOSITORY_SORT = Set.of(ProductSortOptions.P_NAME,
			ProductSortOptions.NAME, ProductSortOptions.ID);

	private Logger logger = LogManager.getLogger();

	//	@Value("${products.default.start}")
	private Integer defaultStart = 0;
	//	@Value("${products.default.count}")
	private Integer defaultCount = 10;
	//	@Value("${products.default.sort.attribute}")
	private ProductSortOptions defaultSortAttribute = ProductSortOptions.PRIORITY;
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
	private VariantFeatureValuesRepository variantFeatureValuesRepo;

	@Autowired
	private  FileService fileService;

	@Autowired
	private EmployeeUserRepository empRepo;

	@Autowired
	private BrandsRepository brandRepo;

	@PersistenceContext
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
	private OrganizationRepository orgRepo;
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
	private ProductExtraAttributesEntityRepository productExtraAttributesRepo;

	@Autowired
	private Product360ShopsRepository product360ShopsRepo;

	@Autowired
	private RelatedProductsRepository relatedProductsRepo;
	@Autowired
	private CartItemRepository cartRepo;
	@Autowired
	private ProductRatingRepository productRatingRepo;
	@Autowired
	private OrganizationService orgService;

	@Autowired
	private ProductCollectionItemRepository collectionItemRepo;

	private final PromotionRepository promotionRepository;

	@Autowired
	private SeoService seoService;

	
	@Autowired
	public ProductServiceImpl(ProductRepository productRepository, StockRepository stockRepository,
						  ProductVariantsRepository productVariantsRepository, ProductImagesRepository productImagesRepository,
						  ProductFeaturesRepository productFeaturesRepository , BundleRepository bundleRepository,
						  StockService stockService, PromotionRepository promotionRepository ) {
		this.productRepository = productRepository;
		this.stockRepository = stockRepository;
		this.productImagesRepository = productImagesRepository;
		this.productVariantsRepository = productVariantsRepository;
		this.productFeaturesRepository = productFeaturesRepository;
		this.bundleRepository = bundleRepository;
		this.stockService = stockService;
		this.promotionRepository = promotionRepository;
	}

	@Override
	@Transactional
	public ProductDetailsDTO getProduct(Long productId, Long shopId, boolean includeOutOfStock, boolean checkVariants,
			boolean getOnlyYeshteryProducts) throws BusinessException {
		var params = new ProductFetchDTO(productId);
		params.setShopId(shopId);
		params.setCheckVariants(checkVariants);
		params.setIncludeOutOfStock(includeOutOfStock);
		params.setOnlyYeshteryProducts(getOnlyYeshteryProducts);
		return getProduct(params);
	}



	@Override
	@Transactional
	public ProductDetailsDTO getProduct(ProductFetchDTO productFetchDTO) throws BusinessException{
		var id = ofNullable(productFetchDTO.getProductId()).orElse(-1L);
		var allowAll = !ofNullable(productFetchDTO.getOnlyYeshteryProducts()).orElse(false);
		ProductEntity product =
				productRepository
						.findByProductId(id , allowAll)
						.orElseThrow(() -> new RuntimeBusinessException(NOT_FOUND, P$PRO$0002, productFetchDTO.getProductId()));

		List<ProductVariantsEntity> productVariants = getProductVariants(product, productFetchDTO.isCheckVariants());

		return createProductDetailsDTO(product, productFetchDTO.getShopId(), productVariants, productFetchDTO.isIncludeOutOfStock());
	}





	private ProductDetailsDTO createProductDetailsDTO(ProductEntity product, Long shopId,
													  List<ProductVariantsEntity> productVariants,
													  boolean includeOutOfStock) {
		List<ProductImageDTO> productsAndVariantsImages = getProductImageDTOS(product, productVariants);
		List<VariantDTO> variantsDTOList = createVariantDTOS(shopId, productVariants, productsAndVariantsImages);
		List<TagsRepresentationObject> tagsDTOList = getProductTagsDTOList(product.getId());
		List<Long> product360Shops = product360ShopsRepo.findShopsByProductId(product.getId());

		ProductDetailsDTO productDTO = toProductDetailsDTO(product, includeOutOfStock);
		productDTO.setShops(product360Shops);
		productDTO.setImages(getProductImages(productsAndVariantsImages));
		productDTO.setVariants(variantsDTOList);
		productDTO.setMultipleVariants(hasMultipleVariants(variantsDTOList));
		productDTO.setVariantFeatures(getVariantFeatures(productVariants));
		productDTO.setBundleItems(getBundleItems(product));
		productDTO.setTags(tagsDTOList);

		return productDTO;
	}



	private List<ProductImageDTO> getProductImageDTOS(ProductEntity product, List<ProductVariantsEntity> productVariants) {
		List<Long> variantIds =
				ofNullable(productVariants)
						.orElse(emptyList())
						.stream()
						.map(ProductVariantsEntity::getId)
						.collect(toList());
		variantIds = variantIds.isEmpty()? null: variantIds;
		List<ProductImageDTO> productsAndVariantsImages = imgService.getProductsAndVariantsImages(asList(product.getId()), variantIds);
		return productsAndVariantsImages;
	}



	private List<VariantDTO> createVariantDTOS(Long shopId, List<ProductVariantsEntity> productVariants, List<ProductImageDTO> productsAndVariantsImages) {
		List<VariantDTO> variantsDTOList = new ArrayList<>();
		if (!isNullOrEmpty(productVariants)) {
			variantsDTOList = getVariantsList(productVariants, shopId, productsAndVariantsImages);
		}
		return variantsDTOList;
	}



	private boolean hasMultipleVariants(List<VariantDTO> variantsDTOList) {
		return variantsDTOList != null && variantsDTOList.size() > 1;
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







	private List<ProductVariantsEntity> getProductVariants(ProductEntity product, boolean checkVariants) throws BusinessException {
		List<ProductVariantsEntity> productVariants = productVariantsRepository.findByProductEntity_Id(product.getId());
		if ((productVariants == null || productVariants.isEmpty()) && checkVariants) {
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
		ProductsResponse response = this.getProductsResponseForBundles(bundleProducts,"asc" , "name" ,  (long)bundleProducts.size() );
		List<ProductRepresentationObject> productRepList = response.getProducts();
		return productRepList;
	}


	private List<VariantDTO> getVariantsList(List<ProductVariantsEntity> productVariants, Long shopId,
											 List<ProductImageDTO> variantsImages) {

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
		ExtraAttributesEntity extraAttrEntity = entity.getExtraAttribute();
		ExtraAttributeType type =
				getExtraAttributeType(extraAttrEntity.getType())
						.orElse(ExtraAttributeType.STRING);
		Boolean invisible = Objects.equals(INVISIBLE, type);
		ExtraAttributeDTO dto = new ExtraAttributeDTO();
		dto.setId(extraAttrEntity.getId());
		dto.setIconUrl(extraAttrEntity.getIconUrl());
		dto.setName(extraAttrEntity.getName());
		dto.setType(type);
		dto.setValue(entity.getValue());
		dto.setInvisible(invisible);
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
		return parseVariantFeatures(variant, 1);
	}



	@Override
	public Map<String, String> parseVariantFeatures(ProductVariantsEntity variant, Integer returnedName) {
		return variant
				.getFeatureValues()
				.stream()
				.collect(toMap(f -> returnedName.equals(0) ? f.getFeature().getName() : f.getFeature().getPname(),
								VariantFeatureValueEntity::getValue));
	}



	private List<VariantFeatureDTO> getVariantFeatures(List<ProductVariantsEntity> productVariants) {
		return	ofNullable(productVariants)
						.orElse(emptyList())
						.stream()
						.filter(this::hasFeatures)
						.map(this::extractVariantFeatures)
						.flatMap(List::stream)
						.distinct()
						.collect(toList());
	}




	private List<VariantFeatureDTO> extractVariantFeatures(ProductVariantsEntity variant){
		return variant
				.getFeatureValues()
				.stream()
				.map(VariantFeatureValueEntity::getFeature)
				.map(this::createVariantFeatureDTO)
				.sorted(Comparator.comparing(VariantFeatureDTO::getName))
				.collect(toList());
	}



	private VariantFeatureDTO createVariantFeatureDTO(ProductFeaturesEntity entity) {
		ProductFeatureType type =
				ofNullable(entity.getType())
				.flatMap(ProductFeatureType::getProductFeatureType)
				.orElse(STRING);
		Map<String,?> extraData = emptyMap();
		try{
			extraData = ofNullable(entity.getExtraData())
					.map(JSONObject::new)
					.map(JSONObject::toMap)
					.orElse(emptyMap());
		}catch(Throwable e){
			logger.error(e,e);
		}
		VariantFeatureDTO dto = new VariantFeatureDTO();
		dto.setName(entity.getName());
		dto.setLabel(entity.getPname());
		dto.setType(type.name());
		dto.setExtraData(extraData);
		return dto;
	}


	private boolean hasFeatures(ProductVariantsEntity variant) {
		return !variant.getFeatureValues().isEmpty();
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
				.filter(Objects::nonNull)
				.filter(stock -> Objects.equals(stock.getShopsEntity().getRemoved(), 0))
				.map(StockDTO::new)
				.collect(toList());
	}

	@Override
	@Transactional
	public ProductsResponse getProducts(ProductSearchParam requestParams) throws BusinessException {
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

		return getProductResponseFromStocks(result, productsCount, params.include_out_of_stock.booleanValue());
	}





	private SQLQuery<?> getProductsQuery(ProductSearchParam params, boolean count) {
		QStocks stock = QStocks.stocks;
		QProducts product = QProducts.products;
		QProductVariants variant = QProductVariants.productVariants;
		QShops shop = QShops.shops;
		QOrganizations organization = QOrganizations.organizations;

		BooleanBuilder predicate = getQueryPredicate(params, product, stock, shop, variant, organization);

		BooleanBuilder predicateForPromotions = getQueryPredicateForPromotions(product, params);
		predicate.and(predicateForPromotions);
		BooleanBuilder predicateForDiscounts = getQueryPredicateForDiscounts(stock, organization, shop, params);
		predicate.and(predicateForDiscounts);
		List<OrderSpecifier> order = getProductQueryOrder(params, product, stock);

		SQLQuery<?> fromProductsClause = productsCustomRepo.getProductsBaseQuery(predicate, params);

		SQLQuery<?> fromCollectionsClause = productsCustomRepo.getCollectionsBaseQuery(predicate, params);

		SubQueryExpression productsQuery = getProductsQuery(stock, product, variant, fromProductsClause);

		SubQueryExpression collectionsQuery = getProductsQuery(stock, product, variant, fromCollectionsClause);

		SQLQuery<?> subQuery = new SQLQuery();

		SQLQuery<?> stocks =
				queryFactory
						.from(subQuery.union(productsQuery,collectionsQuery).as("total_products"))
						.where(Expressions.numberPath(Long.class, "row_num").eq(1L));

		if (!order.isEmpty() && !count) {
			stocks.orderBy(order.get(0));
			if (order.size() > 1)
				stocks.orderBy(order.get(1));
		}


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
				variant.productCode,
				variant.sku,
				product.id,
				product.barcode,
				product.brandId.as("brand_id"),
				product.categoryId.as("category_id"),
				product.description.as("description"),
				product.name.as("name"),
				product.pName.as("pname"),
				product.description.as("description"),
				product.search360.as("has_360_view"),
				product.createdAt.as("creation_date"),
				product.updatedAt.as("update_date"),
				product.productType,
				product.priority,
				product.organizationId,
				SQLExpressions.rowNumber()
						.over()
						.partitionBy(product.id).orderBy(cases()
								.when(stock.quantity.gt(ZERO))
								.then(ONE)
								.otherwise(ZERO).desc())
						.orderBy(stock.price).as("row_num"));

		return productsQuery;
	}


	@Override
	public ProductsFiltersResponse getProductAvailableFilters(ProductSearchParam param) throws BusinessException {
		ProductSearchParam finalParams = getProductSearchParams(param);

		QStocks stock = QStocks.stocks;
		QProducts product = QProducts.products;
		QShops shop = QShops.shops;
		QProductVariants variant = QProductVariants.productVariants;
		QOrganizations organization = QOrganizations.organizations;

		BooleanBuilder predicate = getQueryPredicate(finalParams, product, stock, shop, variant, organization);
		BooleanBuilder predicateForPromotions = getQueryPredicateForPromotions(product, finalParams);
		predicate.and(predicateForPromotions);
		BooleanBuilder queryPredicateForDiscounts = getQueryPredicateForDiscounts(stock, organization, shop, finalParams);
		predicate.and(queryPredicateForDiscounts);

		SQLQuery<?> fromProductsClause = productsCustomRepo.getProductsBaseQuery(predicate, finalParams);
		SQLQuery<?> fromCollectionsClause = productsCustomRepo.getCollectionsBaseQuery(predicate, finalParams);

		Prices prices = getProductPrices(fromProductsClause, fromCollectionsClause);

		List<Organization_BrandRepresentationObject> brands = getProductBrands(fromProductsClause, fromCollectionsClause);

		List<TagsRepresentationObject> tags;
		if (param.getTags_org_id() == null) {
			tags = getProductTags(fromProductsClause, fromCollectionsClause);
		} else {
			tags = getProductTagsOfSpecificOrg(fromProductsClause, fromCollectionsClause, param.getTags_org_id());
		}

		Map<String, List<String>> variantsFeatures = getProductVariantFeatures(fromProductsClause, fromCollectionsClause );

		ProductsFiltersResponse response = new ProductsFiltersResponse(prices, brands, tags, variantsFeatures);

		return response;
	}


	private Prices getProductPrices(SQLQuery<?> fromProductsClause, SQLQuery<?> fromCollectionsClause) {
		QStocks stock = QStocks.stocks;
		SubQueryExpression products = fromProductsClause.select(stock.price.min().as("minPrice"), stock.price.max().as("maxPrice"));
		SubQueryExpression collections = fromCollectionsClause.select(stock.price.min().as("minPrice"), stock.price.max().as("maxPrice"));

		SQLQuery<?> sqlQuery = new SQLQuery<>();
		SQLQuery<?> query = queryFactory
				.select(SQLExpressions.min(Expressions.numberPath(BigDecimal.class, "minPrice")).as("minPrice"),
						SQLExpressions.min(Expressions.numberPath(BigDecimal.class, "maxPrice")).as("maxPrice"))
				.from(sqlQuery.union(products, collections).as("total"));

		return template.queryForObject(query.getSQL().getSQL() , new BeanPropertyRowMapper<>(Prices.class));
	}



	private List<Organization_BrandRepresentationObject> getProductBrands(SQLQuery<?> fromProductsClause, SQLQuery<?> fromCollectionsClause) {
		QBrands brand = QBrands.brands;
		QProducts product = QProducts.products;
		QOrganizations organization = QOrganizations.organizations;

		SubQueryExpression products = queryFactory
				.select(brand.id, brand.bannerImage.as("bannerImage"),brand.name, brand.priority, organization.name.as("orgName"), brand.logo.as("logoUrl"))
				.from(brand)
				.leftJoin(organization).on(brand.organizationId.eq(organization.id))
				.where(brand.id.in(fromProductsClause.select(product.brandId)));
		SubQueryExpression collections = queryFactory
				.select(brand.id, brand.bannerImage.as("bannerImage"), brand.name, brand.priority, organization.name.as("orgName"), brand.logo.as("logoUrl"))
				.from(brand)
				.leftJoin(organization).on(brand.organizationId.eq(organization.id))
				.where(brand.id.in(fromCollectionsClause.select(product.brandId)));

		SQLQuery<?> sqlQuery = new SQLQuery<>();
		SQLQuery<?> query = queryFactory
				.select(Expressions.numberPath(Long.class, "id"),
						Expressions.stringPath("name"),
						Expressions.numberPath(Integer.class, "priority"),
						Expressions.stringPath("orgName"),
						Expressions.stringPath("logoUrl"),
						Expressions.stringPath("bannerImage"))
				.from(sqlQuery.union(products, collections).as("total"))
				.orderBy(Expressions.numberPath(Integer.class, "priority").desc());

		return template.query(query.getSQL().getSQL(),
				new BeanPropertyRowMapper<>(Organization_BrandRepresentationObject.class));
	}


	private List<TagsRepresentationObject> getProductTags(SQLQuery<?> fromProductsClause, SQLQuery<?> fromCollectionsClause) {
		QTags tag = QTags.tags;
		QProducts product = QProducts.products;
		QProductTags productTag = QProductTags.productTags;

		SQLQuery<?> sqlQuery = new SQLQuery<>();
		SubQueryExpression union = sqlQuery.union(fromProductsClause.select(product.id),
				fromCollectionsClause.select(product.id));
		SQLQuery<?> tags = queryFactory
				.select(tag.id, tag.name, tag.alias, tag.metadata, tag.pName.as("pname"), tag.categoryId)
				.from(tag)
				.where(tag.id.in(queryFactory
						.select(productTag.tagId)
						.from(productTag)
						.where(productTag.productId.in(union))));

		return template.query(tags.getSQL().getSQL(),
				new BeanPropertyRowMapper<>(TagsRepresentationObject.class));

	}

	private List<TagsRepresentationObject> getProductTagsOfSpecificOrg(SQLQuery<?> fromProductsClause, SQLQuery<?> fromCollectionsClause, Long orgId) {
		QTags tag = QTags.tags;
		QProducts product = QProducts.products;
		QProductTags productTag = QProductTags.productTags;

		SQLQuery<?> sqlQuery = new SQLQuery<>();
		SubQueryExpression union = sqlQuery.union(fromProductsClause.select(product.id), fromCollectionsClause.select(product.id));
		SQLQuery<?> categoriesQuery = queryFactory
				.select(tag.categoryId)
				.from(tag)
				.where(tag.id.in(queryFactory
						.select(productTag.tagId)
						.from(productTag)
						.where(productTag.productId.in(union))));

		Set<Long> categories = template
				.queryForList(categoriesQuery.getSQL().getSQL(), Long.class)
				.stream()
				.filter(Objects::nonNull)
				.collect(toSet());

		SQLQuery<?> tags = queryFactory
				.select(tag.id, tag.name, tag.alias, tag.metadata, tag.pName.as("pname"), tag.categoryId)
				.from(tag)
				.where(tag.categoryId.in(categories).and(tag.organizationId.eq(orgId)));

		return template.query(tags.getSQL().getSQL(),
				new BeanPropertyRowMapper<>(TagsRepresentationObject.class));

	}


	private Map<String, List<String>> getProductVariantFeatures(SQLQuery<?> fromProductsClause, SQLQuery<?> fromCollectionsClause) {
		QProductVariants variant = QProductVariants.productVariants;
		QProductFeatures feature = QProductFeatures.productFeatures;
		QVariantFeatureValues featureValue = QVariantFeatureValues.variantFeatureValues;

		SQLQuery baseQuery = new SQLQuery();
		baseQuery.union(fromProductsClause.select(variant.id), fromCollectionsClause.select(variant.id)).as("total");

		SQLQuery query = queryFactory
				.selectDistinct(feature.name, featureValue.value)
				.from(featureValue)
				.join(feature).on(feature.id.eq(featureValue.featureId))
				.where(featureValue.variantId.in(baseQuery));

		var variantsList =
				template.query(query.getSQL().getSQL(),
						new BeanPropertyRowMapper<>(com.nasnav.dto.response.navbox.VariantFeatureDTO.class));

		return variantsList
				.stream()
				.collect(
						groupingBy(com.nasnav.dto.response.navbox.VariantFeatureDTO::getName
								, mapping(d -> d.getValue(), toList())));
	}


	private List<OrderSpecifier> getProductQueryOrder(ProductSearchParam params, QProducts product, QStocks stock) {
		List<OrderSpecifier> orderBy = new ArrayList<>();
		if (params.getOrder().equals(SortOrder.DESC))
			switch (params.getSort()) {
				case ID : orderBy.add(product.id.as("id").desc());
				case NAME: orderBy.add( product.name.as("name").desc());
				case P_NAME: orderBy.add( product.pName.as("pname").desc());
				case CREATION_DATE: orderBy.add( product.createdAt.as("creation_date").desc());
				case UPDATE_DATE: orderBy.add( product.updatedAt.as("update_date").desc());
				case PRICE: orderBy.add( stock.price.as("price").desc());
				case PRIORITY: orderBy.add( product.priority.as("priority").desc());
					orderBy.add( product.updatedAt.as("update_date").desc());
			}
		else if (params.getOrder().equals(SortOrder.ASC))
			switch (params.getSort()) {
				case ID : orderBy.add( product.id.as("id").asc());
				case NAME: orderBy.add( product.name.as("name").asc());
				case P_NAME: orderBy.add( product.pName.as("pname").asc());
				case CREATION_DATE: orderBy.add( product.createdAt.as("creation_date").asc());
				case UPDATE_DATE: orderBy.add( product.updatedAt.as("update_date").asc());
				case PRICE: orderBy.add( stock.price.as("price").asc());
				case PRIORITY: orderBy.add( product.priority.as("priority").asc());
					orderBy.add( product.updatedAt.as("update_date").desc());
			}
		return orderBy;
	}




	private BooleanBuilder getQueryPredicate(ProductSearchParam params, QProducts product
			, QStocks stock, QShops shop, QProductVariants variant, QOrganizations organization) {
		BooleanBuilder predicate = new BooleanBuilder();

		predicate.and(product.removed.eq(0));
		predicate.and(product.hide.eq(false));
		predicate.and( shop.removed.eq(0) );

		if (params.yeshtery_products) {
			predicate.and(organization.yeshteryState.eq(1));
		}
		if (params.org_id != null) {
			predicate.and(product.organizationId.eq((params.org_id)));
		}
		if (params.shop_id != null)  {
			predicate.and( stock.shopId.eq(params.shop_id) );
		}

		if(params.brand_id != null)
			predicate.and( product.brandId.eq(params.brand_id) );

		if(params.category_id != null)
			predicate.and( product.categoryId.eq(params.category_id) );

		if(params.minPrice != null)
			predicate.and( stock.price.goe(params.minPrice));

		if(params.maxPrice != null)
			predicate.and( stock.price.loe(params.maxPrice));

		if(params.category_name != null)
			predicate.and( product.id.in(productsCustomRepo.getProductTagsByCategoryNameQuery(params)));

		if(params.category_ids != null && !params.category_ids.isEmpty()) {
			predicate.and( product.id.in(productsCustomRepo.getProductTagsByCategories(params)));
		}

		if(params.name != null)
			predicate.and( product.name.lower().like("%" + params.name.toLowerCase() + "%")
					.or(product.id.like("%" + params.name.toLowerCase() + "%"))
					.or(product.description.lower().like( "% " + params.getName().toLowerCase() + " %"))
					.or(product.description.lower().like( params.getName().toLowerCase() + " %"))
					.or(product.description.lower().like( "% " + params.getName().toLowerCase()))
					.or(variant.productCode.like("%" + params.name + "%") )
					.or(variant.sku.like("%" + params.name + "%") )
					.or(variant.barcode.like("%" + params.name + "%") )
					.or(product.id.in(productsCustomRepo.getProductTagsByNameQuery(params)))
			);


		if(params.product_type != null)
			predicate.and( product.productType.in(params.product_type));

		if(params.hide_empty_stocks) {
			predicate.and( stock.quantity.gt(0));
		}

		if(!params.show_free_products) {
			predicate.and( stock.price.gt(ZERO));
		}

		return predicate;
	}

	private BooleanBuilder getQueryPredicateForPromotions(QProducts product, ProductSearchParam params){
		AllowedPromotionConstraints searchParams = getSearchParam(params);
		BooleanBuilder promoPredicate = new BooleanBuilder();
		if(searchParams.getProductIds() != null && !searchParams.getProductIds().isEmpty())
			promoPredicate.or(product.id.in(searchParams.getProductIds()));
		if(searchParams.getBrandIds() != null && !searchParams.getBrandIds().isEmpty())
			promoPredicate.or(product.brandId.in(searchParams.getBrandIds()));
		if (searchParams.getTagIds() != null && !searchParams.getTagIds().isEmpty())
			promoPredicate.or(product.id.in(productRepository.getProductIdsByTagsList(searchParams.getTagIds())));

		return promoPredicate;
	}

	private BooleanBuilder getQueryPredicateForDiscounts(QStocks stock,QOrganizations organization, QShops shop
			, ProductSearchParam params){
		BooleanBuilder discountPredicate = new BooleanBuilder();
		if(params.discount != null && !params.discount.isEmpty())
			discountPredicate.or(stock.discount.in(params.discount));

		return discountPredicate;
	}



	private AllowedPromotionConstraints getSearchParam(ProductSearchParam productSearchParam) {

		List<PromosConstraints> promotionConstraints = getPromotionConstraints(productSearchParam);
		return extractSearchParamFromConstraints(promotionConstraints);
	}
	private AllowedPromotionConstraints extractSearchParamFromConstraints(List<PromosConstraints> constraints) {
		if (constraints == null)
			return new AllowedPromotionConstraints();

		AllowedPromotionConstraints searchParam = new AllowedPromotionConstraints();
		for (PromosConstraints constraint : constraints) {

			if(constraint.getBrands() != null && !constraint.getBrands().isEmpty())
				searchParam.addBrandIds(constraint.getBrands());

			if(constraint.getProducts() != null && !constraint.getProducts().isEmpty())
				searchParam.addProductIds(constraint.getProducts());

			if(constraint.getTags() != null && !constraint.getTags().isEmpty())
				searchParam.addTagIds(constraint.getTags());

		}
		return searchParam;
	}


	private List<PromosConstraints>  getPromotionConstraints(ProductSearchParam params) {
		Supplier<List<PromotionsEntity>> promotionsListSupplier = getPromosSupplier(params);
		if(promotionsListSupplier == null) {
			return null;
		}
		return promotionsListSupplier.get()
					.stream()
					.map(PromotionsEntity::getConstrainsJson).collect(Collectors.toList())
					.stream().map(this::readJsonStr).collect(Collectors.toList());

	}

private Supplier<List<PromotionsEntity>> getPromosSupplier(ProductSearchParam params) {
		Supplier<List<PromotionsEntity>> supplier;
		if(params.yeshtery_products && params.has_promotions && params.org_id == null)
			return supplier = () -> promotionRepository.findAllActivePromotions();

		if(params.yeshtery_products && params.promo_id != null && params.org_id == null )
			return supplier = () -> promotionRepository.findActivePromosByIds(params.promo_id);

		if(params.has_promotions && params.org_id != null)
		    return supplier = () -> promotionRepository.findActivePromosByOrgIdIn(List.of(params.org_id));

		if(params.promo_id != null && params.org_id != null )
		    return supplier = () -> promotionRepository.findByIdsAndOrgId(params.promo_id,params.org_id);

		return null;

	}


	private PromosConstraints readJsonStr(String jsonStr){
		ObjectMapper objectMapper = createObjectMapper();
		try {
			return objectMapper.readValue(jsonStr, PromosConstraints.class);
		} catch (Exception e) {
			logger.error(e,e);
			throw new RuntimeBusinessException(INTERNAL_SERVER_ERROR, PROMO$JSON$0001, jsonStr);
		}
	}

	ProductSearchParam getProductSearchParams(ProductSearchParam oldParams) throws BusinessException {
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

		if (params.org_id == null && params.shop_id == null && !params.yeshtery_products)
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

		Long settingsOrgId = ofNullable(params.org_id).orElse(params.getYeshtery_org_id());
		Map<String,String> orgSettings = orgService.getOrganizationSettings(settingsOrgId);

		params.show_free_products = isShowFreeProductsAllowed(orgSettings);

		params.hide_empty_stocks = isHideEmptyStocksAllowed(params, orgSettings);

		if (params.include_out_of_stock == null)
			params.include_out_of_stock = false;

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
		if (REPOSITORY_SORT.contains(ProductSortOptions.getProductSortOptions(sort))) {
			Direction direction = order.equals("asc") ? Direction.ASC : Direction.DESC;
			products = productRepository.findByIdIn(productsIds, Sort.by(direction, sort));
		}
		return products;
	}


	private ProductsResponse getProductsResponseForBundles(List<ProductEntity> products, String order, String sort, Long productsCount) {
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
		Map<Long, List<ProductImageDTO>> productImages = imgService.getProductsAllImagesMap(productIdList, variantsIdList);
		Map<Long, String> productCoverImages = imgService.getProductsImagesMap(productImages);

		List<ProductRepresentationObject> productsRep =
				products.stream()
						.map(prod -> getProductRepresentation(prod, productCoverImages))
						.map(p -> setProductImages(p, productImages))
						.collect(toList());

		if (ProductSortOptions.getProductSortOptions(sort) == ProductSortOptions.PRICE)
			sortByPrice(productsRep, order);

		return new ProductsResponse(productsCount, productsRep);

	}

	private ProductsResponse getProductResponseFromStocks(List<ProductRepresentationObject> stocks,
														  Long productsCount, boolean includeOutOfStock) {
		if(stocks != null && !stocks.isEmpty()) {
			List<Long> stocksIds = stocks.stream()
					.map(ProductRepresentationObject::getStockId)
					.collect(toList());

			List<Long> productIdList = stocks.stream()
					.map(ProductRepresentationObject::getId)
					.collect(toList());

			List<Long> variantsIds = productVariantsRepository.getVariantsIdsByStocksIds(stocksIds);

			Map<Long, Prices> productsPricesMap =
					mapInBatches(productIdList, 1000, ids -> stockRepository.getProductsPrices(ids, includeOutOfStock))
							.stream()
							.collect(toMap(Prices::getId, p -> new Prices(p.getMinPrice(), p.getMaxPrice())));

			Map<Long, Prices> collectionsPricesMap =
					mapInBatches(productIdList, 1000, ids -> stockRepository.getCollectionsPrices(ids, includeOutOfStock))
							.stream()
							.collect(toMap(Prices::getId, p -> new Prices(p.getMinPrice(), p.getMaxPrice())));

			Map<Long, List<ProductImageDTO>> productImages = imgService.getProductsAllImagesMap(productIdList, variantsIds);

			Map<Long, String> productCoverImages = imgService.getProductsImagesMap(productImages);

			Map<Long, Double> productRatings = productRatingRepo.findProductsAverageRating(productIdList)
					.stream()
					.collect(toMap(ProductRatingData::getProductId, ProductRatingData::getAverage));

			Map<Long, List<TagsRepresentationObject>> productsTags = getProductsTagsDTOList(productIdList);

			List<Long> productsVariantsCountFlag = filterProductsWithMultipleVariants(productIdList);

			Map<Long, List<Long>> product360Shops = getProducts360ShopsList(productIdList);

			stocks.stream()
					.map(s -> setAdditionalInfo(s, productCoverImages))
					.map(s -> setProductImages(s, productImages))
					.map(s -> setProductTags(s, productsTags))
					.map(s -> setProductMultipleVariants(s, productsVariantsCountFlag))
					.map(s -> setProductPrices(s, productsPricesMap))
					.map(s -> setCollectionPrices(s, collectionsPricesMap))
					.map(s -> setProductShops(s, product360Shops))
					.map(s -> setProductRating(s, productRatings))
					.collect(toList());
		}

		return new ProductsResponse(productsCount, stocks);

	}

	private ProductRepresentationObject setProductRating(ProductRepresentationObject product, Map<Long, Double> ratings) {
		if (ratings.containsKey(product.getId()))
			product.setRating(ratings.get(product.getId()));
		return product;
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
		if (product.getProductType() == STOCK_ITEM){
			Prices prices = ofNullable(pricesMap.get(product.getId())).orElse(new Prices());
			product.setPrices(prices);
		}
		return product;
	}


	private ProductRepresentationObject setCollectionPrices(ProductRepresentationObject product,
															Map<Long, Prices> pricesMap) {
		if (product.getProductType() == COLLECTION){
			Prices prices = ofNullable(pricesMap.get(product.getId())).orElse(new Prices());
			product.setPrices(prices);
		}
		return product;
	}


	private ProductRepresentationObject setProductImages(ProductRepresentationObject product,
														 Map<Long, List<ProductImageDTO>> imagesMap) {
		List<ProductImageDTO> images = imagesMap.get(product.getId());
		product.setImages(images);
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





	private Optional<StocksEntity> getDefaultProductStock(ProductEntity product, boolean includeOutOfStock) {
		return ofNullable(product)
				.map(ProductEntity::getProductVariants)
				.orElseGet(Collections::emptySet)
				.stream()
				.map(ProductVariantsEntity::getStocks)
				.filter(Objects::nonNull)
				.flatMap(Set::stream)
				.filter(s -> s.getQuantity() != null)
				.filter(s -> checkEmptyStock(s, includeOutOfStock))
				.min( comparing(StocksEntity::getPrice));
	}


	private boolean checkEmptyStock(StocksEntity stock, boolean includeOutOfStock) {
		return includeOutOfStock  || stock.getQuantity() > 0;
	}





	@Override
	public ProductUpdateResponse updateProduct(String productJson, Boolean isBundle, Boolean isCollection) {
		Long id = updateProductBatch(asList(productJson), isBundle, isCollection)
				.stream()
				.findFirst()
				.orElse(null);
		return new ProductUpdateResponse(id);
	}




	@Override
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
		Map<Long, BrandsEntity> brands = createBrandsCache(productsJson);

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



	private Map<Long, BrandsEntity> createBrandsCache(List<JSONObject> productsJson) {
		List<Long> brandIds = extractBrandIds(productsJson);

		return mapInBatches(brandIds, 500, brandRepo::findByIdIn)
				.stream()
				.distinct()
				.collect(toMap(BrandsEntity::getId, brand -> brand));
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

		updateProductEntityFromJson(entity, productJsonNode, cache.getBrands());

		return entity;
	}




	private void updateProductEntityFromJson(ProductEntity entity, JsonNode productJsonNode, Map<Long, BrandsEntity> brandsCache){
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

			if ( productDto.getBrandId() != null) {
				BrandsEntity brand = brandsCache.get(productDto.getBrandId());
				entity.setBrand(brand);
			}
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
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, P$BRA$0004);
		}

		long id = brandId.asLong();
		if(!cache.getBrands().containsKey(id)) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, P$BRA$0001 , id);
		}

		Long brandOrgId = ofNullable(cache.getBrands())
				.map(map -> map.get(id))
				.map(BrandsEntity::getOrganizationEntity)
				.map(OrganizationEntity::getId)
				.orElse(-2L);

		if( !Objects.equals(brandOrgId, orgId)) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, P$BRA$0002 , id, orgId);
		}
	}



	@Override
	public ProductsDeleteResponse deleteProducts(List<Long> productIds, Boolean forceDeleteCollectionItems) {
		validateProductToDelete(productIds);
		validateVariantsExistenceInCollections(productIds, forceDeleteCollectionItems);
		try {
			transactions.deleteProducts(productIds, forceDeleteCollectionItems);
		} catch (Throwable e) {
			logger.error(e,e);
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, P$PRO$0009, e.getMessage());
		}
		return new ProductsDeleteResponse(true, productIds);
	}

	@Override
	public void deleteVariants(List<Long> variantIds, Boolean forceDeleteCollectionItems) {
		validateVariantsToDelete(variantIds, forceDeleteCollectionItems);
		try {
			transactions.deleteVariants(variantIds, forceDeleteCollectionItems);
		} catch (Throwable e) {
			logger.error(e,e);
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, P$VAR$009, e.getMessage());
		}
	}

	private void validateVariantsToDelete(List<Long> variantIds, Boolean forceDeleteCollectionItems) {
		Long userOrgId = securityService.getCurrentUserOrganizationId();
		long variantsCount = productVariantsRepository.countByIdInAndProductEntity_organizationId(variantIds, userOrgId);
		if (variantsCount < variantIds.size()) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, P$VAR$010);
		}
		if (!forceDeleteCollectionItems && collectionItemRepo.countByItem_IdInAndItem_ProductEntity_OrganizationId(variantIds, userOrgId) > 0) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, P$PRO$0014);
		}
	}

	private void validateVariantsExistenceInCollections(List<Long> productIds, Boolean forceDelete) {
		if (!forceDelete && collectionItemRepo.countByItem_ProductEntity_IdIn(productIds) > 0) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, P$PRO$0014);
		}
	}


	private void validateProductToDelete(List<Long> productIds) {
		validateUserCanDeleteProducts(productIds);
		validateProductIsNotInBundle(productIds);
	}


	private void validateUserCanDeleteProducts(List<Long> productIds) {
		Long userOrgId = securityService.getCurrentUserOrganizationId();

		mapInBatches(productIds, 500, productRepository::findByIdIn)
				.forEach(p -> validateUserCanDeleteProduct(p, userOrgId));
	}



	private void validateUserCanDeleteProduct(ProductEntity product, Long orgId) {
		if (!Objects.equals(product.getOrganizationId(), orgId)){
			throw new RuntimeBusinessException(FORBIDDEN, P$PRO$0010, product.getId(), orgId);
		}
	}



	private void validateProductIsNotInBundle(List<Long> productIds) {
		List<BundleEntity> bundles =
				mapInBatches(productIds, 500, bundleRepository::getBundlesHavingItemsWithProductIds);
		if(bundles.size() != 0) {
			String bundleIds = bundles
					.stream()
					.map(BundleEntity::getId)
					.map(String::valueOf)
					.collect(joining(","));

			throw new RuntimeBusinessException(NOT_ACCEPTABLE, P$PRO$0011, bundleIds);
		}
	}



	@Override
	public ProductsDeleteResponse deleteBundle(Long bundleId) throws BusinessException {
		validateBundleToDelete(bundleId);

		transactions.deleteBundle(bundleId);
		return new ProductsDeleteResponse(true, Collections.singletonList(bundleId));
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

	@Override
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
		String coverImg = imgService.getProductCoverImage( entity.getId() );
		if (coverImg != null)
			dto.setImageUrl( coverImg );
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
			Map<Long, List<ProductImageDTO>> productImages = imgService.getProductsAllImagesMap(productIdList, null);
			Map<Long, String> productCoverImages = imgService.getProductsImagesMap(productImages);
			productlist = productRepository.findByIdInOrderByNameAsc(productIdList)
					.stream()
					.map(prod -> getProductRepresentation(prod, productCoverImages))
					.map(p -> setProductImages(p, productImages))
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




	@Override
	public void updateBundleElement(BundleElementUpdateDTO element) throws BusinessException {
		validateBundleElementUpdateReq(element);

		if(element.getOperation().equals( DELETE)) {
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
				||opr.equals(DELETE) ) ) {
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



	@Override
	public VariantUpdateResponse updateVariant(VariantUpdateDTO variant) throws BusinessException {
		Long variantId = updateVariantBatch(asList(variant)).stream().findFirst().orElse(-1L);
		return new VariantUpdateResponse(variantId);
	}




	@Override
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
			.collect(collectingAndThen(toList(), extraAttrRepo::saveAll))
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


	private void validateProductNewFlow(NewProductFlowDTO dto ) throws BusinessException {
		
		validateOperation(dto.getOperation());
		validatProductDTO(dto);
	}
	
	private void validatProductDTO(NewProductFlowDTO dto) throws BusinessException {
		
		if (dto.getOperation().equals(UPDATE) && dto.getProductId() == null) {
			throw new BusinessException(
					"Missing required parameters !"
					, "MISSING PARAM"
					, NOT_ACCEPTABLE);
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
		Map<String, String> features = ofNullable(variant.getFeatures())
				.orElse(new HashMap<>());
		if(isBlankOrNull(features)) {
			return;
		}

		if(hasInvalidFeatureKeys(features, cache.getOrganziationFeatures())) {
			throw new BusinessException(
					format("Invalid parameter [features], a feature key doesnot exists or doesn't belong to organization with id[%d]" ,userOrgId )
					, "INVALID PARAM:features"
					, NOT_ACCEPTABLE);
		}
	}




	private boolean hasInvalidFeatureKeys(Map<String, String> features, Set<ProductFeaturesEntity> orgFeatures) {
		Set<Integer> productFeatureIds = orgFeatures.stream().map(ProductFeaturesEntity::getId).collect(toSet());
		return features
				.keySet()
				.stream()
				.map(Integer::valueOf)
				.anyMatch(id -> !productFeatureIds.contains(id));
	}






	private ProductVariantsEntity createVariantEntity(VariantUpdateDTO variant, VariantUpdateCache cache) {
		ProductVariantsEntity entity = new ProductVariantsEntity();

		Operation operation = variant.getOperation();
		Long id = variant.getVariantId();

		if(operation.equals(UPDATE)) {
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
			Set<VariantFeatureValueEntity> featuresValues = updateVariantFeatureValues(variant, entity, cache);
			entity.addFeatureValues(featuresValues);
		}

		if(variant.isUpdated("extraAttr")) {
			Set<ProductExtraAttributesEntity> productExtraAttributesEntities = updateVariantExtraAttributes(variant, entity, cache);
			entity.addExtraAttribute(productExtraAttributesEntities);
		}

		if(variant.isUpdated("sku")) {
			entity.setSku(variant.getSku());
		}

		if(variant.isUpdated("productCode")) {
			entity.setProductCode(variant.getProductCode());
		}

		if(variant.isUpdated("weight")) {
			var weight = ofNullable(variant.getWeight()).orElse(ZERO);
			entity.setWeight(weight);
		}

		String pname = getPname(variant, operation);
		if(!isBlankOrNull(pname)) {
			entity.setPname(pname);
		}

		return entity;
	}


	private Set<VariantFeatureValueEntity> updateVariantFeatureValues(VariantUpdateDTO variant, ProductVariantsEntity entity, VariantUpdateCache cache) {
		Set<VariantFeatureValueEntity> existingFeaturesValues = entity.getFeatureValues();
		Set<VariantFeatureValueEntity> allFeaturesValues = new HashSet<>();

		for (var newFeatureEntry : variant.getFeatures().entrySet()) {
			ProductFeaturesEntity matchedOrgFeature = matchWithOrgFeature(cache, newFeatureEntry);

			VariantFeatureValueEntity featureValue =
					existingFeaturesValues
						.stream()
						.filter(f -> Objects.equals(f.getFeature(), matchedOrgFeature))
						.findFirst()
						.orElseGet(() -> new VariantFeatureValueEntity());

			featureValue.setFeature(matchedOrgFeature);
			featureValue.setVariant(entity);
			featureValue.setValue(newFeatureEntry.getValue().toString());
			allFeaturesValues.add(featureValue);
		}
		return allFeaturesValues;
	}

	private ProductFeaturesEntity matchWithOrgFeature(VariantUpdateCache cache, Map.Entry<String, String> newFeatureEntry){
		return cache.getOrganziationFeatures()
				.stream()
				.filter(orgFeature -> Objects.equals(orgFeature.getId(), Integer.parseInt(newFeatureEntry.getKey().toString())))
				.findFirst()
				.get();
	}


	private Set<ProductExtraAttributesEntity> updateVariantExtraAttributes(VariantUpdateDTO variant, ProductVariantsEntity entity, VariantUpdateCache cache) {
		Set<ProductExtraAttributesEntity> existingExtraAttributes = entity.getExtraAttributes();
		Set<ProductExtraAttributesEntity> allExtraAttributes = new HashSet<>();

		for (var newExtraAttr : variant.getExtraAttr().entrySet()) {
			ExtraAttributesEntity matchedOrgExtraAttr = matchWithOrgExtraAttribute(cache, newExtraAttr);

			ProductExtraAttributesEntity extraAttributesEntity = existingExtraAttributes
					.stream()
					.filter(ex -> ex.getExtraAttribute().getName().equalsIgnoreCase(matchedOrgExtraAttr.getName()))
					.findFirst()
					.orElseGet(() -> new ProductExtraAttributesEntity());

			extraAttributesEntity.setExtraAttribute(matchedOrgExtraAttr);
			extraAttributesEntity.setVariant(entity);
			extraAttributesEntity.setValue(newExtraAttr.getValue());
			allExtraAttributes.add(extraAttributesEntity);
		}
		return allExtraAttributes;
	}

	private ExtraAttributesEntity matchWithOrgExtraAttribute(VariantUpdateCache cache, Map.Entry<String, String> newExtraAttr){
		return cache.getOrgExtraAttributes()
					.keySet()
					.stream()
					.filter(orgExtraAttr -> orgExtraAttr.equalsIgnoreCase(newExtraAttr.getKey()))
					.map(s -> cache.getOrgExtraAttributes().get(s))
					.findFirst()
					.get();
	}

	private ProductExtraAttributesEntity createVariantExtraAttributeEntity(String name, String value, VariantUpdateCache cache) {
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
		String type = getExtraAttributeTypeFromName(name);
		ExtraAttributesEntity newAttr = new ExtraAttributesEntity();
		newAttr.setName(name);
		newAttr.setOrganizationId(orgId);
		newAttr.setType(type);
		return newAttr;
	}



	private String getExtraAttributeTypeFromName(String name){
		if(isNull(name) || name.isEmpty()){
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, P$VAR$008);
		}
		if(name.startsWith("$")){
			return INVISIBLE.getValue();
		}else{
			return ExtraAttributeType.STRING.getValue();
		}
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



	private ProductRepresentationObject getProductRepresentation(ProductEntity product, boolean includeOutOfStock) {
		boolean isHidden = ofNullable(product.getHide()).orElse(false);
		List<TagsRepresentationObject> productTags = getProductTagsDTOList(product.getId());

		ProductRepresentationObject productRep = new ProductRepresentationObject();
		setProductProperties(productRep, product);
		productRep.setMultipleVariants( product.getProductVariants().size() > 1);
		productRep.setTags(productTags);
		productRep.setHidden(isHidden);
		getDefaultProductStock(product, includeOutOfStock)
				.ifPresent(stk -> setDefaultStockData(productRep, stk));
		return productRep;
	}



	private void setDefaultStockData(ProductRepresentationObject productRep, StocksEntity defaultStock) {
		productRep.setPrice( defaultStock.getPrice() );
		productRep.setDiscount( defaultStock.getDiscount() );
		productRep.setStockId( defaultStock.getId());
		productRep.setDefaultVariantFeatures( parseVariantFeatures(defaultStock.getProductVariantsEntity(), 1) );
		if (defaultStock.getCurrency() != null) {
			productRep.setCurrency( defaultStock.getCurrency().getValue());
		}
	}


	private void setProductProperties(ProductRepresentationObject productRep, ProductEntity product) {
		Long brandId = ofNullable(product.getBrand())
				.map(BrandsEntity::getId)
				.orElse(null);
		productRep.setId(product.getId());
		productRep.setName(product.getName());
		productRep.setPname(product.getPname());
		productRep.setBrandId(brandId);
		productRep.setCategoryId(product.getCategoryId());
		productRep.setBarcode(product.getBarcode());
		productRep.setCreationDate(Optional.ofNullable(product.getCreationDate().toString()).orElse(null));
		productRep.setUpdateDate(Optional.ofNullable(product.getUpdateDate().toString()).orElse(null));
		productRep.setHas_360_view(product.getSearch360());
		productRep.setPriority(product.getPriority());
	}




	private ProductRepresentationObject getProductRepresentation(ProductEntity product, Map<Long, String> productCoverImgs) {
		ProductRepresentationObject rep = getProductRepresentation(product, true);
		setAdditionalInfo(rep, productCoverImgs);
		return rep;
	}




	@Override
	public ProductDetailsDTO toProductDetailsDTO(ProductEntity product, boolean includeOutOfStock) {
		ProductDetailsDTO dto = new ProductDetailsDTO();
		ProductRepresentationObject representationObj = getProductRepresentation(product, includeOutOfStock);
		copyProperties(representationObj, dto);
		dto.setDescription( product.getDescription() );
		dto.setProductType( product.getProductType() );
		dto.setOrganizationId( product.getOrganizationId() );
		String coverImg = imgService.getProductCoverImage( product.getId() );
		if (coverImg != null)
			dto.setImageUrl( coverImg );

		return dto;
	}




	@Override
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



	@Override
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
				.map(ids -> mapInBatches(ids, 5000, orgTagRepo::getTagsByProductIdIn))
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
		Collection<Long> batch =  mapInBatches(productIds, 5000, p -> productRepository.getExistingProductIds(new HashSet<>(p), orgId));
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



	@Override
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




	@Override
	public void deleteAllProducts(boolean isConfirmed) throws BusinessException {
		if(!isConfirmed) {
			throw new BusinessException("Unconfirmed Delete operation for all products!" , "UNCONFIRMED OPERATION", NOT_ACCEPTABLE);
		}
		Long orgId = securityService.getCurrentUserOrganizationId();

		cartRepo.deleteByOrganizationId(orgId);
		productVariantsRepository.deleteAllByProductEntity_organizationId(orgId);
		collectionItemRepo.deleteItemsByOrganizationId(orgId);
		productRepository.deleteAllByOrganizationId(orgId);
	}



	@Override
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



	@Override
	public void deleteAllTagsForProducts(List<Long> products) {
		processInBatches(products, 10000, productRepository::deleteAllTagsForProducts);
	}



	@Override
	@Transactional
	public void updateCollection(CollectionItemDTO elements) {
		validateCollectionItems(elements);
		updateCollectionItems(elements);
	}



	private void validateCollectionItems(CollectionItemDTO element) {
		if(!setOf(DELETE, UPDATE, ADD).contains(element.getOperation())) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, P$PRO$0008, "update, delete");
		}
		if (isNull(element.getProductId()))
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, P$PRO$0001);
		if (isNull(element.getVariantIds()) || element.getVariantIds().isEmpty())
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, P$VAR$003);
	}



	private void updateCollectionItems(CollectionItemDTO element) {
		Long orgId = securityService.getCurrentUserOrganizationId();
		ProductCollectionEntity collection = getCollectionByProductIdAndOrgId(element.getProductId(), orgId);

		List<Long> givenItemsIds = element.getVariantIds();
		List<ProductVariantsEntity> variantsEntities =
				productVariantsRepository.findDistinctByIdInAndProductEntity_OrganizationId(givenItemsIds, orgId);
		Map<Long,Integer> itemsOrder =	getCollectionItemsOrder(givenItemsIds);

		validateVariantsExistence(variantsEntities, givenItemsIds);

		Operation operation = element.getOperation();
		if(Objects.equals(operation, DELETE)) {
			removeCollectionItems(new HashSet<>(givenItemsIds), collection);
		}else if(Objects.equals(operation, UPDATE)){
			recreateCollectionItems(collection, variantsEntities, itemsOrder);
		}else if(Objects.equals(operation, ADD)){
			appendCollectionItems(collection, variantsEntities, itemsOrder);
		}
		productCollectionRepo.save(collection);
	}



	private Map<Long, Integer> getCollectionItemsOrder(List<Long> givenItemsIds) {
		return IntStream
				.range(0, givenItemsIds.size())
				.mapToObj(i -> new ImmutablePair<>(givenItemsIds.get(i), i))
				.collect(toMap(ImmutablePair::getKey, ImmutablePair::getValue, FunctionalUtils::getFirst));
	}




	private void appendCollectionItems(ProductCollectionEntity collection, List<ProductVariantsEntity> variants
						,Map<Long,Integer> itemsOrder) {
		Integer currentMaxPriority = getCollectionItemsCurrentMaxPriority(collection);
		List<ProductVariantsEntity> newItems = getOnlyNewCollectionItems(variants, collection);
		createCollectionItemsWithPriority(collection, newItems, itemsOrder, currentMaxPriority)
			.forEach(collection::addItem);
	}



	private void recreateCollectionItems(ProductCollectionEntity collection
			, List<ProductVariantsEntity> variants, Map<Long,Integer> itemsOrder) {
		clearCollectionItems(collection);
		createCollectionItemsWithPriority(collection, variants, itemsOrder, -1)
			.forEach(collection::addItem);
	}



	private void clearCollectionItems(ProductCollectionEntity collection) {
		Set<ProductCollectionItemEntity> oldItems = collection.getItems();
		collectionItemRepo.deleteItems(oldItems);
		oldItems.clear();
	}


	private void removeCollectionItems(Set<Long> variantIds, ProductCollectionEntity collection) {
				collection
				.getItems()
				.stream()
				.filter(item -> variantIds.contains(item.getItem().getId()))
				.collect(
						collectingAndThen(
								toSet()
								, toDelete -> {
										collectionItemRepo.deleteItems(toDelete);
										collection.getItems().removeAll(toDelete);
										return toDelete;
								}));
	}



	private List<ProductCollectionItemEntity> createCollectionItemsWithPriority(ProductCollectionEntity collection
			, List<ProductVariantsEntity> variants, Map<Long,Integer> itemsOrder, Integer currentMaxPriority){
		return variants
				.stream()
				.map(variant -> createCollectionItem(collection, variant, getPriorityForNewItem(itemsOrder, currentMaxPriority, variant)))
				.collect(toList());
	}



	private int getPriorityForNewItem(Map<Long, Integer> itemsOrder, Integer currentMaxPriority, ProductVariantsEntity variant) {
		return currentMaxPriority + 1 + itemsOrder.get(variant.getId());
	}



	private List<ProductVariantsEntity> getOnlyNewCollectionItems(List<ProductVariantsEntity> variants, ProductCollectionEntity collection) {
		Set<Long> currentItems = getCollectionItemsIds(collection);
		return variants
				.stream()
				.filter(variant -> !currentItems.contains(variant.getId()))
				.collect(toList());
	}



	private Integer getCollectionItemsCurrentMaxPriority(ProductCollectionEntity collection) {
		return collection
				.getItems()
				.stream()
				.map(ProductCollectionItemEntity::getPriority)
				.max(Integer::compareTo)
				.orElse(-1);
	}



	private Set<Long> getCollectionItemsIds(ProductCollectionEntity collection) {
		return collection
				.getVariants()
				.stream()
				.map(ProductVariantsEntity::getId)
				.collect(toSet());
	}




	private ProductCollectionItemEntity createCollectionItem(ProductCollectionEntity collection
			,ProductVariantsEntity variant, Integer priority){
		ProductCollectionItemEntity item = new ProductCollectionItemEntity();
		item.setCollection(collection);
		item.setItem(variant);
		item.setPriority(priority);
		return item;
	}


	private void validateVariantsExistence(List<ProductVariantsEntity> variantsEntities, List<Long> variantsIds) {
		Set<Long> fetchedVariantsIds =
				variantsEntities
				.stream()
				.map(ProductVariantsEntity::getId)
				.collect(toSet());
		Set<Long> givenVariantIds = new HashSet<>(variantsIds);
		if (!Objects.equals(fetchedVariantsIds, givenVariantIds)) {
			givenVariantIds.removeAll(fetchedVariantsIds);
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, P$VAR$0001, givenVariantIds.toString());
		}
	}



	private ProductCollectionEntity getCollectionByProductIdAndOrgId(Long productId, Long orgId) {
		return productCollectionRepo.findByIdAndOrganizationId(productId, orgId)
				.orElseThrow(() ->  new RuntimeBusinessException(NOT_ACCEPTABLE, P$PRO$0002, productId));
	}


	private ProductCollectionEntity getCollectionById(Long productId) {
		return productCollectionRepo
				.findByCollectionId(productId)
				.orElseThrow(() ->  new RuntimeBusinessException(NOT_FOUND, P$PRO$0012, productId));
	}



	@Override
	public ProductDetailsDTO getCollection(Long id) {
		ProductCollectionEntity entity = getCollectionById(id);
		return toCollectionDetailsDTO(entity);
	}


	@Override
	public List<ProductDetailsDTO> getEmptyCollections() {
		Long orgId = securityService.getCurrentUserOrganizationId();
		List<ProductCollectionEntity> collectionsEntities = productCollectionRepo.findByOrganizationId(orgId);

		return collectionsEntities
				.stream()
				.filter(c -> c.getItems().isEmpty())
				.map(c -> toCollectionDetailsDTO(c))
				.collect(toList());
	}



	@Override
	public List<ProductDetailsDTO> getEmptyProducts()  {
		Long orgId = securityService.getCurrentUserOrganizationId();
		return productRepository.findEmptyProductsByOrganizationIdAndProductType(orgId, 0)
				.stream()
				.map(c -> createProductDetailsDTO(c, null, null, true))
				.collect(toList());
	}



	private ProductDetailsDTO toCollectionDetailsDTO(ProductCollectionEntity entity) {
		String coverImg = imgService.getProductCoverImage( entity.getId() );
		List<TagsRepresentationObject> tagsDTOList = getProductTagsDTOList(entity.getId());
		List<ProductImageDTO> collectionImages = imgService.getProductsAndVariantsImages(asList(entity.getId()), null);
		List<Long> collection360Shops = product360ShopsRepo.findShopsByProductId(entity.getId());

		ProductDetailsDTO dto = new ProductDetailsDTO();
		copyProperties(entity, dto, new String[] {"variants"});

		dto.setCreationDate(entity.getCreationDate().toString());
		dto.setUpdateDate(entity.getUpdateDate().toString());
		dto.setHas_360_view(entity.getSearch360());
		if (coverImg != null){
			dto.setImageUrl( coverImg );
		}
		dto.setTags(tagsDTOList);
		dto.setImages(getProductImages(collectionImages));
		dto.setShops(collection360Shops);

		if(!entity.getVariants().isEmpty()) {
			List<ProductVariantsEntity> variantsList = new ArrayList<>(entity.getVariants());
			List<Long> variantsIds = getVariantsIds(variantsList);
			List<ProductImageDTO> productsAndVariantsImages = imgService.getProductsAndVariantsImages(asList(entity.getId()), variantsIds);

			dto.setVariants(getItemsDtoList(entity, productsAndVariantsImages));
			dto.setVariantFeatures( getVariantFeatures(variantsList) );
			dto.setImages(getProductImages(productsAndVariantsImages));
		}
		return dto;
	}



	private List<VariantDTO> getItemsDtoList(ProductCollectionEntity entity, List<ProductImageDTO> productsAndVariantsImages) {
		return entity
				.getItems()
				.stream()
				.sorted(comparing(ProductCollectionItemEntity::getPriority))
				.map(ProductCollectionItemEntity::getItem)
				.map(v -> createVariantDto(null, v, productsAndVariantsImages))
				.collect(toList());
	}



	private List<Long> getVariantsIds(List<ProductVariantsEntity> variants) {
		return variants
				.stream()
				.map(ProductVariantsEntity::getId)
				.collect(toList());
	}


	@Override
	public VariantsResponse getVariants(Long orgId, String name, Integer start, Integer count) {
		VariantSearchParam params = normalizeVariantSearchParam(name, start, count);
		Long total = productVariantsRepository.countByOrganizationId(orgId, params.getName());
		PageRequest page = getQueryPage(params.getStart(), params.getCount());
		List<ProductVariantsEntity> variantsEntities =
				productVariantsRepository.findByOrganizationId(orgId, params.getName(), page);

		return prepareVariantsDtos(variantsEntities, total);
	}

	private VariantsResponse prepareVariantsDtos(List<ProductVariantsEntity> variantsEntities, Long total) {
		List<Long> variantsIds = getVariantsIds(variantsEntities);
		List<ProductImageDTO> productsAndVariantsImages = variantsIds.isEmpty() ? new ArrayList<>() : imgService.getProductsAndVariantsImages(null,variantsIds) ;

		List<VariantDTO> variants =  variantsEntities
				.stream()
				.map(v -> createVariantDto(null, v, productsAndVariantsImages))
				.collect(toList());
		return new VariantsResponse(total, variants);
	}

	@Override
	public VariantsResponse getVariantsForYeshtery(String name, Integer start, Integer count) {
		VariantSearchParam params = normalizeVariantSearchParam(name, start, count);
		Long total = productVariantsRepository.countByYeshteryProducts(params.getName());
		PageRequest page = getQueryPage(params.getStart(), params.getCount());
		List<ProductVariantsEntity> variantsEntities =
				productVariantsRepository.findByYeshteryProducts(params.getName(), page);

		return prepareVariantsDtos(variantsEntities, total);
	}

	@Override
	public void deleteVariantFeatureValue(Long variantId, Integer featureId) {
		Long orgId = securityService.getCurrentUserOrganizationId();
		if (variantId != null) {
			VariantFeatureValueEntity entity =
					variantFeatureValuesRepo.findByVariantIdAndFeatureIdAndOrganizationId(orgId, variantId, featureId)
							.orElseThrow(() -> new RuntimeBusinessException(NOT_FOUND, P$VAR$011, featureId, variantId));
			variantFeatureValuesRepo.delete(entity);
		} else {
			Set<VariantFeatureValueEntity> entities = variantFeatureValuesRepo.findByFeatureIdAndOrganizationId(orgId, featureId);
			variantFeatureValuesRepo.deleteAll(entities);
		}
	}

	@Override
	public void deleteVariantExtraAttribute(Long variantId, Integer extraAttributeId, Long extraAttributeValueId) {
		Long orgId = securityService.getCurrentUserOrganizationId();
		ProductVariantsEntity variant = productVariantsRepository.findByIdAndProductEntity_OrganizationId(variantId, orgId)
				.orElseThrow(() -> new RuntimeBusinessException(NOT_FOUND, P$VAR$0001, variantId));
		ExtraAttributesEntity extraAttribute = extraAttrRepo.findByIdAndOrganizationId(extraAttributeId, orgId)
				.orElseThrow(() -> new RuntimeBusinessException(NOT_FOUND, ORG$EXTRATTR$0001, extraAttributeId));

		if (extraAttributeValueId != null) {
			productExtraAttributesRepo.deleteByIdVariantAndExtraAttribute(extraAttributeValueId, variant, extraAttribute);
		} else {
			productExtraAttributesRepo.deleteByIdVariantAndExtraAttribute(variant, extraAttribute);
		}
	}

	private VariantSearchParam normalizeVariantSearchParam(String name, Integer start, Integer count) {
		VariantSearchParam params = new VariantSearchParam();
		if (isBlankOrNull(start) || start < 0) {
			params.setStart(0);
		} else {
			params.setStart(start);
		}
		if (isBlankOrNull(count) || count < 0 || count > 1000) {
			params.setCount(10);
		} else {
			params.setCount(count);
		}
		params.setName(ofNullable(name.toLowerCase()).orElse(""));
		return params;
	}


	@Override
	public void updateRelatedItems(RelatedItemsDTO relatedItems) {
		if (relatedItems.isAdd()) {
			addRelatedItems(relatedItems);
		} else {
			removeRelatedItems(relatedItems);
		}
	}


	private void removeRelatedItems(RelatedItemsDTO relatedItems) {
		Long orgId = securityService.getCurrentUserOrganizationId();
		relatedProductsRepo.deleteByProductAndRelatedProductsIn(relatedItems.getProductId(),
				relatedItems.getRelatedProductsIds());
	}

	private void addRelatedItems(RelatedItemsDTO relatedItems) {
		Long orgId = securityService.getCurrentUserOrganizationId();

		ProductEntity existingProduct = getProductEntity(relatedItems.getProductId(), orgId);
		List<ProductEntity> existingProducts = productRepository
				.findByIdInAndOrganizationId(relatedItems.getRelatedProductsIds(), orgId);

		existingProducts
				.stream()
				.map( r -> new RelatedProductsEntity(existingProduct, r))
				.forEach(r -> relatedProductsRepo.save(r));

	}

	private ProductEntity getProductEntity(Long productId, Long orgId) {
		return productRepository
				.findByIdAndOrganizationId(productId, orgId)
				.orElseThrow( () -> new RuntimeBusinessException(NOT_ACCEPTABLE, P$PRO$0002, productId));

	}


	@Override
	public List<ProductRepresentationObject> getRelatedProducts(Long productId) {
		return relatedProductsRepo
				.findByProduct_Id(productId)
				.stream()
				.map(RelatedProductsEntity::getRelatedProduct)
				.map(p -> getProductRepresentation(p, true))
				.collect(toList());
	}

	@Override
	public void deleteCollection(List<Long> ids) {
		Long orgId = securityService.getCurrentUserOrganizationId();

		List<ProductCollectionEntity> collections = productCollectionRepo.findByIdInAndOrganizationId(ids, orgId);
		validateCollectionDeletion(ids, collections);

		Set<ProductCollectionItemEntity> collectionItems = collections
				.stream()
				.map(ProductCollectionEntity::getItems)
				.flatMap(Set::stream)
				.collect(toSet());

		collectionItemRepo.deleteItems(collectionItems);
		productCollectionRepo.removeCollections(collections, orgId);
	}


	private void validateCollectionDeletion(List<Long> ids, List<ProductCollectionEntity> collections) {
		if (ids.size() != collections.size()) {
			List<Long> collectionsIds = collections
					.stream()
					.map(ProductCollectionEntity::getId)
					.collect(toList());
			ids.removeAll(collectionsIds);
			throw new RuntimeBusinessException(NOT_FOUND, P$PRO$0013, ids.toString());
		}
	}



    @Override
		public List<Long> getVariantsWithFeature(ProductFeaturesEntity feature) {
		return variantFeatureValuesRepo.findByFeature(feature.getId(), feature.getOrganization().getId());
    }

	@Override

	public ProductUpdateResponse updateProductVersion2(
			NewProductFlowDTO productJson,
			MultipartFile[] imgs,
			Integer[] uploadedImagePriorities,
			List<Map<String, Long>> updatedImages,
			Long[] deletedImages
	) throws BusinessException, JsonMappingException, JsonProcessingException {

		validateProductNewFlow(productJson);
		ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
		String json = ow.writeValueAsString(productJson);

		List<Long> tagsId = productJson.getTags();
		List<String> keywords = productJson.getKeywords();
		Long id = updateProductBatch(asList(json), false, false).stream().findFirst().orElse(null);
		if (id != null) {
			if (imgs != null) {
				for (int i = 0; i < imgs.length; i++) {
					ProductImageUpdateDTO img = new ProductImageUpdateDTO();
					img.setOperation(Operation.CREATE);
					img.setProductId(id);
					int priority = (uploadedImagePriorities != null && i < uploadedImagePriorities.length)
							? uploadedImagePriorities[i]
							: i + 1;
					img.setPriority(priority);
					img.setType(7);
					imgService.updateProductImage(imgs[i], img);
				}
			}

			if (tagsId != null && !tagsId.isEmpty()) {
				ProductTagDTO tags = new ProductTagDTO();
				tags.setTagIds(tagsId);
				tags.setProductIds(Arrays.asList(id));
				updateProductTags(tags);

			}

			if (keywords != null && !keywords.isEmpty()) {
				SeoKeywordsDTO seo = new SeoKeywordsDTO(SeoEntityType.PRODUCT, id, keywords);
				seoService.addSeoKeywords(seo);
			}
		}



		if (updatedImages != null && !updatedImages.isEmpty()) {
			for (Map<String, Long> imageInfo : updatedImages) {
				Long imageId = imageInfo.get("id");
				Long priority = imageInfo.get("priority");
				Optional<ProductImagesEntity> optionalImage = productImagesRepository.findById(imageId);
				if (optionalImage.isPresent()) {
					ProductImagesEntity image =optionalImage.get();
					image.setPriority(Math.toIntExact(priority));
					productImagesRepository.save(image);
				}
			}
		}

		if (deletedImages != null && deletedImages.length > 0) {
			for (Long imageId : deletedImages) {
				imgService.deleteImage(imageId, null, null);
			}
		}


		return new ProductUpdateResponse(id);
	}

@Override
public VariantUpdateResponse updateVariantV2(
			VariantUpdateDTO variant,
			MultipartFile[] imgs,
			Integer[] uploadedImagePriorities,
			List<Map<String, Long>> updatedImages,
			Long[] deletedImages
	) throws BusinessException {
		Long id = updateVariantBatch(asList(variant)).stream().findFirst().orElse(-1L);
		if (id != null) {

			Operation operation = variant.getOperation();
			if (imgs != null) {
				//Insert
				for (int i = 0; i < imgs.length; i++) {
					ProductImageUpdateDTO img = new ProductImageUpdateDTO();
					img.setOperation(Operation.CREATE);
					int priority = (uploadedImagePriorities != null && i < uploadedImagePriorities.length)
							? uploadedImagePriorities[i]
							: i + 1;
					img.setPriority(priority);
					img.setVariantId(id);
					img.setType(7);
					img.setProductId(variant.getProductId());
					imgService.updateProductImage(imgs[i], img);
				}
			}


			//Update
			if (updatedImages != null && !updatedImages.isEmpty()) {
				for (Map<String, Long> imageInfo : updatedImages) {
					Long imageId = imageInfo.get("id");
					Long priority = imageInfo.get("priority");
					Optional<ProductImagesEntity> optionalImage = productImagesRepository.findById(imageId);
					if (optionalImage.isPresent()) {
						ProductImagesEntity image =optionalImage.get();
						image.setPriority(Math.toIntExact(priority));
						productImagesRepository.save(image);
					}
				}
			}

			//DELETE
			if (deletedImages != null && deletedImages.length > 0) {
				for (Long imageId : deletedImages) {
					imgService.deleteImage(imageId, null, null);
				}
			}
		}

		return new VariantUpdateResponse(id);
	}

	@Transactional
	@Override
	public ProductDetailsDTO getProductData(ProductFetchDTO productFetchDTO) throws BusinessException {
		var id = ofNullable(productFetchDTO.getProductId()).orElse(-1L);
		var allowAll = !ofNullable(productFetchDTO.getOnlyYeshteryProducts()).orElse(false);
		ProductEntity product = productRepository.findByProductId(id, allowAll)
				.orElseThrow(() -> new RuntimeBusinessException(NOT_FOUND, P$PRO$0002, productFetchDTO.getProductId()));

		List<ProductVariantsEntity> productVariants = getProductVariants(product, productFetchDTO.isCheckVariants());

		return createNEWProductDetailsDTO(product, productFetchDTO.getShopId(), productVariants,
				productFetchDTO.isIncludeOutOfStock());
	}

	@Transactional
	@Override
	public ProductsResponse getOutOfStockProducts(ProductSearchParam requestParams) throws BusinessException {
		ProductSearchParam params = getProductSearchParams(requestParams);

		QProducts products = QProducts.products;
		QProductVariants productVariants = QProductVariants.productVariants;
		QStocks stocks = QStocks.stocks;
		QOrganizations organizations = QOrganizations.organizations;
		QShops shops = QShops.shops;

		BooleanBuilder predicate = getQueryPredicate(params, products, stocks, shops, productVariants, organizations);

		BooleanExpression outOfStockExpression = stocks.quantity.sum().eq(0).or(stocks.quantity.sum().isNull());

		SQLQuery<Long> countQuery = queryFactory.select(products.id.countDistinct()).from(products)
				.leftJoin(products._productVariantsProductIdFkey, productVariants)
				.leftJoin(productVariants._stocksVariantIdFkey, stocks)
				.leftJoin(products.productsOrganizationIdFkey, organizations)
				.leftJoin(shops).on(shops.id.eq(stocks.shopId))
				.groupBy(products.id)
				.where(predicate)
				.having(outOfStockExpression);
		long productsCount = countQuery.fetchCount();

		SQLQuery<Long> productsQuery = queryFactory.selectDistinct(products.id).from(products)
				.leftJoin(products._productVariantsProductIdFkey, productVariants)
				.leftJoin(productVariants._stocksVariantIdFkey, stocks)
				.leftJoin(products.productsOrganizationIdFkey, organizations)
				.leftJoin(shops).on(shops.id.eq(stocks.shopId))
				.groupBy(products.id)
				.where(predicate)
				.having(outOfStockExpression)
				.offset(params.start)
				.limit(params.count);

		Long[] productIds = productsQuery.fetch().toArray(Long[]::new);
		SQLQuery<Tuple> allProductQuery = queryFactory
				.select(products.all())
				.from(products)
				.where(products.id.in(productIds));

		List<ProductRepresentationObject> result = template.query(allProductQuery.getSQL().getSQL(),
				new BeanPropertyRowMapper<>(ProductRepresentationObject.class));
		return getProductResponseFromStocks(result, productsCount, params.include_out_of_stock);
	}


	private ProductDetailsDTO createNEWProductDetailsDTO(ProductEntity product, Long shopId,
			List<ProductVariantsEntity> productVariants, boolean includeOutOfStock) {
		List<ProductImageDTO> productsAndVariantsImages = getProductImageDTOS(product, productVariants);
		List<VariantDTO> variantsDTOList = createNEWVariantDTOS(shopId, productVariants, productsAndVariantsImages);
		List<TagsRepresentationObject> tagsDTOList = getProductTagsDTOList(product.getId());
		List<Long> product360Shops = product360ShopsRepo.findShopsByProductId(product.getId());

		ProductDetailsDTO productDTO = toProductDetailsDTO(product, includeOutOfStock);
		productDTO.setShops(product360Shops);
		productDTO.setImages(getProductImages(productsAndVariantsImages));
		productDTO.setVariants(variantsDTOList);
		productDTO.setMultipleVariants(hasMultipleVariants(variantsDTOList));
		productDTO.setVariantFeatures(getVariantFeatures(productVariants));
		productDTO.setBundleItems(getBundleItems(product));
		productDTO.setTags(tagsDTOList);
		productDTO.setKeywords(getSeoKeywords(product.getId(), SeoEntityType.PRODUCT, product.getOrganizationId()));
		

		return productDTO;
	}
	
	private List<VariantDTO> createNEWVariantDTOS(Long shopId, List<ProductVariantsEntity> productVariants,
			List<ProductImageDTO> productsAndVariantsImages) {
		List<VariantDTO> variantsDTOList = new ArrayList<>();
		if (!isNullOrEmpty(productVariants)) {
			variantsDTOList = getNewVariantsList(productVariants, shopId, productsAndVariantsImages);
		}
		return variantsDTOList;
	}
	
	private List<VariantDTO> getNewVariantsList(List<ProductVariantsEntity> productVariants, Long shopId,
			List<ProductImageDTO> variantsImages) {

		return productVariants.stream().map(variant -> createVariantDto(shopId, variant, variantsImages))

				.collect(toList());
	}
	
	List<SeoKeywordsDTO> getSeoKeywords(Long entityId, SeoEntityType type, Long orgId) {
		return seoService.getSeoKeywords(orgId, entityId, type);
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
	private Map<Long, BrandsEntity> brands;
}




@Data
@AllArgsConstructor
class ProductAndBrandPair{
	private Long productId;
	private Long brandId;
}

