package com.nasnav.service;

import static com.nasnav.commons.utils.CollectionUtils.divideToBatches;
import static com.nasnav.commons.utils.CollectionUtils.processInBatches;
import static com.nasnav.commons.utils.EntityUtils.allIsNull;
import static com.nasnav.commons.utils.EntityUtils.anyIsNull;
import static com.nasnav.commons.utils.EntityUtils.firstExistingValueOf;
import static com.nasnav.commons.utils.EntityUtils.noneIsNull;
import static com.nasnav.commons.utils.StringUtils.generateUUIDToken;
import static com.nasnav.commons.utils.StringUtils.isNotBlankOrNull;
import static com.nasnav.constatnts.EntityConstants.Operation.CREATE;
import static com.nasnav.constatnts.EntityConstants.Operation.UPDATE;
import static com.nasnav.constatnts.error.dataimport.ErrorMessages.ERR_BRAND_NAME_NOT_EXIST;
import static com.nasnav.constatnts.error.dataimport.ErrorMessages.ERR_CONVERT_TO_JSON;
import static com.nasnav.constatnts.error.dataimport.ErrorMessages.ERR_TAGS_NOT_FOUND;
import static com.nasnav.enumerations.OrderStatus.NEW;
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
import static org.apache.commons.beanutils.BeanUtils.copyProperties;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.nasnav.commons.utils.CollectionUtils;
import org.jboss.logging.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.commons.model.IndexedData;
import com.nasnav.commons.model.dataimport.ProductImportDTO;
import com.nasnav.commons.utils.EntityUtils;
import com.nasnav.constatnts.EntityConstants;
import com.nasnav.dao.BrandsRepository;
import com.nasnav.dao.ProductFeaturesRepository;
import com.nasnav.dao.ProductRepository;
import com.nasnav.dao.TagsRepository;
import com.nasnav.dto.BrandDTO;
import com.nasnav.dto.ProductImportMetadata;
import com.nasnav.dto.ProductUpdateDTO;
import com.nasnav.dto.StockUpdateDTO;
import com.nasnav.dto.TagsDTO;
import com.nasnav.dto.VariantUpdateDTO;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.exceptions.ImportProductException;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.exceptions.StockValidationException;
import com.nasnav.integration.IntegrationService;
import com.nasnav.persistence.BrandsEntity;
import com.nasnav.persistence.ProductFeaturesEntity;
import com.nasnav.persistence.TagsEntity;
import com.nasnav.response.OrganizationResponse;
import com.nasnav.service.helpers.CachingHelper;
import com.nasnav.service.model.DataImportCachedData;
import com.nasnav.service.model.ProductTagPair;
import com.nasnav.service.model.VariantBasicData;
import com.nasnav.service.model.VariantCache;
import com.nasnav.service.model.VariantIdentifier;
import com.nasnav.service.model.importproduct.context.ImportProductContext;
import com.nasnav.service.model.importproduct.context.Product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import reactor.core.publisher.Flux;

@Service
public class DataImportServiceImpl implements DataImportService {


    @Autowired
    private BrandsRepository brandRepo;

    @Autowired
    private ProductService productService;

    @Autowired
    private StockService stockService;

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
    
    @Autowired
    private ProductRepository productRepo;
    @Autowired
	private ProductServiceTransactions productServiceTransactions;
    
    private Logger logger = Logger.getLogger(getClass());
    
    
    

    @Override
//    @Transactional(rollbackFor = Throwable.class)		// adding this will cause exception because of the enforced rollback , still unknown why
    public ImportProductContext importProducts(List<ProductImportDTO> productImportDTOS, ProductImportMetadata productImportMetadata) throws BusinessException, ImportProductException {
    	ImportProductContext context = new ImportProductContext(productImportDTOS, productImportMetadata);
    	
    	importNonExistingBrands(context);    	
    	importNonExistingTags(context);
    	
    	DataImportCachedData cache = createRequiredDataCache(productImportDTOS);
    	
    	validateProductData(productImportDTOS, cache, context);
    	
        ProductDataLists productsData = toProductDataList(productImportDTOS, productImportMetadata, cache);

        saveToDB(productsData, context);

        if(productImportMetadata.isDryrun() || !context.isSuccess()) {
        	TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        	clearImportContext(context);
        }
        
        if(productImportMetadata.isDeleteOldProducts()) {
        	Set<Long> productsToDelete = getProductsToDelete(context);
			processInBatches(productsToDelete, 500, productServiceTransactions::deleteProducts);
        }

        return context;
    }



