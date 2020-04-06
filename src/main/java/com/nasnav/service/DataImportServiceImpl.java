package com.nasnav.service;

import static com.nasnav.commons.utils.EntityUtils.allIsNull;
import static com.nasnav.commons.utils.EntityUtils.anyIsNull;
import static com.nasnav.commons.utils.EntityUtils.noneIsNull;
import static com.nasnav.commons.utils.StringUtils.isNotBlankOrNull;
import static com.nasnav.constatnts.EntityConstants.Operation.CREATE;
import static com.nasnav.constatnts.error.dataimport.ErrorMessages.ERR_BRAND_NAME_NOT_EXIST;
import static com.nasnav.constatnts.error.dataimport.ErrorMessages.ERR_CONVERT_TO_JSON;
import static com.nasnav.constatnts.error.dataimport.ErrorMessages.ERR_PREPARE_PRODUCT_DTO_DATA;
import static com.nasnav.constatnts.error.dataimport.ErrorMessages.ERR_TAGS_NOT_FOUND;
import static com.nasnav.integration.enums.MappingType.PRODUCT_VARIANT;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static java.util.Comparator.comparing;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static java.util.function.BinaryOperator.minBy;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;

import org.apache.commons.beanutils.BeanUtils;
import org.jboss.logging.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.commons.model.dataimport.ProductImportDTO;
import com.nasnav.commons.utils.EntityUtils;
import com.nasnav.constatnts.EntityConstants;
import com.nasnav.dao.BrandsRepository;
import com.nasnav.dao.ProductFeaturesRepository;
import com.nasnav.dao.ProductRepository;
import com.nasnav.dao.TagsRepository;
import com.nasnav.dto.BrandDTO;
import com.nasnav.dto.ProductImportMetadata;
import com.nasnav.dto.ProductTagDTO;
import com.nasnav.dto.ProductUpdateDTO;
import com.nasnav.dto.StockUpdateDTO;
import com.nasnav.dto.TagsDTO;
import com.nasnav.dto.VariantUpdateDTO;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.exceptions.ImportProductException;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.integration.IntegrationService;
import com.nasnav.persistence.BrandsEntity;
import com.nasnav.persistence.ProductEntity;
import com.nasnav.persistence.ProductFeaturesEntity;
import com.nasnav.persistence.ProductVariantsEntity;
import com.nasnav.persistence.TagsEntity;
import com.nasnav.response.OrganizationResponse;
import com.nasnav.response.ProductUpdateResponse;
import com.nasnav.response.VariantUpdateResponse;
import com.nasnav.service.helpers.CachingHelper;
import com.nasnav.service.model.ImportProductContext;
import com.nasnav.service.model.VariantCache;
import com.nasnav.service.model.VariantIdentifier;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import reactor.core.publisher.Flux;
import reactor.tuple.Tuple;

@Service
public class DataImportServiceImpl implements DataImportService {


    @Autowired
    private BrandsRepository brandRepo;

    @Autowired
    private ProductService productService;

    @Autowired
    private StockService stockService;

    @Autowired
    private ProductRepository productRepo;

    @Autowired
    private SecurityService security;

    @Autowired
    private IntegrationService integrationService;
    
    @Autowired
    private TagsRepository tagsRepo;
    
    @Autowired
    private CategoryService categoryService;
    
    
    @Autowired
	private ProductFeaturesRepository featureRepo;
    
    @Autowired
    private OrganizationService organizationService;
    

    @Autowired
    private CachingHelper cachingHelper;
    
    
    private Logger logger = Logger.getLogger(getClass());
    
    
    

    @Override
//    @Transactional(rollbackFor = Throwable.class)		// adding this will cause exception because of the enforced rollback , still unknown why
    public ImportProductContext importProducts(List<ProductImportDTO> productImportDTOS, ProductImportMetadata productImportMetadata) throws BusinessException, ImportProductException {
    	ImportProductContext context = new ImportProductContext(productImportDTOS, productImportMetadata);
    	
    	importNonExistingBrands(context);    	
    	importNonExistingTags(context);
    	
    	DataImportCachedData cache = createRequiredDataCache(productImportDTOS);
    	
    	validateProductData(productImportDTOS, cache, context);
    	
        List<ProductData> productsData = toProductDataList(productImportDTOS, productImportMetadata, cache);

        saveToDB(productsData, context);

        if(productImportMetadata.isDryrun() || !context.isSuccess()) {
        	TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        	clearImportContext(context);
        }            

        return context;
    }





