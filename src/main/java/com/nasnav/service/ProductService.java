package com.nasnav.service;

import static com.nasnav.constatnts.error.product.ProductSrvErrorMessages.ERR_CANNOT_DELETE_BUNDLE_ITEM;
import static com.nasnav.constatnts.error.product.ProductSrvErrorMessages.ERR_CANNOT_DELETE_PRODUCT_BY_OTHER_ORG_USER;
import static com.nasnav.constatnts.error.product.ProductSrvErrorMessages.ERR_PRODUCT_DELETE_FAILED;
import static com.nasnav.constatnts.error.product.ProductSrvErrorMessages.ERR_PRODUCT_HAS_NO_DEFAULT_STOCK;
import static com.nasnav.constatnts.error.product.ProductSrvErrorMessages.ERR_PRODUCT_HAS_NO_VARIANTS;
import static com.nasnav.constatnts.error.product.ProductSrvErrorMessages.ERR_PRODUCT_READ_FAIL;
import static com.nasnav.constatnts.error.product.ProductSrvErrorMessages.ERR_PRODUCT_STILL_USED;
import static com.nasnav.constatnts.error.product.ProductSrvErrorMessages.ERR_CANNOT_DELETE_PRODUCT_USED_IN_NEW_ORDERS;
import static java.lang.String.format;
import static java.util.Comparator.comparing;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
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
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.beanutils.BeanUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
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
import com.nasnav.dao.CategoriesRepository;
import com.nasnav.dao.EmployeeUserRepository;
import com.nasnav.dao.ProductFeaturesRepository;
import com.nasnav.dao.ProductImagesRepository;
import com.nasnav.dao.ProductRepository;
import com.nasnav.dao.ProductVariantsRepository;
import com.nasnav.dao.StockRepository;
import com.nasnav.dto.BundleDTO;
import com.nasnav.dto.BundleElementUpdateDTO;
import com.nasnav.dto.ProductBaseInfo;
import com.nasnav.dto.ProductDetailsDTO;
import com.nasnav.dto.ProductImageUpdateDTO;
import com.nasnav.dto.ProductImgDTO;
import com.nasnav.dto.ProductRepresentationObject;
import com.nasnav.dto.ProductSortOptions;
import com.nasnav.dto.ProductUpdateDTO;
import com.nasnav.dto.ProductsResponse;
import com.nasnav.dto.StockDTO;
import com.nasnav.dto.VariantDTO;
import com.nasnav.dto.VariantFeatureDTO;
import com.nasnav.dto.VariantUpdateDTO;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.persistence.BaseUserEntity;
import com.nasnav.persistence.BrandsEntity;
import com.nasnav.persistence.BundleEntity;
import com.nasnav.persistence.ProductEntity;
import com.nasnav.persistence.ProductFeaturesEntity;
import com.nasnav.persistence.ProductImagesEntity;
import com.nasnav.persistence.ProductTypes;
import com.nasnav.persistence.ProductVariantsEntity;
import com.nasnav.persistence.StocksEntity;
import com.nasnav.request.BundleSearchParam;
import com.nasnav.response.BundleResponse;
import com.nasnav.response.ProductImageDeleteResponse;
import com.nasnav.response.ProductImageUpdateResponse;
import com.nasnav.response.ProductUpdateResponse;
import com.nasnav.response.VariantUpdateResponse;
import com.sun.istack.logging.Logger;

@Service
public class ProductService {

	private Logger logger = Logger.getLogger(ProductService.class);

	//	@Value("${products.default.start}")
	private Integer defaultStart = 0;
	//	@Value("${products.default.count}")
	private Integer defaultCount = 10;
	//	@Value("${products.default.sort.attribute}")
	private String defaultSortAttribute = "name";
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
	private CategoriesRepository categoriesRepo;


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

		Optional<ProductEntity> optionalProduct = productRepository.findById(productId);
		if (!optionalProduct.isPresent()) {
			return null;
		}

		ProductEntity product = optionalProduct.get();
		List<ProductVariantsEntity> productVariants = getProductVariants(product);