	private Set<Long> getProcessedProducts(ImportProductContext context) {
		Set<Long> processedProducts = 
				Stream
				.concat(
						context.getCreatedProducts().stream()
						, context.getUpdatedProducts().stream())
				.map(Product::getId)
				.collect(toSet());
		if(processedProducts.isEmpty()) {
			processedProducts.add(-1L);
		}
		return processedProducts;
	}





	private Set<Long> getProductsToDelete(ImportProductContext context) {
		Long orgId = security.getCurrentUserOrganizationId();
		Set<Long> productsToDelete = productRepo.listProductIdByOrganizationId(orgId);
		Set<Long> processedProducts = getProcessedProducts(context);
		processedProducts.forEach(productsToDelete::remove);
		return productsToDelete;
	}





	private void validateProductData(List<ProductImportDTO> productImportDTOS,DataImportCachedData cache,
			ImportProductContext context) throws ImportProductException {
		
		IntStream
		.range(0, productImportDTOS.size())
		.mapToObj(i -> new IndexedData<>(i, productImportDTOS.get(i)))
		.map(product -> new IndexedData<>(product.getIndex(), toVariantIdentifier(product.getData())))
		.filter(variantId -> !isNullVariantIdentifier(variantId.getData()))
		.filter(variantId -> isNoVariantExistWithId(variantId.getData(), cache.getVariantsCache()))
		.map(this::createErrorMessage)
		.forEach(err -> context.logNewError(err.getData(), err.getIndex()+1));
		
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





	private Map<String, TagsEntity> getExistingTags(Set<String> tagsNames) {
		Long orgId = security.getCurrentUserOrganizationId();
		Set<String> tagNamesInLowerCase = toLowerCase(tagsNames);
		return
			Flux
			 .fromIterable(tagNamesInLowerCase)
			 .window(500)
			 .map(Flux::buffer)
			 .flatMap(Flux::single)
			 .map(HashSet::new)
			 .flatMapIterable(tagNamesBatch -> findTagsNameLowerCaseInAndOrganizationId(tagNamesBatch, orgId))
			 .collectMap(tag -> tag.getName().toLowerCase(), tag -> tag)
			 .block();
	}




	private Set<TagsEntity> findTagsNameLowerCaseInAndOrganizationId(Set<String> tagNamesInLowerCase, Long orgId){
    	if(tagNamesInLowerCase == null || tagNamesInLowerCase.isEmpty()){
    		return emptySet();
		}
    	return tagsRepo.findByNameLowerCaseInAndOrganizationEntity_Id(tagNamesInLowerCase, orgId);
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

    
    
    

	private void saveToDB(ProductDataLists productsData, ImportProductContext context) throws BusinessException {
		//save procedure is mutable, so the product data is modified after each step
		//this can introduce annoying bugs, so, it is better we think of something better later.
		ProductDataImportContext.of(productsData, context)
		.map(this::saveProducts)
		.map(this::saveVariants)
		.map(this::saveStocks);
    }
	
	
	
	
	
	private ProductDataImportContext saveProducts(ProductDataImportContext context) {
		ImportProductContext importContext = context.getContext();
		ProductImportMetadata importMetaData = importContext.getImportMetaData();
		boolean updateProductEnabled = importMetaData.isUpdateProduct();
		boolean insertNewProducts = importMetaData.isInsertNewProducts();
		
		List<ProductData> newProducts = context.getProductsData().getNewProductsData();
		List<ProductData> existingProducts = context.getProductsData().getExistingProductsData();
		
		if(insertNewProducts){
			logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>> saving New products ....");
			saveNewProducts(importContext, newProducts);
		}			
        
		if(updateProductEnabled) {
			logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>> saving existing products ....");			
			saveExistingProducts(importContext, existingProducts);
		}
		
		return context;
	}

	
	
	
	private void saveNewProducts(ImportProductContext importContext, List<ProductData> newProducts) {
		boolean resetTags = importContext.getImportMetaData().isResetTags();
		List<ProductUpdateDTO> productDtos = getProductUpdateDtoList(newProducts);
		List<Long> newProductIds = saveProducts(productDtos);
        
		for(int i=0; i < newProductIds.size(); i++) {
			Long productId = newProductIds.get(i);
			ProductData data = newProducts.get(i);
			data.getProductDto().setId(productId);
	        data.getVariants().forEach(variant -> variant.setProductId(productId));	
	        
			importContext.logNewCreatedProduct(productId, data.getProductDto().getName());
		}
		
		saveProductTags(newProducts, resetTags);
	}



	
	
	
	private void saveExistingProducts(ImportProductContext importContext, List<ProductData> existingProducts) {
		ProductImportMetadata importMetaData = importContext.getImportMetaData();
		boolean updateProductEnabled = importMetaData.isUpdateProduct();
		boolean updateStocksEnabled = importMetaData.isUpdateStocks();
		boolean resetTags = importMetaData.isResetTags();
		
		List<ProductUpdateDTO> existingProductDtos = getProductUpdateDtoList(existingProducts);
		List<Long> exisintgProductIds = saveProducts(existingProductDtos);
        
		for(int i=0; i < exisintgProductIds.size(); i++) {
			Long productId = exisintgProductIds.get(i);
			ProductData data = existingProducts.get(i);
	        data.getVariants().forEach(variant -> variant.setProductId(productId));	 
	        if(updateProductEnabled ||updateStocksEnabled) {
				importContext.logNewUpdatedProduct(data.getProductDto().getId(), data.getProductDto().getName());
			}
		}
		
		saveProductTags(existingProducts, resetTags);
	}




	
	
	private List<ProductUpdateDTO> getProductUpdateDtoList(List<ProductData> newProducts) {
		return newProducts
				.stream()
				.map(ProductData::getProductDto)
				.collect(toList());
	}

	
	
	
	
	private ProductDataImportContext saveVariants(ProductDataImportContext context) {
		if(context.getContext().getImportMetaData().isInsertNewProducts()) {
			saveNewProductsVariants(context);
		}
		
		if(context.getContext().getImportMetaData().isUpdateProduct()) {
			saveExistingProductsVariants(context);
		}
				
		return context;
	}





	private void saveNewProductsVariants(ProductDataImportContext context) {
		logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>> saving New variants ....");
		List<ProductData> productsData = context.getProductsData().getNewProductsData();
		saveVariants(productsData);
	}





	private void saveVariants(List<ProductData> productsData) {
		try {
			List<VariantDTOWithExternalIdAndStock> variants = getVariants(productsData);
			List<Long> savedVariants = productService.updateVariantBatch(variants);
			
			for(int i=0; i < variants.size(); i++) {
				Long variantId = savedVariants.get(i);
				VariantDTOWithExternalIdAndStock variant = variants.get(i);
				saveExternalMapping(variant, variantId);
				variant.getStock().setVariantId(variantId);
			}
		} catch (BusinessException e) {
			logger.error(e,e);
			throw new RuntimeBusinessException(e);
		}
	}





	private void saveExistingProductsVariants(ProductDataImportContext context) {
		logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>> saving Existing variants ....");
		List<ProductData> productsData = context.getProductsData().getExistingProductsData();
		saveVariants(productsData);
	}




	
	
	
	private List<VariantDTOWithExternalIdAndStock> getVariants(List<ProductData> products) {
		return 
			products
			.stream()
			.map(ProductData::getVariants)
			.flatMap(List::stream)
			.collect(toList());
	}
	
	
	
	
	
	
	private ProductDataImportContext saveStocks(ProductDataImportContext context) {
		ProductImportMetadata importMetaData = context.getContext().getImportMetaData();
		boolean updateStocksEnabled = importMetaData.isUpdateStocks();
		boolean insertNewProducts = importMetaData.isInsertNewProducts();
		
		List<StockUpdateDTO> newStocks = getNewProductsStocks(context);
		List<StockUpdateDTO> updatedStocks = getUpdatedProductsStocks(context);
		
		if(insertNewProducts) {
			insertNewStocks(context, newStocks);
		}
		
		if (updateStocksEnabled) {
			insertUpdatedProductsStocks(context, updatedStocks);
		}
		
		return context;
	}





	private void insertUpdatedProductsStocks(ProductDataImportContext context, List<StockUpdateDTO> updatedStocks) {
		try {
			logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>> saving existing stocks ....");
			//we let updateStockBatch() recreate the variants cache, as the variants cache, as the initial cache was invalidated
			//after saving variants.
			stockService.updateStockBatch(updatedStocks);   
		} catch (StockValidationException e) {
			context.getContext().logNewError(e, e.getErrorMessage(), e.getIndex());
			throw e;
		}
		context
		.getProductsData()
		.getExistingProductsData()
		.forEach(data -> context.getContext().logNewUpdatedProduct(data.getProductDto().getId(), data.getProductDto().getName()));
	}





	private void insertNewStocks(ProductDataImportContext context, List<StockUpdateDTO> newStocks) {
		try {
			logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>> saving new stocks ....");
			//we let updateStockBatch() recreate the variants cache, as the variants cache, as the initial cache was invalidated
			//after saving variants.
			stockService.updateStockBatch(newStocks);   
		} catch (StockValidationException e) {
			context.getContext().logNewError(e, e.getErrorMessage(), e.getIndex());
			throw e;
		}
	}





	private List<StockUpdateDTO> getUpdatedProductsStocks(ProductDataImportContext context) {
		return getUpdatedVariants(context)
				.stream()
				.map(VariantDTOWithExternalIdAndStock::getStock)
				.collect(toList());
	}





	private List<VariantDTOWithExternalIdAndStock> getUpdatedVariants(ProductDataImportContext context) {
		return context
				.getProductsData()
				.getExistingProductsData()
				.stream()
				.map(ProductData::getVariants)
				.flatMap(List::stream)
//				.filter(variant -> Objects.equals(variant.getOperation(), UPDATE))
				.collect(toList());
	}





	private List<StockUpdateDTO> getNewProductsStocks(ProductDataImportContext context) {
		return context
				.getProductsData()
				.getNewProductsData()
				.stream()
				.map(ProductData::getVariants)
				.flatMap(List::stream)
//				.filter(variant -> Objects.equals(variant.getOperation(), CREATE))
				.map(VariantDTOWithExternalIdAndStock::getStock)
				.collect(toList());
	}
	


	


	private void saveProductTags(List<ProductData> data, boolean isResetTags) {
		Long orgId = security.getCurrentUserOrganizationId();
        Set<String> allTagsNames = 
        		data
        		.stream()
        		.map(ProductData::getTagsNames)
        		.flatMap(Set::stream)
        		.collect(toSet());
        Map<String, TagsEntity> tagsMap = getTagsNamesMap(orgId, allTagsNames);
        
        validateTags(orgId, allTagsNames, tagsMap);        
        
        saveProductsTagsToDB(tagsMap, data, isResetTags);
	}




	private Map<String, TagsEntity> getTagsNamesMap(Long orgId, Set<String> tagsNames) {
    	Set<String> tagNamesInLowerCase = toLowerCase(tagsNames);
		return findTagsNameLowerCaseInAndOrganizationId(tagNamesInLowerCase, orgId)
				.stream()
				.collect(toMap(t -> t.getName().toLowerCase(), Function.identity(), this::pickLowerId));
	}



	private  TagsEntity pickLowerId(TagsEntity tag1, TagsEntity tag2) {
    	return Stream.of(tag1, tag2)
				.min(Comparator.comparing(TagsEntity::getId))
				.get();
	}




	private Set<String> toLowerCase(Set<String> tagsNames) {
		return tagsNames
				.stream()
				.map(String::toLowerCase)
				.collect(toSet());
	}


	private void saveProductsTagsToDB(Map<String, TagsEntity> tagsMap, List<ProductData> data, boolean isResetTags){
		Set<ProductTagPair> productTags = 
				data
				.parallelStream()
				.map(product -> toTagAndProductIdPairs(product, tagsMap))
				.flatMap(List::stream)
				.distinct()
				.collect(toSet());

		List<Long> productIds = 
				data
				.parallelStream()
				.map(ProductData::getProductDto)
				.map(ProductUpdateDTO::getId)
				.filter(Objects::nonNull)
				.collect(toList());
		
        if( isResetTags && !productTags.isEmpty()) {
        	productService.deleteAllTagsForProducts(productIds);
        }
		
		productService.addTagsToProducts(productTags);
	}

	
	
	
	
	
	
	private List<ProductTagPair> toTagAndProductIdPairs(ProductData data, Map<String, TagsEntity> tagsMap) {
		return data
				.getTagsNames()
				.stream()
				.map(tagName -> toTagAndProductIdPair(data, tagsMap, tagName))
				.collect(toList());
	}
	
	
	
	
	
	private ProductTagPair toTagAndProductIdPair(ProductData data, Map<String, TagsEntity> tagsMap, String tagName){
		Long orgId = security.getCurrentUserOrganizationId();
		Long tagId = ofNullable(tagsMap.get(tagName.toLowerCase()))
					.map(TagsEntity::getId)
					.orElseThrow(() -> 
						new RuntimeBusinessException(
	        				format(ERR_TAGS_NOT_FOUND, tagName, orgId)
	        				, "INVLAID PRODUCT DATA"
	        				, NOT_ACCEPTABLE
	        			));
		return new ProductTagPair(data.getProductDto().getId(), tagId);
	}

	
	



	private void validateTags(Long orgId, Set<String> tagsNames, Map<String, TagsEntity> tagsMap){
		Set<String> nonExistingTags = 
        		tagsNames
        		.stream()
        		.filter(tagName -> !tagsMap.keySet().contains(tagName.toLowerCase()))
        		.collect(toSet());
        
        if(!nonExistingTags.isEmpty()) {
        	throw new RuntimeBusinessException(
        				format(ERR_TAGS_NOT_FOUND, nonExistingTags, orgId)
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

    
    
    
    
    private List<Long> saveProducts(List<ProductUpdateDTO> dtoList){
    	List<String> productJsonList = 
    			dtoList
    			.stream()
    			.map(this::getProductDtoJson)
    			.collect(toList());
    	return productService.updateProductBatch(productJsonList, false, false);
    }

    
    
    

    private String getProductDtoJson(ProductUpdateDTO dto) {
        String productDtoJson = "";
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            productDtoJson = mapper.writeValueAsString(dto);
        } catch (Exception e) {
            logger.error(e, e);
            throw new RuntimeBusinessException(
                    format(ERR_CONVERT_TO_JSON, dto.getClass().getName())
                    , "INTERNAL SERVER ERROR"
                    , INTERNAL_SERVER_ERROR);
        }
        return productDtoJson;
    }


    
    

    private ProductDataLists toProductDataList(List<ProductImportDTO> rows
    		, ProductImportMetadata importMetaData, DataImportCachedData cache) throws BusinessException, RuntimeBusinessException {    	
    	List<ProductData> allProductData = 
    			IntStream
    			.range(0, rows.size())
    			.mapToObj(i -> new IndexedProductImportDTO(i, rows.get(i)))
//	    		.parallel() //transactions are not shared among threads
	    		.collect(groupingBy(row -> firstExistingValueOf(row.getProductGroupKey(), row.getName(), generateUUIDToken())))
	    		.values()
	    		.stream()
	    		.filter(EntityUtils::noneIsEmpty)
	    		.map(singleProductRows -> toProductData(singleProductRows,importMetaData, cache))
	    		.collect(toList());
    	
    	List<ProductData> newProductsData = 
    			allProductData
    			.stream()
    			.filter(productData -> isNewProductsInsertAllowed(importMetaData, productData))
    			.collect(toList());
    	
    	List<ProductData> existingProductsData = 
    			allProductData
    			.stream()
    			.filter(productData -> isUpdateProductsAllowed(importMetaData, productData))
    			.collect(toList());
    	return new ProductDataLists(allProductData, newProductsData, existingProductsData);
    }

    
    
    
	private boolean isNewProductsInsertAllowed(ProductImportMetadata importMetaData, ProductData product) {
		return importMetaData.isInsertNewProducts() && !product.isExisting();
	}
	
	
	
	
	private boolean isUpdateProductsAllowed(ProductImportMetadata importMetaData, ProductData product) {
		return (importMetaData.isUpdateProduct() || importMetaData.isUpdateStocks()) 
					&& product.isExisting();
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
				.findByOrganizationEntity_IdAndRemovedOrderByPriorityDesc(orgId, 0)
				.stream()
				.collect(toMap(
							brand -> brand.getName().toUpperCase() 
							, brand -> brand
							, minBy(comparing(BrandsEntity::getId))));
	}




    private ProductData toProductData(List<? extends ProductImportDTO> productDataRows, ProductImportMetadata importMetaData, DataImportCachedData cache) {
    	ProductImportDTO pivotProductRow = getPivotProductDataRow(productDataRows, cache);
    	ProductUpdateDTO productDto = createProductDto(pivotProductRow, cache);
    	List<VariantDTOWithExternalIdAndStock> productVariantsData =
    		        getVariantsData(productDataRows, importMetaData, cache, productDto);
    	
    	ProductData data = new ProductData();
        data.setOriginalRowData(productDataRows.toString());
        data.setProductDto(productDto);
        data.setVariants(productVariantsData);
        data.setTagsNames( ofNullable(pivotProductRow.getTags()).orElse(emptySet()) );
        
        return data;
    }





	private List<VariantDTOWithExternalIdAndStock> getVariantsData(List<? extends ProductImportDTO> productDataRows,
			ProductImportMetadata importMetaData, DataImportCachedData cache, ProductUpdateDTO product) {
		return productDataRows
				.stream()
				.map(row -> createVariantDto(row, importMetaData, cache, product))
				.collect(toList());
	}





	private <T extends ProductImportDTO> ProductImportDTO getPivotProductDataRow(List<T> productDataRows, DataImportCachedData cache){
		T firstProductRow = 
    			productDataRows
    			.stream()
    			.findFirst()
    			.orElseThrow(() -> new RuntimeBusinessException("No Product Data found!", "INVALID PARAM: csv", NOT_ACCEPTABLE));
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
        stock.setDiscount(row.getDiscount());
        stock.setUnit(row.getUnit());
        return stock;
    }

    
    
    

    private VariantDTOWithExternalIdAndStock createVariantDto(ProductImportDTO row, ProductImportMetadata importMetaData, DataImportCachedData cache, ProductUpdateDTO product) {
    	Map<String,String> featureNameToIdMapping = cache.getFeatureNameToIdMapping();
    	
        String features = getFeaturesAsJsonString(row, featureNameToIdMapping);        
        String extraAtrributes = getExtraAttrAsJsonString(row);        
        StockUpdateDTO variantStock = createStockDto(row, importMetaData);

        VariantDTOWithExternalIdAndStock variant = new VariantDTOWithExternalIdAndStock();
        variant.setVariantId(row.getVariantId());
        variant.setBarcode(row.getBarcode());
        variant.setDescription(row.getDescription());
        variant.setName(row.getName());
        variant.setOperation(EntityConstants.Operation.CREATE);
        variant.setPname(row.getPname());
        variant.setExternalId(row.getExternalId());
        variant.setStock(variantStock);
        variant.setProductId(product.getProductId());
        variant.setSku(row.getSku());
        variant.setProductCode(row.getProductCode());
        variant.setWeight(row.getWeight());
        
        if(extraAtrributes != null) {
        	variant.setExtraAttr(extraAtrributes);
        }
        
        Optional<VariantBasicData> variantBasicData = 
        		cachingHelper
        		.getVariantFromCache(toVariantIdentifier(row), cache.getVariantsCache());
        if(variantBasicData.isPresent()){
        	setVariantDtoAsUpdated(variant, variantBasicData.get(), features);
        }else {
        	String newVariantFeatures = isNotBlankOrNull(features)? features : "{}";
        	variant.setFeatures(newVariantFeatures);
        }

        return variant;
    }





	private void setVariantDtoAsUpdated(VariantDTOWithExternalIdAndStock variant, VariantBasicData variantEntity, String features) {
		variant.setVariantId(variantEntity.getVariantId());
		variant.setOperation(EntityConstants.Operation.UPDATE);
		variant.getStock().setVariantId(variantEntity.getVariantId());
		if (isNotBlankOrNull(features)) {
            variant.setFeatures(features);	//features are updated only if they are passed to setFeatures in the DTO
        }
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
				.filter(map -> !map.isEmpty())	//if no features are provided, return null 
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
    
    


    private ProductUpdateDTO createProductDto(ProductImportDTO row, DataImportCachedData cache){

        ProductUpdateDTO product = new ProductUpdateDTO();
        product.setDescription(row.getDescription());
        product.setBarcode(row.getBarcode());
        product.setName(row.getName());
        product.setOperation(EntityConstants.Operation.CREATE);
        product.setPname(row.getPname());
        
        ofNullable(row.getBrand())
        .map(brand -> getBrandId(row, cache.getBrandsCache()))
        .ifPresent(product::setBrandId);
        
        Optional<Long> productId = getProductId(row, cache);
        if (productId.isPresent()) {
        	product.setId(productId.get());
        	product.setOperation(UPDATE);
		}

        return product;
    }





	private Long getBrandId(ProductImportDTO row, Map<String, BrandsEntity> brandsCache) {
		return ofNullable(row.getBrand())
				.map(String::toUpperCase)
				.map(brandsCache::get)
				.map(BrandsEntity::getId)
				.orElseThrow(() ->
					new RuntimeBusinessException(
				            format(ERR_BRAND_NAME_NOT_EXIST, row.getBrand())
				            , "INVALID DATA:brand"
				            , NOT_ACCEPTABLE));
	}





	private Optional<Long> getProductId(ProductImportDTO row, DataImportCachedData cache) {
		return cachingHelper
				.getVariantFromCache(toVariantIdentifier(row), cache.getVariantsCache())
				.map(VariantBasicData::getProductId);
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
			OrganizationResponse response = organizationService.createOrganizationBrand(brandDto, null, null, null);
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
		return !brandRepo.existsByNameIgnoreCaseAndOrganizationEntity_idAndRemoved(brandName, orgId, 0);
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
class ProductDataImportContext{
	private ProductDataLists productsData;
	private ImportProductContext context;
	
	public static Optional<ProductDataImportContext> of(ProductDataLists productData
			,  ImportProductContext context){
		return Optional.of(new ProductDataImportContext(productData, context));
	} 
}



@Data
@AllArgsConstructor
class ProductDataLists{
	private List<ProductData> allProductsData;
	private List<ProductData> newProductsData;
	private List<ProductData> existingProductsData;
}



@Data
@EqualsAndHashCode(callSuper = true)
class IndexedProductImportDTO extends ProductImportDTO{
	private Integer index;
	
	public IndexedProductImportDTO(Integer index, ProductImportDTO productImportDTO) {
		try {
			copyProperties(this, productImportDTO);
			this.index = index;
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}
}




@Data
class TagAndProductId{
	private Long tagId;
	private Long productId;
	private String tagName;
}