	private void validateProductData(List<ProductImportDTO> productImportDTOS, DataImportCachedData cache,
			ImportProductContext context) throws ImportProductException {
		
		IntStream
		.range(0, productImportDTOS.size())
		.mapToObj(i -> new IndexedData<>(i, productImportDTOS.get(i)))
		.map(product -> new IndexedData<>(product.getIndex(), toVariantIdentifier(product.getData())))
		.filter(variantId -> !isNullVariantIdentifier(variantId.getData()))
		.filter(variantId -> isNoVariantExistWithId(variantId.getData(), cache.getVariantsCache()))
		.map(this::createErrorMessage)
		.forEach(err -> context.logNewError(err.getData(), err.getIndex()));
		
		if(!context.getErrors().isEmpty()) {
			throw new ImportProductException(context);
		}
	}





	private IndexedData<String> createErrorMessage(IndexedData<VariantIdentifier> variantId) {
		return new IndexedData<>(variantId.getIndex()
					,format("No variant found with id[%s] nor external Id[%s] at row[%d]!"
								, variantId.getData().getVariantId()
								, variantId.getData().getExternalId()
								, variantId.getIndex() + 1));
	}

	
	
	
	
	private boolean isNoVariantExistWithId(VariantIdentifier identifier, VariantCache cache) {
		return !cache.getIdToVariantMap().containsKey(identifier.getVariantId());
	}





	private boolean isNullVariantIdentifier(VariantIdentifier identifiers) {
		return nonNull(identifiers) && allIsNull(identifiers.getVariantId());
	}




	private void clearImportContext(ImportProductContext context) {
		context.getCreatedBrands().clear();
		context.getCreatedTags().clear();
		context.getCreatedProducts().clear();
		context.getUpdatedProducts().clear();
	}
    
    
    
    

    private void importNonExistingTags(ImportProductContext context) {
    	
    	Set<String> tagNames = getTagNames(context);
    	
    	Map<String, TagsEntity> existingTags = getExistingTags(tagNames);
    	
    	createNonExistingTags(context, tagNames, existingTags);
	}





	private void createNonExistingTags(ImportProductContext context, Set<String> tagNames,
			Map<String, TagsEntity> existingTags) {
		tagNames
    	.stream()
    	.filter(Objects::nonNull)
    	.filter(tagName -> !existingTags.keySet().contains(tagName.toLowerCase()))
    	.map(this::toTagDTO)
    	.map(this::createNewtag)
    	.forEach(tag -> logTagCreation(tag, context));
	}





	private Map<String, TagsEntity> getExistingTags(Set<String> tagNames) {
		Long orgId = security.getCurrentUserOrganizationId();
		return 
			Flux
			 .fromIterable(tagNames)
			 .window(500)
			 .map(Flux::buffer)
			 .flatMap(Flux::single)
			 .map(HashSet::new)
			 .flatMapIterable(tagNamesBatch -> tagsRepo.findByNameInAndOrganizationEntity_Id(tagNamesBatch, orgId))
			 .collectMap(tag -> tag.getName().toLowerCase(), tag -> tag)
			 .block();
	}





	private Set<String> getTagNames(ImportProductContext context) {
		return 
			Flux
			 .fromIterable(context.getProducts())
			 .flatMapIterable(ProductImportDTO::getTags)
			 .filter(Objects::nonNull)
			 .distinct()
			 .collect(toSet())
			 .block();
	}
    
    
    
    
    private void logTagCreation(TagsEntity tag, ImportProductContext context) {
    	context.logNewTag(tag.getId(), tag.getName());
    }
    
    
    