		return createProductDetailsDTO(product, shopId, productVariants);
	}

	private ProductDetailsDTO createProductDetailsDTO(ProductEntity product, Long shopId, List<ProductVariantsEntity> productVariants) throws BusinessException {

		List<VariantDTO> variantsDTOList = getVariantsList(productVariants, product.getId(), shopId);

		ProductDetailsDTO productDTO = null;
		try {
			productDTO = toProductDetailsDTO(product);
			productDTO.setVariants(variantsDTOList);
			if (variantsDTOList != null && variantsDTOList.size() > 1) {
				productDTO.setMultipleVariants(true);
			}			
			productDTO.setVariantFeatures( getVariantFeatures(productVariants) );
			productDTO.setBundleItems( getBundleItems(product));
			productDTO.setImages( getProductImages(product.getId() ) );
		} catch (IllegalAccessException | InvocationTargetException e) {
			logger.log(Level.SEVERE, e.getMessage(), e );
			throw new BusinessException(
					String.format(ERR_PRODUCT_READ_FAIL, product.getId())
					,"INTERNAL SERVER ERROR"
					, HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return productDTO;
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
		ProductsResponse response = this.getProductsResponse(bundleProducts,"asc" , "name" , 0, Integer.MAX_VALUE, null );
		List<ProductRepresentationObject> productRepList = response == null? new ArrayList<>() : response.getProducts();
		return productRepList;
	}


	private List<VariantDTO> getVariantsList(List<ProductVariantsEntity> productVariants, Long productId, Long shopId) throws BusinessException{

		return productVariants.stream()
				.map(variant -> createVariantDto(shopId, variant))
				.filter( variant -> !variant.getStocks().isEmpty())
				.collect(Collectors.toList());
	}




	private VariantDTO createVariantDto(Long shopId, ProductVariantsEntity variant)  {
		VariantDTO variantObj = new VariantDTO();
		variantObj.setId(variant.getId());
		variantObj.setBarcode( variant.getBarcode() );
		variantObj.setStocks( getStockList(variant, shopId) );
		variantObj.setVariantFeatures( getVariantFeaturesValues(variant) );
		variantObj.setImages( getProductVariantImages(variant.getId()) );
		return variantObj;
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
			features =  productVariants
					.stream()
					.filter(this::hasFeatures)
					.map(this::extractVariantFeatures)
					.flatMap(List::stream)
					.distinct()
					.collect(Collectors.toList());
		}

		return features;
	}




	private List<VariantFeatureDTO> extractVariantFeatures(ProductVariantsEntity variant){
		JacksonJsonParser parser = new JacksonJsonParser();
		Map<String, Object> keyValueMap =  parser.parseMap(variant.getFeatureSpec());
		return keyValueMap.keySet()
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
			return productImages.stream()
					.map(ProductImgDTO::new)
					.collect(Collectors.toSet());
		}
		return null;
	}



	private List<ProductImgDTO> getProductVariantImages(Long variantId) {
		List<ProductImagesEntity> variantImages = productImagesRepository.findByProductVariantsEntity_Id(variantId);

		List<ProductImgDTO> variantImagesArray = new ArrayList<>();
		if (variantImages != null && !variantImages.isEmpty()) {
			variantImagesArray = variantImages.stream()
					.filter(img-> img != null)
					.map(ProductImgDTO::new)
					.collect(Collectors.toList());
		}

		return variantImagesArray;
	}




	private List<StockDTO> getStockList(ProductVariantsEntity variant,Long shopId)  {

		List<StocksEntity> stocks = stockService.getVariantStockForShop(variant, shopId);

		return	stocks.stream()
				.filter(stock -> stock != null)
				.map(StockDTO::new)
				.collect(Collectors.toList());
	}





	public ProductsResponse getProductsResponseByShopId(Long shopId, Long categoryId, Long brandId, Integer start,
	                                                    Integer count, String sort, String order, String searchName) {

		if (start == null)
			start = defaultStart;
		if (count == null)
			count = defaultCount;

		if (sort == null)
			sort = defaultSortAttribute;

		if (order == null)
			order = defaultOrder;

		List<StocksEntity> stocks = stockRepository.findByShopsEntity_Id(shopId);
		List<ProductEntity> products = null;

		if (stocks != null && !stocks.isEmpty()) {

			List<Long> productsIds = stocks.stream()
											.map(StocksEntity::getProductVariantsEntity )
											.filter(Objects::nonNull)
											.map(ProductVariantsEntity::getProductEntity)
											.filter(Objects::nonNull)
											.map(ProductEntity::getId)
											.collect(Collectors.toList());

			if (categoryId == null && brandId == null) {
				products = getProductsByIds(productsIds, order, sort);
			} else if (categoryId != null && brandId == null){
				products = getProductsByIdsAndCategoryId(productsIds, categoryId, order, sort);
			} else if (categoryId == null && brandId != null){
				products = getProductsByIdsAndBrandId(productsIds, brandId, order, sort);
			} else {
				products = getProductsByIdsAndCategoryIdAndBrandId(productsIds, categoryId, brandId, order, sort);
			}

			if (searchName != null) {
				products = products.stream().filter(product -> product.getName().toLowerCase().contains(searchName.toLowerCase())).collect(Collectors.toList());
			}
		}
		
		return getProductsResponse(products, order, sort, start, count,shopId);
	}

	
	
	
	
	
	public ProductsResponse getProductsResponseByOrganizationId(Long organizationId, Long categoryId, Long brandId,
	                                                            Integer start, Integer count, String sort, String order,
	                                                            String searchName) {
		if (start == null)
			start = defaultStart;
		if (count == null)
			count = defaultCount;
		if (sort == null)
			sort = defaultSortAttribute;
		if (order == null)
			order = defaultOrder;

		List<ProductEntity> products = null;
		if (categoryId == null && brandId == null) {
			products = getProductsForOrganizationId(organizationId, order, sort);
		} else if (categoryId != null && brandId == null){
			products = getProductsForOrganizationIdAndCategoryId(organizationId, categoryId, order, sort);
		} else if (categoryId == null && brandId != null){
			products = getProductsForOrganizationIdAndBrandId(organizationId, brandId, order, sort);
		} else {
			products = getProductsForOrganizationIdAndCategoryIdAndBrandId(organizationId, categoryId, brandId, order, sort);
		}

		if (searchName != null) {
			products = products.stream().filter(product -> product.getName().toLowerCase().contains(searchName.toLowerCase())).collect(Collectors.toList());
		}

		return getProductsResponse(products, order, sort, start, count, null);
	}

	
	
	
	
	
	private List<ProductEntity> getProductsForOrganizationId(Long organizationId, String order, String sort) {

		List<ProductEntity> products = null;

		if (ProductSortOptions.getProductSortOptions(sort) == ProductSortOptions.ID) {
			if (order.equals("asc")) {
				products = productRepository.findByOrganizationIdOrderByIdAsc(organizationId);
			} else {
				products = productRepository.findByOrganizationIdOrderByIdDesc(organizationId);
			}
		} else if (ProductSortOptions.getProductSortOptions(sort) == ProductSortOptions.NAME) {

			if (order.equals("asc")) {
				products = productRepository.findByOrganizationIdOrderByNameAsc(organizationId);
			} else {
				products = productRepository.findByOrganizationIdOrderByNameDesc(organizationId);
			}
		} else if (ProductSortOptions.getProductSortOptions(sort) == ProductSortOptions.P_NAME) {
			if (order.equals("asc")) {
				products = productRepository.findByOrganizationIdOrderByPnameAsc(organizationId);
			} else {
				products = productRepository.findByOrganizationIdOrderByPnameDesc(organizationId);
			}
		} else {
			products = productRepository.findByOrganizationId(organizationId);
		}
		return products;
	}

	
	
	
	
	
	private List<ProductEntity> getProductsForOrganizationIdAndCategoryId(Long organizationId, Long categoryId,
	                                                                      String order, String sort) {
		List<ProductEntity> products = null;

		if (ProductSortOptions.getProductSortOptions(sort) == ProductSortOptions.ID) {
			if (order.equals("asc")) {
				products = productRepository.findByOrganizationIdAndCategoryIdOrderByIdAsc(organizationId, categoryId);
			} else {
				products = productRepository.findByOrganizationIdAndCategoryIdOrderByIdDesc(organizationId, categoryId);
			}
		} else if (ProductSortOptions.getProductSortOptions(sort) == ProductSortOptions.NAME) {

			if (order.equals("asc")) {
				products = productRepository.findByOrganizationIdAndCategoryIdOrderByNameAsc(organizationId,
						categoryId);
			} else {
				products = productRepository.findByOrganizationIdAndCategoryIdOrderByNameDesc(organizationId,
						categoryId);
			}
		} else if (ProductSortOptions.getProductSortOptions(sort) == ProductSortOptions.P_NAME) {
			if (order.equals("asc")) {
				products = productRepository.findByOrganizationIdAndCategoryIdOrderByPnameAsc(organizationId,
						categoryId);
			} else {
				products = productRepository.findByOrganizationIdAndCategoryIdOrderByPnameDesc(organizationId,
						categoryId);
			}
		} else {
			products = productRepository.findByOrganizationIdAndCategoryId(organizationId, categoryId);
		}
		return products;
	}

	
	
	
	
	
	private List<ProductEntity> getProductsForOrganizationIdAndBrandId(Long organizationId, Long brandId,
	                                                                   String order, String sort) {
		List<ProductEntity> products = null;

		if (ProductSortOptions.getProductSortOptions(sort) == ProductSortOptions.ID) {
			if (order.equals("asc")) {
				products = productRepository.findByOrganizationIdAndBrandIdOrderByIdAsc(organizationId, brandId);
			} else {
				products = productRepository.findByOrganizationIdAndBrandIdOrderByIdDesc(organizationId, brandId);
			}
		} else if (ProductSortOptions.getProductSortOptions(sort) == ProductSortOptions.NAME) {

			if (order.equals("asc")) {
				products = productRepository.findByOrganizationIdAndBrandIdOrderByNameAsc(organizationId,
						brandId);
			} else {
				products = productRepository.findByOrganizationIdAndBrandIdOrderByNameDesc(organizationId,
						brandId);
			}
		} else if (ProductSortOptions.getProductSortOptions(sort) == ProductSortOptions.P_NAME) {
			if (order.equals("asc")) {
				products = productRepository.findByOrganizationIdAndBrandIdOrderByPnameAsc(organizationId,
						brandId);
			} else {
				products = productRepository.findByOrganizationIdAndBrandIdOrderByPnameDesc(organizationId,
						brandId);
			}
		} else {
			products = productRepository.findByOrganizationIdAndBrandId(organizationId, brandId);
		}
		return products;
	}

	
	
	
	
	
	
	private List<ProductEntity> getProductsForOrganizationIdAndCategoryIdAndBrandId(Long organizationId,
	                                                                                Long categoryId, Long brandId, String order, String sort) {
		List<ProductEntity> products = null;

		if (ProductSortOptions.getProductSortOptions(sort) == ProductSortOptions.ID) {
			if (order.equals("asc")) {
				products = productRepository.findByOrganizationIdAndCategoryIdAndBrandIdOrderByIdAsc(organizationId,
						categoryId, brandId);
			} else {
				products = productRepository.findByOrganizationIdAndCategoryIdAndBrandIdOrderByIdDesc(organizationId,
						categoryId, brandId);
			}
		} else if (ProductSortOptions.getProductSortOptions(sort) == ProductSortOptions.NAME) {

			if (order.equals("asc")) {
				products = productRepository.findByOrganizationIdAndCategoryIdAndBrandIdOrderByNameAsc(organizationId,
						categoryId, brandId);
			} else {
				products = productRepository.findByOrganizationIdAndCategoryIdAndBrandIdOrderByNameDesc(organizationId,
						categoryId, brandId);
			}
		} else if (ProductSortOptions.getProductSortOptions(sort) == ProductSortOptions.P_NAME) {
			if (order.equals("asc")) {
				products = productRepository.findByOrganizationIdAndCategoryIdAndBrandIdOrderByPnameAsc(organizationId,
						categoryId, brandId);
			} else {
				products = productRepository.findByOrganizationIdAndCategoryIdAndBrandIdOrderByPnameDesc(organizationId,
						categoryId, brandId);
			}
		} else {
			products = productRepository.findByOrganizationIdAndCategoryIdAndBrandId(organizationId, categoryId,
					brandId);
		}
		return products;
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

	
	
	
	
	
	
	
	
	
	
	
	private List<ProductEntity> getProductsByIdsAndCategoryId(List<Long> productsIds, Long categoryId, String order,
	                                                          String sort) {
		List<ProductEntity> products = null;

		if (ProductSortOptions.getProductSortOptions(sort) == ProductSortOptions.ID) {
			if (order.equals("asc")) {
				products = productRepository.findByIdInAndCategoryIdOrderByIdAsc(productsIds, categoryId);
			} else {
				products = productRepository.findByIdInAndCategoryIdOrderByIdDesc(productsIds, categoryId);
			}
		} else if (ProductSortOptions.getProductSortOptions(sort) == ProductSortOptions.NAME) {
			if (order.equals("asc")) {
				products = productRepository.findByIdInAndCategoryIdOrderByNameAsc(productsIds, categoryId);
			} else {
				products = productRepository.findByIdInAndCategoryIdOrderByNameDesc(productsIds, categoryId);
			}
		} else if (ProductSortOptions.getProductSortOptions(sort) == ProductSortOptions.P_NAME) {
			if (order.equals("asc")) {
				products = productRepository.findByIdInAndCategoryIdOrderByPnameAsc(productsIds, categoryId);
			} else {
				products = productRepository.findByIdInAndCategoryIdOrderByPnameDesc(productsIds, categoryId);
			}
		} else {
			products = productRepository.findByIdInAndCategoryId(productsIds, categoryId);
		}
		return products;
	}

	
	
	
	
	
	
	
	private List<ProductEntity> getProductsByIdsAndBrandId(List<Long> productsIds, Long brandId, String order,
	                                                       String sort) {
		List<ProductEntity> products = null;

		if (ProductSortOptions.getProductSortOptions(sort) == ProductSortOptions.ID) {
			if (order.equals("asc")) {
				products = productRepository.findByIdInAndBrandIdOrderByIdAsc(productsIds, brandId);
			} else {
				products = productRepository.findByIdInAndBrandIdOrderByIdDesc(productsIds, brandId);
			}
		} else if (ProductSortOptions.getProductSortOptions(sort) == ProductSortOptions.NAME) {
			if (order.equals("asc")) {
				products = productRepository.findByIdInAndBrandIdOrderByNameAsc(productsIds, brandId);
			} else {
				products = productRepository.findByIdInAndBrandIdOrderByNameDesc(productsIds, brandId);
			}
		} else if (ProductSortOptions.getProductSortOptions(sort) == ProductSortOptions.P_NAME) {
			if (order.equals("asc")) {
				products = productRepository.findByIdInAndBrandIdOrderByPnameAsc(productsIds, brandId);
			} else {
				products = productRepository.findByIdInAndBrandIdOrderByPnameDesc(productsIds, brandId);
			}
		} else {
			products = productRepository.findByIdInAndBrandId(productsIds, brandId);
		}
		return products;
	}

	
	
	
	
	
	
	
	private List<ProductEntity> getProductsByIdsAndCategoryIdAndBrandId(List<Long> productsIds, Long categoryId,
	                                                                    Long brandId, String order, String sort) {
		List<ProductEntity> products = null;

		if (ProductSortOptions.getProductSortOptions(sort) == ProductSortOptions.ID) {
			if (order.equals("asc")) {
				products = productRepository.findByIdInAndCategoryIdAndBrandIdOrderByIdAsc(productsIds, categoryId, brandId);
			} else {
				products = productRepository.findByIdInAndCategoryIdAndBrandIdOrderByIdDesc(productsIds, categoryId, brandId);
			}
		} else if (ProductSortOptions.getProductSortOptions(sort) == ProductSortOptions.NAME) {
			if (order.equals("asc")) {
				products = productRepository.findByIdInAndCategoryIdAndBrandIdOrderByNameAsc(productsIds, categoryId,
						brandId);
			} else {
				products = productRepository.findByIdInAndCategoryIdAndBrandIdOrderByNameDesc(productsIds, categoryId,
						brandId);
			}
		} else if (ProductSortOptions.getProductSortOptions(sort) == ProductSortOptions.P_NAME) {
			if (order.equals("asc")) {
				products = productRepository.findByIdInAndCategoryIdAndBrandIdOrderByPnameAsc(productsIds, categoryId,
						brandId);
			} else {
				products = productRepository.findByIdInAndCategoryIdAndBrandIdOrderByPnameDesc(productsIds, categoryId,
						brandId);
			}
		} else {
			products = productRepository.findByIdInAndCategoryIdAndBrandId(productsIds, categoryId, brandId);
		}
		return products;
	}


	
	
	
	
	

	private ProductsResponse getProductsResponse(List<ProductEntity> products, String order, String sort, Integer start,
	                                             Integer count, Long shopId) {		
		if(products == null) {
			return new ProductsResponse();
		}
		
		List<Long> productIdList = products.stream()
											.map(ProductEntity::getId)
											.collect(Collectors.toList());
				
		List<ProductEntity> productsFullData = new ArrayList<>();
		if(!productIdList.isEmpty()) {
			productsFullData.addAll( productRepository.findFullDataByIdIn(productIdList) );
		}
		
		Map<Long, String> productCoverImages = imgService.getProductsCoverImages(productIdList);

		List<ProductRepresentationObject> productsRep = 
				productsFullData.stream()
								.map(prod -> getProductRepresentation(prod, productCoverImages))
								.collect(Collectors.toList());

		if (ProductSortOptions.getProductSortOptions(sort) == ProductSortOptions.PRICE) {			
			sortByPrice(productsRep, order);
		}
		
		return createProductResposneObj(start, count, productsRep);
	}


	

	

	private void sortByPrice(List<ProductRepresentationObject> productsRep, String order) {
		if (order.equals("desc")) {
			Collections.sort(productsRep, comparing(ProductRepresentationObject::getPrice).reversed() );				
		} else {
			Collections.sort(productsRep, comparing(ProductRepresentationObject::getPrice));
		}
	}




	
	private ProductsResponse createProductResposneObj(Integer start, Integer count,
			List<ProductRepresentationObject> productsRep) {
		ProductsResponse productsResponse = new ProductsResponse();
		int lastProductIndex = productsRep.size() - 1;
		if (start > lastProductIndex) {
			// TODO proper start handling(maybe default) or error massage
			return null;
		}
		int toIndex = start + count;
		if (toIndex > lastProductIndex)
			toIndex = productsRep.size();

		productsResponse.setProducts(productsRep.subList(start, toIndex));
		productsResponse.setTotal((long) productsRep.size());

		return productsResponse;
	}

	
	
	
	
	
	private ProductRepresentationObject setAdditionalInfo(ProductRepresentationObject product, Map<Long, String> productCoverImgs) {
		String imgUrl = productCoverImgs.get(product.getId());
		product.setImageUrl(imgUrl);
		
		return product;
	}

	
	
	
	
	private StocksEntity getDefaultProductStock(Map.Entry<Long,List<StocksEntity>> productStocks){
		return productStocks.getValue()
							.stream()
							.min( comparing(StocksEntity::getPrice))
							.orElseThrow(() -> new IllegalStateException(format(ERR_PRODUCT_HAS_NO_DEFAULT_STOCK, productStocks.getKey())));
	}
	
	
	
	
	private Optional<StocksEntity> getDefaultProductStock(ProductEntity product) {
		return Optional.ofNullable(product)
						.map(ProductEntity::getProductVariants)
						.map(Set::stream)
						.map(s -> s.filter(var -> !var.getStocks().isEmpty()))
						.map(s -> s.flatMap( variant -> variant.getStocks().stream()))
						.flatMap(s -> s.min( comparing(StocksEntity::getPrice)));
	}
	
	
	
	
	
	
	
	private List getProductsMinPrices(List<ProductRepresentationObject> productsRep, List<Long> productIdList, Long shopId) {
		List<ProductVariantsEntity> productsVariants = productVariantsRepository.findByProductEntity_IdIn(productIdList);
		for (ProductRepresentationObject obj : productsRep) {
			List<ProductVariantsEntity> productVariants = productsVariants.stream()
					.filter(variant -> variant.getProductEntity().getId().equals(obj.getId()))
					.collect(Collectors.toList());
			if (productVariants.isEmpty()) {
				obj.setAvailable(false);
				obj.setHidden(true);
			}
			else {
				if (productVariants.size() > 1)
					obj.setMultipleVariants(true);
				else {
					List<StocksEntity> productStocks;
					if (shopId != null)
						productStocks = stockRepository.findByProductVariantsEntityIdAndShopsEntityIdOrderByPriceAsc(productVariants.get(0).getId(), shopId);
					else
						productStocks = stockRepository.findByProductVariantsEntityIdOrderByPriceAsc(productVariants.get(0).getId());
					if(!productStocks.isEmpty()) {
						obj.setPrice(productStocks.get(0).getPrice());
						obj.setDiscount(productStocks.get(0).getDiscount());
						if (productStocks.get(0).getCurrency() != null)
							obj.setCurrency(productStocks.get(0).getCurrency().ordinal());
						obj.setStockId(productStocks.get(0).getId());
					}
					else
						obj.setAvailable(false);
				}
			}
		}
		return productsRep;
	}


	
	
	
	
	
	
	
	
	private void setProductAvailability(List<StocksEntity> stocks, ProductRepresentationObject productRep) {
		stocks.stream()
				.filter(Objects::nonNull)
				.filter(stock -> Objects.equals( getStockProductId(stock),  productRep.getId()))
				.findFirst()
				.ifPresent(s -> productRep.setAvailable(true));
	}
	
	
	
	

	private Long getStockProductId(StocksEntity stock) {
		return Optional.ofNullable(stock)
						.map(StocksEntity::getProductVariantsEntity)
						.map(ProductVariantsEntity::getProductEntity)
						.map(ProductEntity::getId)
						.orElse(0L);
	}

	
	
	
	
	

	public ProductUpdateResponse updateProduct(String productJson, Boolean isBundle) throws BusinessException {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		BaseUserEntity user =  empRepo.getOneByEmail(auth.getName());

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

		JsonNode categoryId = productJson.path("category_id");
		JsonNode brandId = productJson.path("brand_id");
		validateCategoryId(categoryId);
		validateBrandId(user, brandId);
	}

	
	



	private void validateProductDtoToUpdate(JsonNode productJson, BaseUserEntity user)
			throws BusinessException {
		JsonNode id = productJson.path("product_id");
		JsonNode categoryId = productJson.path("category_id");
		JsonNode brandId = productJson.path("brand_id");

		if(id.isMissingNode())
			throw new BusinessException("No product id provided!", "INVALID_PARAM:product_id" , HttpStatus.NOT_ACCEPTABLE);

		if(!id.isNull() && !productRepository.existsById(id.asLong()))
			throw new BusinessException("No prodcut exists with ID: "+ id + " !", "INVALID_PARAM:product_id" , HttpStatus.NOT_ACCEPTABLE);

		if(!categoryId.isMissingNode() )
			validateCategoryId(categoryId);

		if(!brandId.isMissingNode() )
			validateBrandId(user, brandId);
	}


	
	


	private void checkCreateProuctReqParams(JsonNode productJson)
			throws BusinessException {
		JsonNode name = productJson.path("name");
		JsonNode categoryId = productJson.path("category_id");
		JsonNode brandId = productJson.path("brand_id");

		if(name.isMissingNode())
			throw new BusinessException("Product name Must be provided! ", "MISSING_PARAM:name" , HttpStatus.NOT_ACCEPTABLE);

		if( name.isNull() )
			throw new BusinessException("Product name cannot be Null ", "MISSING_PARAM:name" , HttpStatus.NOT_ACCEPTABLE);

		if(categoryId.isMissingNode())
			throw new BusinessException("category_id Must be provided! ", "MISSING_PARAM:category_id" , HttpStatus.NOT_ACCEPTABLE);

		if(categoryId.isNull() )
			throw new BusinessException("category_id cannot be Null!" , "MISSING_PARAM:category_id" , HttpStatus.NOT_ACCEPTABLE);

		if(brandId.isMissingNode())
			throw new BusinessException("Brand Id Must be provided!" , "MISSING_PARAM:brand_Id" , HttpStatus.NOT_ACCEPTABLE);
	}




	private void validateCategoryId(JsonNode categoryId) throws BusinessException {
		if(categoryId.isMissingNode())
			return ;

		if(categoryId.isNull())
			throw new BusinessException("category_id cannot be Null!" , "MISSING_PARAM:category_id" , HttpStatus.NOT_ACCEPTABLE);

		long id = categoryId.asLong();
		if(!categoriesRepo.existsById(id) )
			throw new BusinessException("No Category exists with ID: " + id + " !" , "INVALID_PARAM:category_id" , HttpStatus.NOT_ACCEPTABLE);
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

	
	
	


	public ProductUpdateResponse deleteProduct(Long productId) throws BusinessException {
		
		if(!productRepository.existsById(productId)) {
			return new ProductUpdateResponse(true, productId); //if the product doesn't exists, then..mission accomplished!
		}
		
		validateProductToDelete(productId);

		List<ProductImagesEntity> imgs = productImagesRepository.findByProductEntity_Id(productId) ;
		try {
			transactions.deleteProduct(productId);
		}catch(DataIntegrityViolationException e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
			throw new BusinessException( format(ERR_PRODUCT_STILL_USED, productId), "INVAILID PARAM:product_id", HttpStatus.NOT_ACCEPTABLE);
		}catch(Throwable e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
			throw new BusinessException( format(ERR_PRODUCT_DELETE_FAILED , productId), "INVAILID PARAM:product_id", HttpStatus.INTERNAL_SERVER_ERROR);
		}

		for(ProductImagesEntity img : imgs) {
			fileService.deleteFileByUrl(img.getUri());
		};

		return new ProductUpdateResponse(true, productId);
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



	public ProductUpdateResponse deleteBundle(Long bundleId) throws BusinessException {
		validateBundleToDelete(bundleId);

		List<StocksEntity> bundleStocks = stockRepository.findByProductIdIn(Arrays.asList(bundleId));
		try {
			bundleStocks.forEach(stockRepository::delete);
		}catch(DataIntegrityViolationException e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
			throw new BusinessException(
					String.format("Failed to delete bundle with id[%d]! bundle is still used in the system (stocks, orders, bundles, ...)!", bundleId)
					, "INVAILID PARAM:product_id"
					, HttpStatus.FORBIDDEN);
		}catch(Throwable e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
			throw new BusinessException(
					String.format("Failed to delete bundle with id[%d]!", bundleId)
					, "INVAILID PARAM:product_id"
					, HttpStatus.INTERNAL_SERVER_ERROR);
		}


		return deleteProduct(bundleId);
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

		if(params.getCategory_id() != null)
			predicates.add( builder.equal(root.get("categoryId"), params.getCategory_id() ));


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
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		BaseUserEntity user =  empRepo.getOneByEmail(auth.getName());
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

		if( opr.equals( Operation.UPDATE)) {
			entity = productVariantsRepository.findById( variant.getVariantId()).get();
		}


		if(variant.isUpdated("productId")){
			ProductEntity product = productRepository.findById( variant.getProductId() ).get();
			entity.setProductEntity(product);
		}

		if(variant.isUpdated("name")){
			entity.setName( variant.getName());
		}

		setPnameOrGenerateDefault(variant, entity, opr);

		if(variant.isUpdated("description")) {
			entity.setDescription( variant.getDescription() );
		}

		if(variant.isUpdated("barcode")) {
			entity.setBarcode( variant.getBarcode() );
		}

		if(variant.isUpdated("features")) {
			entity.setFeatureSpec( variant.getFeatures() );
		}

		entity = productVariantsRepository.save(entity);

		return entity;
	}




	private void setPnameOrGenerateDefault(VariantUpdateDTO variant, ProductVariantsEntity entity,
	                                       Operation opr) {

		if(variant.isUpdated("pname") && !StringUtils.isBlankOrNull( variant.getPname()) ) {
			entity.setPname(variant.getPname() );
		}else if(opr.equals( Operation.CREATE )){
			String defaultPname = createPnameFromVariantFeatures(variant);
			entity.setPname(defaultPname);
		}

	}




	private String createPnameFromVariantFeatures(VariantUpdateDTO variant) {
		JSONObject json = new JSONObject(variant.getFeatures());

		StringBuilder pname = new StringBuilder();
		for(String key: json.keySet()) {
			String featureName = getProductFeatureName(key);
			String value = json.get(key).toString();

			if(pname.length() != 0)
				pname.append("-");

			String toAppend = featureName + "-"+value;
			pname.append(StringUtils.encodeUrl(toAppend));
		}


		String defaultPname = StringUtils.encodeUrl(pname.toString());
		return defaultPname;
	}




	private String getProductFeatureName(String idAsStr) {
		return Optional.ofNullable(idAsStr)
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
	  productRep.setId( product.getId());
	  productRep.setName(product.getName());
	  productRep.setPname( product.getPname());
	  productRep.setCategoryId( product.getCategoryId());
	  productRep.setBrandId( product.getBrandId());
	  productRep.setBarcode( product.getBarcode());
	  productRep.setMultipleVariants( product.getProductVariants().size() > 1);      
	  
	  
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
	productRep.setAvailable(stockExists);
	productRep.setHidden(!stockExists);  
  
	return productRep;
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
}
