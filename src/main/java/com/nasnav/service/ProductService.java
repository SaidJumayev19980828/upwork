package com.nasnav.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.stereotype.Service;

import com.nasnav.dao.BundleRepository;
import com.nasnav.dao.ProductFeaturesRepository;
import com.nasnav.dao.ProductImagesRepository;
import com.nasnav.dao.ProductRepository;
import com.nasnav.dao.ProductVariantsRepository;
import com.nasnav.dao.StockRepository;
import com.nasnav.dto.ProductDetailsDTO;
import com.nasnav.dto.ProductImgDTO;
import com.nasnav.dto.ProductRepresentationObject;
import com.nasnav.dto.ProductSortOptions;
import com.nasnav.dto.ProductsResponse;
import com.nasnav.dto.VariantFeatureDTO;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.persistence.ProductEntity;
import com.nasnav.persistence.ProductFeaturesEntity;
import com.nasnav.persistence.ProductImagesEntity;
import com.nasnav.persistence.ProductVariantsEntity;
import com.nasnav.persistence.StocksEntity;

@Service
public class ProductService {


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




	public ProductDetailsDTO getProduct(Long productId, Long shopId) throws BusinessException {

		Optional<ProductEntity> optionalProduct = productRepository.findById(productId);
		if (optionalProduct == null || !optionalProduct.isPresent()) {
			return null;
		}
		ProductEntity product = optionalProduct.get();
		ProductDetailsDTO response = new ProductDetailsDTO(product);
		
		
		response.setImages( getProductImages(productId) );
		

		List<ProductVariantsEntity> productVariants = productVariantsRepository.findByProductEntity_Id(productId);

		
		
		JSONArray variantJsonArray = null;
		
		if (productVariants != null && !productVariants.isEmpty()) {
			variantJsonArray = getVariantsJSONArray(productVariants, productId, shopId);
		} else {
			JSONArray stockArray = getStockJsonArray(productId, shopId, null);
			if (stockArray != null) {
				JSONObject variantObj = new JSONObject();
				variantObj.put("id", 0);
				variantObj.put("stocks", stockArray);
				variantJsonArray = new JSONArray();
				variantJsonArray.put(variantObj);
			}
		}
		
		
		response.setVariantFeatures( getVariantFeatures(productVariants) );
		
		
		if (variantJsonArray!=null && !variantJsonArray.isEmpty()) {
			response.put("variants", variantJsonArray);
		}
		
		response.setBundleItems( getBundleItems(product));
        

		return response;
	}



    private List<ProductRepresentationObject> getBundleItems(ProductEntity product) {
    	
        List<Long> bundleProductsIdList = bundleRepository.GetBundleItemsProductIds(product.getId());
        List<ProductEntity> bundleProducts = this.getProductsByIds(bundleProductsIdList , "asc", "name");
        ProductsResponse response = this.getProductsResponse(bundleProducts,"asc" , "name" , 0, Integer.MAX_VALUE );
        List<ProductRepresentationObject> productRepList = response == null? new ArrayList<>() : response.getProducts();
        return productRepList;
    }




    private JSONArray getVariantsJSONArray(List<ProductVariantsEntity> productVariants, Long productId, Long shopId) {

		JSONArray variantJsonArray = new JSONArray();

		productVariants.forEach(variant -> {

			JSONObject variantObj = new JSONObject();
			variantObj.put("id", variant.getId());
			variantObj.put("barcode", variant.getBarcode());

			variantObj.put("stocks", getStockJsonArray(productId, shopId, variant.getId()));

			setProductFeatureNameAndValue(variant, variantObj);

			JSONArray variantImages = getProductVariantImages(variant.getId());
			if (variantImages != null && !variantImages.isEmpty()) {
				variantObj.put("images", variantImages);
			}
			variantJsonArray.put(variantObj);
		});

		return variantJsonArray;
	}

	private void setProductFeatureNameAndValue(ProductVariantsEntity variant, JSONObject variantObj) {

		if (hasFeatures(variant)) {

			String[] keyValueVariant = variant.getFeatureSpec().replace("{", "").replace("}", "").split(",");

			List<String> keyValueVariantList = Arrays.asList(keyValueVariant);

			keyValueVariantList.forEach(feature -> {
				String[] kvPair = feature.split(":");
				Optional<ProductFeaturesEntity> optionalFeature = productFeaturesRepository
						.findById(Integer.parseInt(kvPair[0]));
				if (optionalFeature != null && optionalFeature.isPresent()) {
					variantObj.put(optionalFeature.get().getName(), kvPair[1]);
				}
			});
		}
	}
	
	
	
	

	private List<VariantFeatureDTO> getVariantFeatures(List<ProductVariantsEntity> productVariants) {
		List<VariantFeatureDTO> features = new ArrayList<>();
		
		if(productVariants != null ) {
			features =  productVariants
					.stream()
					.filter(this::hasFeatures)
					.map(variant -> extractVariantFeatures(variant) )
					.flatMap(List::stream)
					.collect(Collectors.toList());				
		}
			
		return features;
		
	}
	
	
	
	
	public List<VariantFeatureDTO> extractVariantFeatures(ProductVariantsEntity variant){
		JacksonJsonParser parser = new JacksonJsonParser();
		Map<String, Object> keyVal =  parser.parseMap(variant.getFeatureSpec());
		return keyVal.keySet()
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



	private JSONArray getProductVariantImages(Long variantId) {
		List<ProductImagesEntity> variantImages = productImagesRepository.findByProductVariantsEntity_Id(variantId);

		if (variantImages != null && !variantImages.isEmpty()) {
			JSONArray variantImagesArray = new JSONArray();

			variantImages.forEach(image -> {
				JSONObject imageJson = new JSONObject();
				imageJson.put("url", image.getUri());
				imageJson.put("priority", image.getPriority());
				variantImagesArray.put(imageJson);
			});
			return variantImagesArray;
		}
		return null;
	}



	private JSONArray getStockJsonArray(Long productId, Long shopId, Long variantId) {

		if (shopId == null) {
			return null;
		}
		List<StocksEntity> stocks = null;
		if (variantId != null) {
			stocks = stockRepository.findByProductEntity_IdAndShopsEntity_IdAndProductVariantsEntity_Id(productId,
					shopId, variantId);
		} else {
			stocks = getProductStockForShop(productId, shopId);
		}

		if (stocks != null && !stocks.isEmpty()) {
			JSONArray stocksArray = new JSONArray();
			stocks.forEach(stock -> {
                JSONObject stockObject = createStockJSONObject(shopId, stock);
				stocksArray.put(stockObject);
			});
			return stocksArray;
		}
		return null;

	}




    private JSONObject createStockJSONObject(Long shopId, StocksEntity stock) {
        JSONObject stockObject = new JSONObject();
        stockObject.put("shop_id", shopId);
        stockObject.put("quantity", stock.getQuantity());
        stockObject.put("price", stock.getPrice());
        stockObject.put("discount", stock.getDiscount());
        return stockObject;
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



}