    private TagsDTO toTagDTO(String tagName) {
    	TagsDTO dto = new TagsDTO();
    	dto.setOperation(CREATE.getValue());
    	dto.setName(tagName);
    	dto.setHasCategory(false);
    	return dto;
    }
    
    
    
    private TagsEntity createNewtag(TagsDTO tag) {
    	try {
			return categoryService.createOrUpdateTag(tag);
		} catch (BusinessException e) {
			logger.error(e,e);
			throw new RuntimeBusinessException(e);
		}
    }

    
    
    

	private void saveToDB(List<ProductData> productsData, ImportProductContext context) throws BusinessException {
        IntStream
        	.range(0, productsData.size())
//        	.parallel() // transactions are not shared among threads
        	.mapToObj(i -> Tuple.of(i, productsData.get(i)))
        	.forEach( tuple -> saveSingleProductToDbAndLogErrors(context, tuple.getT1(), tuple.getT2()));
    }




	private void saveSingleProductToDbAndLogErrors(ImportProductContext context, int rowNum, ProductData data) {
		try {
		    saveSingleProductDataToDB(data, context);
		} catch (Throwable e) {
		    logger.error(e, e);
		    context.logNewError(e, data.toString(), rowNum);		
		}
	}
    
    
    
    


    private void saveSingleProductDataToDB(ProductData product, ImportProductContext context) throws BusinessException {
        if (product.isExisting()) {
            updateProduct(product, context);            
        } else {
            Long productId = saveNewImportedProduct(product);
            context.logNewCreatedProduct(productId, product.getProductDto().getName());
        }

    }




	private void updateProduct(ProductData product, ImportProductContext context) throws BusinessException {
		ProductImportMetadata importMetaData = context.getImportMetaData();
		boolean updateProductEnabled = importMetaData.isUpdateProduct();
		boolean updateStocksEnabled = importMetaData.isUpdateStocks();
		
		if (updateProductEnabled) {
		    Long productId = saveProductDto(product.getProductDto());
		    saveProductTags(product, productId);
		    for(VariantDTOWithExternalIdAndStock variant: product.getVariants()) {
		    	saveVariant(variant);
		    }
		}
		
		if (updateStocksEnabled) {
			for(VariantDTOWithExternalIdAndStock variant: product.getVariants()) {
				stockService.updateStock(variant.getStock());
		    }
		}
		
		if(updateProductEnabled ||updateStocksEnabled) {
			context.logNewUpdatedProduct(product.getProductDto().getId(), product.getProductDto().getName());
		}
	}
    
    
    
	
	private Long saveVariant(VariantDTOWithExternalIdAndStock variant) throws BusinessException {
		 VariantUpdateResponse variantResponse = productService.updateVariant(variant);	
		 Long variantId = variantResponse.getVariantId();
		 saveExternalMapping(variant, variantId);
		 return variantId;
	}


	
	
	
    private Long saveNewImportedProduct(ProductData data) throws BusinessException {
        Long productId = saveProductDto(data.getProductDto());
        saveProductTags(data, productId);
        data.getProductDto().setId(productId);
        saveVariantsAndStocks(data, productId);
        return productId;
    }




    
	private void saveVariantsAndStocks(ProductData data, Long productId) throws BusinessException {
		for(VariantDTOWithExternalIdAndStock variant: data.getVariants()) {
			variant.setProductId(productId);			
	        Long variantId = saveVariant(variant);
	        
	        variant.getStock().setVariantId(variantId);
	        stockService.updateStock(variant.getStock());
	    }
	}





	private void saveProductTags(ProductData data, Long productId) throws BusinessException {
		Long orgId = security.getCurrentUserOrganizationId();
        Set<String> tagsNames = data.getTagsNames();
        Map<String, TagsEntity> tagsMap = getTagsNamesMap(orgId, tagsNames);
        
        validateTags(orgId, tagsNames, tagsMap);        
        
        saveProductTagsToDB(tagsMap, productId);
	}




	private Map<String, TagsEntity> getTagsNamesMap(Long orgId, Set<String> tagsNames) {
		return tagsRepo
				.findByNameInAndOrganizationEntity_Id(tagsNames, orgId)
				.stream()
				.collect(toMap(TagsEntity::getName, t -> t));
	}




