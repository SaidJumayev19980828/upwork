package com.nasnav.service;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.apache.commons.beanutils.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Objects;
import com.nasnav.commons.utils.StringUtils;
import com.nasnav.constatnts.EntityConstants.Operation;
import com.nasnav.dao.BrandsRepository;
import com.nasnav.dao.BundleRepository;
import com.nasnav.dao.CategoriesRepository;
import com.nasnav.dao.EmployeeUserRepository;
import com.nasnav.dao.ProductFeaturesRepository;
import com.nasnav.dao.ProductImagesRepository;
import com.nasnav.dao.ProductRepository;
import com.nasnav.dao.ProductVariantsRepository;
import com.nasnav.dao.StockRepository;
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
import com.nasnav.exceptions.BusinessException;
import com.nasnav.persistence.BaseUserEntity;
import com.nasnav.persistence.BrandsEntity;
import com.nasnav.persistence.ProductEntity;
import com.nasnav.persistence.ProductFeaturesEntity;
import com.nasnav.persistence.ProductImagesEntity;
import com.nasnav.persistence.ProductVariantsEntity;
import com.nasnav.persistence.StocksEntity;
import com.nasnav.response.ProductImageDeleteResponse;
import com.nasnav.response.ProductImageUpdateResponse;
import com.nasnav.response.ProductUpdateResponse;
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

	private final StockServiceImpl stockService;
	
	@Autowired
	private  FileService fileService;
	
	
	@Autowired
	private EmployeeUserRepository empRepo;
	
	@Autowired
	private CategoriesRepository categoriesRepo;
	
	
	@Autowired
	private BrandsRepository brandRepo;

	@Autowired
	public ProductService(ProductRepository productRepository, StockRepository stockRepository,
	                      ProductVariantsRepository productVariantsRepository, ProductImagesRepository productImagesRepository,
	                      ProductFeaturesRepository productFeaturesRepository , BundleRepository bundleRepository,
	                      StockServiceImpl stockService) {
		this.productRepository = productRepository;
		this.stockRepository = stockRepository;
		this.productImagesRepository = productImagesRepository;
		this.productVariantsRepository = productVariantsRepository;
		this.productFeaturesRepository = productFeaturesRepository;
		this.bundleRepository = bundleRepository;
		this.stockService = stockService;
	}




	public ProductDetailsDTO getProduct(Long productId, Long shopId){

		Optional<ProductEntity> optionalProduct = productRepository.findById(productId);
		if (!optionalProduct.isPresent()) {
			return null;
		}

		ProductEntity product = optionalProduct.get();

		List<ProductVariantsEntity> productVariants = productVariantsRepository.findByProductEntity_Id(productId);

		List<VariantDTO> variantsDTOList;
		if (productVariants != null && !productVariants.isEmpty()) {
			variantsDTOList = getVariantsList(productVariants, productId, shopId);
		} else {
			variantsDTOList = createDummyVariantWithProductStocks(productId, shopId);
		}

		ProductDetailsDTO productDTO = new ProductDetailsDTO(product);
		productDTO.setVariants(variantsDTOList);
		productDTO.setVariantFeatures( getVariantFeatures(productVariants) );
		productDTO.setBundleItems( getBundleItems(product));
		productDTO.setImages( getProductImages(productId) );

		return productDTO;
	}

	private List<VariantDTO> createDummyVariantWithProductStocks(Long productId, Long shopId) {
		List<VariantDTO> variantsDTOList = new ArrayList<>();

		List<StockDTO> productStocks = getStockList(productId, shopId, null);
		if (productStocks != null) {
			VariantDTO variantObj = new VariantDTO();
			variantObj.setId(0L);
			variantObj.setStocks(productStocks);
			variantsDTOList.add(variantObj);
		}

		return variantsDTOList;
	}


	private List<ProductRepresentationObject> getBundleItems(ProductEntity product) {

		List<Long> bundleProductsIdList = bundleRepository.GetBundleItemsProductIds(product.getId());
		List<ProductEntity> bundleProducts = this.getProductsByIds(bundleProductsIdList , "asc", "name");
		ProductsResponse response = this.getProductsResponse(bundleProducts,"asc" , "name" , 0, Integer.MAX_VALUE );
		List<ProductRepresentationObject> productRepList = response == null? new ArrayList<>() : response.getProducts();
		return productRepList;
	}




    private List<VariantDTO> getVariantsList(List<ProductVariantsEntity> productVariants, Long productId, Long shopId) {

		List<VariantDTO> variantDTOList = new ArrayList<>();

		productVariants.forEach(variant -> {

			VariantDTO variantObj = new VariantDTO();
			variantObj.setId(variant.getId());
			variantObj.setBarcode( variant.getBarcode() );
			variantObj.setStocks( getStockList(productId, shopId, variant.getId()));
			variantObj.setVariantFeatures( getVariantFeaturesValues(variant) );
			variantObj.setImages( getProductVariantImages(variant.getId()) );

			variantDTOList.add(variantObj);
		});

		return variantDTOList;
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
				.collect(Collectors.toMap(Map.Entry::getKey , Map.Entry::getValue))
				;
	}

	private Map.Entry<String,String> getVariantFeatureMapEntry(Map.Entry<String,Object> entry) {
		if(entry == null || entry.getKey() == null)
			return null;

		Integer id = Integer.parseInt(entry.getKey());
		Optional<ProductFeaturesEntity> opt = productFeaturesRepository.findById(id);
		if(!opt.isPresent())
			return null;

		return new AbstractMap.SimpleEntry<>(
				opt.get().getName()
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

	private List<ProductImgDTO> getProductImages(Long productId) {

		List<ProductImagesEntity> productImages = productImagesRepository.findByProductEntity_Id(productId);

		if (productImages != null && !productImages.isEmpty()) {
			return productImages.stream()
					.map(ProductImgDTO::new)
					.collect(Collectors.toList());
		}
		return null;
	}



	private List<ProductImgDTO> getProductVariantImages(Long variantId) {
		List<ProductImagesEntity> variantImages = productImagesRepository.findByProductVariantsEntity_Id(variantId);

		List<ProductImgDTO> variantImagesArray = null;
		if (variantImages != null && !variantImages.isEmpty()) {
			variantImagesArray = variantImages.stream()
					.filter(img-> img != null)
					.map(ProductImgDTO::new)
					.collect(Collectors.toList());
		}

		return variantImagesArray;
	}



	private List<StockDTO> getStockList(Long productId, Long shopId, Long variantId) {
		List<StockDTO> stocksArray = null;

		if (shopId == null) {
			return stocksArray;
		}

		List<StocksEntity> stocks;
		if (variantId != null) {
			stocks = stockRepository.findByProductEntity_IdAndShopsEntity_IdAndProductVariantsEntity_Id(productId,
					shopId, variantId);
		} else {
			stocks = getProductStockForShop(productId, shopId);
		}

		if (stocks != null && !stocks.isEmpty()) {
			stocksArray = stocks.stream()
					.filter(stock -> stock != null)
					.map(stock -> new StockDTO(stock,shopId))
					.collect(Collectors.toList());
		}

		return stocksArray;
	}




	private List<StocksEntity> getProductStockForShop(Long productId, Long shopId) {

		return stockService.getProductStockForShop(productId, shopId);

	}








	public ProductsResponse getProductsResponseByShopId(Long shopId, Long categoryId, Long brandId, Integer start,
	                                                    Integer count, String sort, String order) {

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

			List<Long> productsIds = stocks.stream().filter(stock -> stock.getProductEntity() != null)
					.map(stock -> stock.getProductEntity().getId()).collect(Collectors.toList());

			if (categoryId == null && brandId == null) {
				products = getProductsByIds(productsIds, order, sort);
			} else if (categoryId != null && brandId == null){
				products = getProductsByIdsAndCategoryId(productsIds, categoryId, order, sort);
			} else if (categoryId == null && brandId != null){
				products = getProductsByIdsAndBrandId(productsIds, brandId, order, sort);
			} else {
				products = getProductsByIdsAndCategoryIdAndBrandId(productsIds, categoryId, brandId, order, sort);
			}
		}
		return getProductsResponse(products, order, sort, start, count);

	}

	public ProductsResponse getProductsResponseByOrganizationId(Long organizationId, Long categoryId, Long brandId,
	                                                            Integer start, Integer count, String sort, String order) {
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

		return getProductsResponse(products, order, sort, start, count);
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
	                                             Integer count) {

		ProductsResponse productsResponse = null;
		if (products != null) {
			productsResponse = new ProductsResponse();

			List<StocksEntity> stocks = stockRepository.findByProductEntity_IdIn(
					products.stream().map(product -> product.getId()).collect(Collectors.toList()));

			List<ProductRepresentationObject> productsRep = products.stream()
					.map(product -> (ProductRepresentationObject) product.getRepresentation())
					.collect(Collectors.toList());
			productsRep.forEach(pRep -> {

				Optional<StocksEntity> optionalStock = stocks.stream().filter(stock -> stock != null
						&& stock.getProductEntity() != null && stock.getProductEntity().getId().equals(pRep.getId()))
						.findFirst();

				if (optionalStock != null && optionalStock.isPresent()) {
					pRep.setAvailable(true);
					pRep.setPrice(optionalStock.get().getPrice());
				} else {
					pRep.setAvailable(false);
				}

			});

			if (ProductSortOptions.getProductSortOptions(sort) == ProductSortOptions.PRICE) {

				if (order.equals("desc")) {

					Collections.sort(productsRep, new Comparator<ProductRepresentationObject>() {
						@Override
						public int compare(ProductRepresentationObject p1, ProductRepresentationObject p2) {
							if (p1.getPrice() == null)
								return -1;
							if (p2.getPrice() == null)
								return 1;
							return Double.compare(p1.getPrice().doubleValue(), p2.getPrice().doubleValue());
						}
					});

				} else {

					Collections.sort(productsRep, new Comparator<ProductRepresentationObject>() {
						@Override
						public int compare(ProductRepresentationObject p1, ProductRepresentationObject p2) {
							if (p1.getPrice() == null)
								return 1;
							if (p2.getPrice() == null)
								return -1;
							return Double.compare(p2.getPrice().doubleValue(), p1.getPrice().doubleValue());
						}
					});
				}
			}
			int lastProductIndex = productsRep.size() - 1;
			if (start > lastProductIndex) {
				// TODO proper start handling(maybe default) or error massage
				return null;
			}
			int toIndex = start + count;
			if (toIndex > lastProductIndex)
				toIndex = productsRep.size();

			productsResponse.setProducts(productsRep.subList(start, toIndex));
			productsResponse.setTotal(stockService.getStockItemsQuantitySum(stocks));
		}

		return productsResponse;
	}
	
	
	
	
	public ProductUpdateResponse updateProduct(String productJson) throws BusinessException {
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
		
		ProductEntity entity = prepareProdcutEntity(rootNode, user);
		ProductEntity saved = productRepository.save(entity);
		
		return new ProductUpdateResponse(true, saved.getId());		
	}




	private ProductEntity prepareProdcutEntity(JsonNode productJsonNode, BaseUserEntity user)
			throws BusinessException {
		
		Long id = productJsonNode.path("product_id").asLong();
		JsonNode operationNode = productJsonNode.path("operation");	
		Operation operation = Operation.valueOf(operationNode.asText().toUpperCase());
		
		ProductEntity entity;
		
		if(Operation.CREATE.equals(operation))
			entity = new ProductEntity();	
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
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		BaseUserEntity user =  empRepo.getOneByEmail(auth.getName());
		Long userOrgId = user.getOrganizationId();
		
		productRepository.findById(productId)
					.filter(p -> p.getOrganizationId().equals(userOrgId) )
					.orElseThrow(() -> new BusinessException(
												"Product of ID["+productId+"] cannot be deleted by a user from oraganization of id ["+ userOrgId + "]" 
												, "INSUFFICIENT_RIGHTS"
												, HttpStatus.FORBIDDEN));
		
		productRepository.deleteById(productId);
		return new ProductUpdateResponse(true, productId);
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
					String.format("Missing required parameters! required parameters for updating existing image are: %s", imgMetaData.getRequiredPropertiesForDataUpdate())
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
		
		validateProductVariant(imgMetaData, productId);
			
	}




	private void validateProductVariant(ProductImageUpdateDTO imgMetaData, Long productId) throws BusinessException {
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
		
		if(!Objects.equal(userOrg, productOrg))
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
		
		productImagesRepository.deleteById(imgId);
		
		fileService.deleteFileByUrl(img.getUri());
		
		return new ProductImageDeleteResponse(productId);
	}

}