	private void saveProductTagsToDB(Map<String, TagsEntity> tagsMap, Long productId) throws BusinessException {
		ProductTagDTO productTagDTO = new ProductTagDTO();
        List<Long> tagIds = getTagIdList(tagsMap);
        productTagDTO.setProductIds(asList(productId));
        productTagDTO.setTagIds(tagIds);
        
        productService.updateProductTags(productTagDTO);
	}




	private List<Long> getTagIdList(Map<String, TagsEntity> tagsMap) {
		return tagsMap
	    		.values()
	    		.stream()
	    		.map(TagsEntity::getId)
	    		.collect(toList());
	}




	private void validateTags(Long orgId, Set<String> tagsNames, Map<String, TagsEntity> tagsMap)
			throws BusinessException {
		Set<String> nonExistingTags = 
        		tagsNames
        		.stream()
        		.filter(tagName -> !tagsMap.keySet().contains(tagName))
        		.collect(toSet());
        
        if(!nonExistingTags.isEmpty()) {
        	throw new BusinessException(
        				String.format(ERR_TAGS_NOT_FOUND, nonExistingTags, orgId)
        				, "INVLAID PRODUCT DATA"
        				, NOT_ACCEPTABLE
        			);
        }
	}

    
    
    
    private void saveExternalMapping(VariantDTOWithExternalIdAndStock variant, Long variantId) throws BusinessException{
        if ( !anyIsNull(variant.getExternalId(), variantId) ) {
        	Long orgId = security.getCurrentUserOrganizationId();
        	integrationService.addMappedValue(orgId, PRODUCT_VARIANT, variantId.toString(), variant.getExternalId());
        }            
    }

    
    
    
    
    private Long saveProductDto(ProductUpdateDTO dto) throws BusinessException {
        String productDtoJson = getProductDtoJson(dto);
        ProductUpdateResponse productResponse = productService.updateProduct(productDtoJson, false);
        Long productId = productResponse.getProductId();
        return productId;
    }

    
    
    

    private String getProductDtoJson(ProductUpdateDTO dto) throws BusinessException {
        ProductUpdateDTO dtoClone = prepareProductUpdateDto(dto);
        String productDtoJson = "";
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            productDtoJson = mapper.writeValueAsString(dtoClone);
        } catch (Exception e) {
            logger.error(e, e);
            throw new BusinessException(
                    String.format(ERR_CONVERT_TO_JSON, dtoClone.getClass().getName())
                    , "INTERNAL SERVER ERROR"
                    , HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return productDtoJson;
    }


    private ProductUpdateDTO prepareProductUpdateDto(ProductUpdateDTO dto) throws BusinessException {
        try {
        	Long orgId = security.getCurrentUserOrganizationId();
            ProductUpdateDTO dtoClone = (ProductUpdateDTO) BeanUtils.cloneBean(dto);
            Optional<ProductEntity> product = 
            		productRepo
            			.findByNameAndOrganizationId(dto.getName(), orgId)
            			.stream()
            			.findFirst();
            if (product.isPresent()) {
                dtoClone.setId(product.get().getId());
                dtoClone.setOperation(EntityConstants.Operation.UPDATE);
            }
            return dtoClone;
        } catch (Exception e) {
            logger.error(e, e);
            throw new BusinessException(
                    String.format(ERR_PREPARE_PRODUCT_DTO_DATA, dto.toString())
                    , "INTERNAL SERVER ERROR"
                    , HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    private List<ProductData> toProductDataList(List<ProductImportDTO> rows
    		, ProductImportMetadata importMetaData, DataImportCachedData cache) throws BusinessException, RuntimeBusinessException {    	
    	
    	
    	return rows
    			.stream()
//	    		.parallel() //transactions are not shared among threads
	    		.filter(row -> isNotBlankOrNull(row.getBrand()))
	    		.collect(groupingBy(row -> ofNullable(row.getProductGroupKey()).orElse(row.getName())))
	    		.values()
	    		.stream()
	    		.filter(EntityUtils::noneIsEmpty)
	    		.map(productData -> toProductDataWrapped(importMetaData, cache, productData))
	    		.collect(toList());
    }




	private DataImportCachedData createRequiredDataCache(List<ProductImportDTO> rows) {
		Map<String, String> featureNameToIdMapping = getFeatureNameToIdMap();
    	Map<String, BrandsEntity> brandNameToIdMapping = getBrandsMapping();
    	VariantCache variantCache = createVariantsCache(rows);
    	return new DataImportCachedData(featureNameToIdMapping, brandNameToIdMapping,variantCache);
	}




	private VariantCache createVariantsCache(List<ProductImportDTO> rows) {
		List<VariantIdentifier> variantIdentifiers = toVariantIdentifiers(rows);
		return cachingHelper.createVariantCache(variantIdentifiers);
	}





	private List<VariantIdentifier> toVariantIdentifiers(List<ProductImportDTO> rows) {
		return rows
				.stream()
				.map(this::toVariantIdentifier)
				.collect(toList());
	}

	
	
	
	private VariantIdentifier toVariantIdentifier(ProductImportDTO row) {
		VariantIdentifier identifier = new VariantIdentifier();
		String variantId = ofNullable(row.getVariantId()).map(String::valueOf).orElse(null);
		identifier.setVariantId(variantId);
		identifier.setExternalId(row.getExternalId());
		identifier.setBarcode(row.getBarcode());
		return identifier;
	}




	private Map<String, BrandsEntity> getBrandsMapping() {
		Long orgId = security.getCurrentUserOrganizationId();
		return brandRepo
				.findByOrganizationEntity_Id(orgId)
				.stream()
				.collect(toMap(
							brand -> brand.getName().toUpperCase() 
							, brand -> brand
							, minBy(comparing(BrandsEntity::getId))));
	}




	private ProductData toProductDataWrapped(ProductImportMetadata importMetaData,
			DataImportCachedData cache, List<ProductImportDTO> productData) {
		try {
			return toProductData(productData, importMetaData, cache);
		} catch (BusinessException e) {
			logger.error(e,e);
			throw new RuntimeBusinessException(e);
		}
	}


    private ProductData toProductData(List<ProductImportDTO> productDataRows, ProductImportMetadata importMetaData, DataImportCachedData cache) throws BusinessException {
    	ProductImportDTO pivotProductRow = getPivotProductDataRow(productDataRows, cache);
    	List<VariantDTOWithExternalIdAndStock> productVariantsData =
    		        getVariantsData(productDataRows, importMetaData, cache);
    	
    	ProductData data = new ProductData();
        data.setOriginalRowData(productDataRows.toString());
        data.setProductDto(createProductDto(pivotProductRow, cache));
        data.setVariants(productVariantsData);
        data.setTagsNames( ofNullable(pivotProductRow.getTags()).orElse(emptySet()) );
        
        return data;
    }





	private List<VariantDTOWithExternalIdAndStock> getVariantsData(List<ProductImportDTO> productDataRows,
			ProductImportMetadata importMetaData, DataImportCachedData cache) {
		return productDataRows
				.stream()
				.map(row -> createVariantDto(row, importMetaData, cache))
				.collect(toList());
	}





	private ProductImportDTO getPivotProductDataRow(List<ProductImportDTO> productDataRows, DataImportCachedData cache) throws BusinessException {
		ProductImportDTO firstProductRow = 
    			productDataRows
    			.stream()
    			.findFirst()
    			.orElseThrow(() -> new BusinessException("No Product Data found!", "INVALID PARAM: csv", NOT_ACCEPTABLE));
		return productDataRows
    			.stream()
    			.filter(productDataRow -> hasExistingVariant(productDataRow, cache))
    			.findFirst()
    			.orElse(firstProductRow);
	}
    
    
    


    private boolean hasExistingVariant(ProductImportDTO productDataRow, DataImportCachedData cache) {
		return ofNullable(productDataRow)
				.map(this::toVariantIdentifier)
				.flatMap(identifier -> cachingHelper.getVariantFromCache(identifier, cache.getVariantsCache()))
				.isPresent();
	}

    
    
    

    private StockUpdateDTO createStockDto(ProductImportDTO row, ProductImportMetadata importMetaData) {
        StockUpdateDTO stock = new StockUpdateDTO();
        stock.setCurrency(importMetaData.getCurrency());
        stock.setShopId(importMetaData.getShopId());
        stock.setPrice(row.getPrice());
        stock.setQuantity(row.getQuantity());
        return stock;
    }

    
    
    

    private VariantDTOWithExternalIdAndStock createVariantDto(ProductImportDTO row, ProductImportMetadata importMetaData, DataImportCachedData cache) {
    	Map<String,String> featureNameToIdMapping = cache.getFeatureNameToIdMapping();
    	
        String features = getFeaturesAsJsonString(row, featureNameToIdMapping);        
        String extraAtrributes = getExtraAttrAsJsonString(row);        
        StockUpdateDTO variantStock = createStockDto(row, importMetaData);

        VariantDTOWithExternalIdAndStock variant = new VariantDTOWithExternalIdAndStock();
        variant.setVariantId(row.getVariantId());
        variant.setBarcode(row.getBarcode());
        variant.setFeatures("{}");
        variant.setDescription(row.getDescription());
        variant.setName(row.getName());
        variant.setOperation(EntityConstants.Operation.CREATE);
        variant.setPname(row.getPname());
        variant.setExternalId(row.getExternalId());
        variant.setStock(variantStock);
        if (features != null) {
            variant.setFeatures(features);
        }
        if(extraAtrributes != null) {
        	variant.setExtraAttr(extraAtrributes);
        }
        
        cachingHelper
        .getVariantFromCache(toVariantIdentifier(row), cache.getVariantsCache())
        .ifPresent(variantEntity -> {
        	setVariantDtoAsUpdated(variant, variantEntity);
        });

        return variant;
    }





	private void setVariantDtoAsUpdated(VariantDTOWithExternalIdAndStock variant, ProductVariantsEntity variantEntity) {
		variant.setVariantId(variantEntity.getId());
		variant.setProductId(variantEntity.getProductEntity().getId());
		variant.setOperation(EntityConstants.Operation.UPDATE);
		variant.getStock().setVariantId(variantEntity.getId());
	}





	private String getExtraAttrAsJsonString(ProductImportDTO row) {
		return ofNullable(row.getExtraAttributes())
				.map(JSONObject::new)
				.map(JSONObject::toString)
				.orElse(null);
	}





	private String getFeaturesAsJsonString(ProductImportDTO row, Map<String, String> featureNameToIdMapping) {
		return ofNullable(row.getFeatures())
				.map(map -> toFeaturesIdToValueMap(map, featureNameToIdMapping))
				.map(JSONObject::new)
				.map(JSONObject::toString)
				.orElse(null);
	}
    
    
    
    
    
    private Map<String,String> toFeaturesIdToValueMap(Map<String,String> featuresNameToValueMap, Map<String,String> nameToIdMap){    	
    	return ofNullable(featuresNameToValueMap)
    			.orElse(emptyMap())
    			.entrySet()
    			.stream()
    			.filter(ent -> noneIsNull(ent.getKey(), ent.getValue()))
    			.collect(toMap(ent -> nameToIdMap.get(ent.getKey())
    									, Map.Entry::getValue));
    }




	private Map<String, String> getFeatureNameToIdMap() {
		Long orgId = security.getCurrentUserOrganizationId();
    	return 
    		featureRepo
	    		.findByOrganizationId(orgId)
				.stream()
				.collect(toMap(ProductFeaturesEntity::getName, this::getFeatureIdAsStr));
	}
    
    
    
    private String getFeatureIdAsStr(ProductFeaturesEntity feature) {
    	return String.valueOf(feature.getId());
    }
    
    


    private ProductUpdateDTO createProductDto(ProductImportDTO row, DataImportCachedData cache) throws BusinessException {
    	Long brandId = getBrandId(row, cache.getBrandsCache());
        
        ProductUpdateDTO product = new ProductUpdateDTO();
        product.setBrandId(brandId);
        product.setDescription(row.getDescription());
        product.setBarcode(row.getBarcode());
        product.setName(row.getName());
        product.setOperation(EntityConstants.Operation.CREATE);
        product.setPname(row.getPname());
        
        ifVariantExistsSetAsUpdateOperation(row, cache, product);
        
        return product;
    }





	private Long getBrandId(ProductImportDTO row, Map<String, BrandsEntity> brandsCache) throws BusinessException {
		return ofNullable(row.getBrand())
				.map(String::toUpperCase)
				.map(brandsCache::get)
				.map(BrandsEntity::getId)
				.orElseThrow(() ->
					new BusinessException(
				            format(ERR_BRAND_NAME_NOT_EXIST, row.getBrand())
				            , "INVALID DATA:brand"
				            , HttpStatus.NOT_ACCEPTABLE));
	}





	private void ifVariantExistsSetAsUpdateOperation(ProductImportDTO row, DataImportCachedData cache,
			ProductUpdateDTO product) {
		cachingHelper
        .getVariantFromCache(toVariantIdentifier(row), cache.getVariantsCache())
		.map(ProductVariantsEntity::getProductEntity)
		.map(ProductEntity::getId)
		.ifPresent(
				id -> { product.setId(id); 
						product.setOperation(EntityConstants.Operation.UPDATE);});
	}
    
    
    
    private void importNonExistingBrands(ImportProductContext context) {
    	context
    		.getProducts()
			.stream()
			.map(ProductImportDTO::getBrand)
			.distinct()
			.filter(Objects::nonNull)
			.filter(this::isBrandNotExists)
			.map(this::toBrandDTO)
			.map(this::createBrand)
			.forEach(brand -> logBrandCreation(brand, context));;
	}
    
    
    

	
	
	
	private void logBrandCreation(BrandDTO brand, ImportProductContext context) {
		context.logNewBrand(brand.getId(), brand.getName());
	}





	private BrandDTO createBrand(BrandDTO brandDto) {
		try {
			OrganizationResponse response = organizationService.createOrganizationBrand(brandDto, null, null);
			BrandDTO createdBrand = new BrandDTO();
			createdBrand.setId(response.getBrandId());
			createdBrand.setName(brandDto.getName());
			return createdBrand;
		}catch(Throwable t) {
			logger.error(t,t);
			throw new RuntimeBusinessException(
					format("Failed to import brand with name [%s]",brandDto.getName())
					, "INTEGRATION FAILURE"
					,HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	
	
	
	
	private boolean isBrandNotExists(String brandName) {
		Long orgId = security.getCurrentUserOrganizationId();		
		return !brandRepo.existsByNameIgnoreCaseAndOrganizationEntity_id(brandName, orgId);
	}
	
	
	
	
	private BrandDTO toBrandDTO(String brandName) {
		BrandDTO dto = new BrandDTO();
		dto.setOperation("CREATE");
		dto.setName(brandName);
		return dto;
	}


    
    
}


@Data
class ProductData{
	private ProductUpdateDTO productDto;
    private List<VariantDTOWithExternalIdAndStock> variants;    
    private String originalRowData;
    
    private Set<String> tagsNames;

    public ProductData() {
        variants = new ArrayList<>();
        productDto = new ProductUpdateDTO();
        originalRowData = "[]";
        tagsNames = emptySet();
    }
    
    public boolean isExisting() {
    	return ofNullable(productDto)
    			.map(ProductUpdateDTO::getId)
    			.isPresent();
    } 
}





@Data
@EqualsAndHashCode(callSuper = true)
class VariantDTOWithExternalIdAndStock extends VariantUpdateDTO{
	private String externalId; 
	private StockUpdateDTO stock;
}





@Data
@AllArgsConstructor
class DataImportCachedData {
	private Map<String, String> featureNameToIdMapping;
	private Map<String, BrandsEntity> brandsCache;
	private VariantCache variantsCache;
}



@Data
@AllArgsConstructor
class IndexedData<T>{
	private Integer index;
	private T data;
}
