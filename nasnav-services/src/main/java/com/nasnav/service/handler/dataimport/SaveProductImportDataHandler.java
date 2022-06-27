package com.nasnav.service.handler.dataimport;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.commons.model.handler.*;
import com.nasnav.dao.TagsRepository;
import com.nasnav.dto.ProductImportMetadata;
import com.nasnav.dto.ProductUpdateDTO;
import com.nasnav.dto.StockUpdateDTO;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.exceptions.StockValidationException;
import com.nasnav.integration.IntegrationService;
import com.nasnav.persistence.TagsEntity;
import com.nasnav.service.ProductService;
import com.nasnav.service.StockService;
import com.nasnav.service.handler.Handler;
import com.nasnav.service.model.ProductTagPair;
import com.nasnav.service.model.importproduct.context.ImportProductContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.nasnav.commons.utils.EntityUtils.anyIsNull;
import static com.nasnav.constatnts.error.dataimport.ErrorMessages.ERR_CONVERT_TO_JSON;
import static com.nasnav.constatnts.error.dataimport.ErrorMessages.ERR_TAGS_NOT_FOUND;
import static com.nasnav.exceptions.ErrorCodes.TAG$TREE$0002;
import static com.nasnav.integration.enums.MappingType.PRODUCT_VARIANT;
import static java.lang.String.format;
import static java.util.Collections.emptySet;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.*;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;


@Service(HandlerChainFactory.SAVE_PRODUCT_IMPORT_DATA)
@RequiredArgsConstructor
@Slf4j
public class SaveProductImportDataHandler implements Handler<ImportDataCommand> {


    private final ProductService productService;

    private final TagsRepository tagsRepo;

    private final IntegrationService integrationService;

    private final StockService stockService;

    @Transactional
    @Override
    public void handle(final ImportDataCommand importDataCommand, HandlerChainProcessStatus status) throws Exception {

        status.markAsNotCancelable();

        saveToDB(importDataCommand.getProductsDataLists(), importDataCommand.getContext(), importDataCommand.getOrgId());

        if (importDataCommand.getImportMetadata().isDryrun() || !importDataCommand.getContext().isSuccess()
                || status.isCanceled()) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            clearImportContext(importDataCommand.getContext());
        }

    }

    @Override
    public String getName() {

        return HandlerChainFactory.SAVE_PRODUCT_IMPORT_DATA;
    }

    //TODO Check Duplication DataImportServiceImpl
    private void clearImportContext(ImportProductContext context) {

        context.getCreatedBrands().clear();
        context.getCreatedTags().clear();
        context.getCreatedProducts().clear();
        context.getUpdatedProducts().clear();
    }

    //TODO Check Duplication DataImportServiceImpl
    private void saveToDB(ProductDataLists productsData, ImportProductContext context, Long orgId) {
        //save procedure is mutable, so the product data is modified after each step
        //this can introduce annoying bugs, so, it is better we think of something better later.
        ProductDataImportContext.of(productsData, context)
                .map(productDataImportContext -> saveProducts(productDataImportContext, orgId))
                .map(productDataImportContext -> saveVariants(productDataImportContext, orgId))
                .map(this::saveStocks);
    }

    //TODO Check Duplication DataImportServiceImpl
    private ProductDataImportContext saveStocks(ProductDataImportContext context) {

        ProductImportMetadata importMetaData = context.getContext().getImportMetaData();
        boolean updateStocksEnabled = importMetaData.isUpdateStocks();
        boolean insertNewProducts = importMetaData.isInsertNewProducts();

        List<StockUpdateDTO> newStocks = getNewProductsStocks(context);
        List<StockUpdateDTO> updatedStocks = getUpdatedProductsStocks(context);

        if (insertNewProducts) {
            insertNewStocks(context, newStocks);
        }

        if (updateStocksEnabled) {
            insertUpdatedProductsStocks(context, updatedStocks);
        }

        return context;
    }

    //TODO Check Duplication DataImportServiceImpl
    private void insertUpdatedProductsStocks(ProductDataImportContext context, List<StockUpdateDTO> updatedStocks) {

        try {
            log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>> saving existing stocks ....");
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

    //TODO Check Duplication DataImportServiceImpl
    private void insertNewStocks(ProductDataImportContext context, List<StockUpdateDTO> newStocks) {

        try {
            log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>> saving new stocks ....");
            //we let updateStockBatch() recreate the variants cache, as the variants cache, as the initial cache was invalidated
            //after saving variants.
            stockService.updateStockBatch(newStocks);
        } catch (StockValidationException e) {
            context.getContext().logNewError(e, e.getErrorMessage(), e.getIndex());
            throw e;
        }
    }

    //TODO Check Duplication DataImportServiceImpl
    private List<StockUpdateDTO> getUpdatedProductsStocks(ProductDataImportContext context) {

        return getUpdatedVariants(context)
                .stream()
                .map(VariantDTOWithExternalIdAndStock::getStock)
                .collect(toList());
    }

    //TODO Check Duplication DataImportServiceImpl
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

    //TODO Check Duplication DataImportServiceImpl
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

    //TODO Check Duplication DataImportServiceImpl
    private ProductDataImportContext saveProducts(ProductDataImportContext context, Long orgId) {

        ImportProductContext importContext = context.getContext();
        ProductImportMetadata importMetaData = importContext.getImportMetaData();
        boolean updateProductEnabled = importMetaData.isUpdateProduct();
        boolean insertNewProducts = importMetaData.isInsertNewProducts();

        List<ProductData> newProducts = context.getProductsData().getNewProductsData();
        List<ProductData> existingProducts = context.getProductsData().getExistingProductsData();

        if (insertNewProducts) {
            log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>> saving New products ....");
            saveNewProducts(importContext, newProducts, orgId);
        }

        if (updateProductEnabled) {
            log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>> saving existing products ....");
            saveExistingProducts(importContext, existingProducts, orgId);
        }

        return context;
    }

    //TODO Check Duplication DataImportServiceImpl
    private void saveExistingProducts(ImportProductContext importContext, List<ProductData> existingProducts, Long orgId) {

        ProductImportMetadata importMetaData = importContext.getImportMetaData();
        boolean updateProductEnabled = importMetaData.isUpdateProduct();
        boolean updateStocksEnabled = importMetaData.isUpdateStocks();
        boolean resetTags = importMetaData.isResetTags();

        List<ProductUpdateDTO> existingProductDtos = getProductUpdateDtoList(existingProducts);
        List<Long> exisintgProductIds = saveProducts(existingProductDtos);

        for (int i = 0; i < exisintgProductIds.size(); i++) {
            Long productId = exisintgProductIds.get(i);
            ProductData data = existingProducts.get(i);
            data.getVariants().forEach(variant -> variant.setProductId(productId));
            if (updateProductEnabled || updateStocksEnabled) {
                importContext.logNewUpdatedProduct(data.getProductDto().getId(), data.getProductDto().getName());
            }
        }

        saveProductTags(existingProducts, resetTags, orgId);
    }

    //TODO Check Duplication DataImportServiceImpl
    private void saveNewProducts(ImportProductContext importContext, List<ProductData> newProducts, Long orgId) {

        boolean resetTags = importContext.getImportMetaData().isResetTags();
        List<ProductUpdateDTO> productDtos = getProductUpdateDtoList(newProducts);
        List<Long> newProductIds = saveProducts(productDtos);

        for (int i = 0; i < newProductIds.size(); i++) {
            Long productId = newProductIds.get(i);
            ProductData data = newProducts.get(i);
            data.getProductDto().setId(productId);
            data.getVariants().forEach(variant -> variant.setProductId(productId));

            importContext.logNewCreatedProduct(productId, data.getProductDto().getName());
        }

        saveProductTags(newProducts, resetTags, orgId);
    }

    //TODO Check Duplication DataImportServiceImpl
    private List<Long> saveProducts(List<ProductUpdateDTO> dtoList) {

        List<String> productJsonList =
                dtoList
                        .stream()
                        .map(this::getProductDtoJson)
                        .collect(toList());
        return productService.updateProductBatch(productJsonList, false, false);
    }

    //TODO Check Duplication DataImportServiceImpl
    private List<ProductUpdateDTO> getProductUpdateDtoList(List<ProductData> newProducts) {

        return newProducts
                .stream()
                .map(ProductData::getProductDto)
                .collect(toList());
    }

    //TODO Check Duplication DataImportServiceImpl
    private String getProductDtoJson(ProductUpdateDTO dto) {

        String productDtoJson = "";
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            productDtoJson = mapper.writeValueAsString(dto);
        } catch (Exception e) {
            log.error("Get Product DTO JSON ", e);
            throw new RuntimeBusinessException(
                    format(ERR_CONVERT_TO_JSON, dto.getClass().getName())
                    , "INTERNAL SERVER ERROR"
                    , INTERNAL_SERVER_ERROR);
        }
        return productDtoJson;
    }

    //TODO Check Duplication DataImportServiceImpl
    private void saveProductTags(List<ProductData> data, boolean isResetTags, Long orgId) {

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

    //TODO Check Duplication DataImportServiceImpl
    private void saveProductsTagsToDB(Map<String, TagsEntity> tagsMap, List<ProductData> data, boolean isResetTags) {

        Set<ProductTagPair> productTags =
                data
                        .parallelStream()
                        .map(product -> toTagAndProductIdPairs(product, tagsMap))
                        .flatMap(List::stream)
                        .collect(toSet());

        List<Long> productIds =
                data
                        .parallelStream()
                        .map(ProductData::getProductDto)
                        .map(ProductUpdateDTO::getId)
                        .filter(Objects::nonNull)
                        .collect(toList());

        if (isResetTags && !productTags.isEmpty()) {
            productService.deleteAllTagsForProducts(productIds);
        }

        productService.addTagsToProducts(productTags);
    }

    //TODO Check Duplication DataImportServiceImpl
    private List<ProductTagPair> toTagAndProductIdPairs(ProductData data, Map<String, TagsEntity> tagsMap) {

        return data
                .getTagsNames()
                .stream()
                .map(tagName -> toTagAndProductIdPair(data, tagsMap, tagName))
                .collect(toList());
    }

    //TODO Check Duplication DataImportServiceImpl
    private ProductTagPair toTagAndProductIdPair(ProductData data, Map<String, TagsEntity> tagsMap, String tagName) {

        Long tagId = ofNullable(tagsMap.get(tagName.toLowerCase()))
                .map(TagsEntity::getId)
                .orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, TAG$TREE$0002, tagName));
        return new ProductTagPair(data.getProductDto().getId(), tagId);
    }

    //TODO Check Duplication DataImportServiceImpl
    private void validateTags(Long orgId, Set<String> tagsNames, Map<String, TagsEntity> tagsMap) {

        Set<String> nonExistingTags =
                tagsNames
                        .stream()
                        .filter(tagName -> !tagsMap.containsKey(tagName.toLowerCase()))
                        .collect(toSet());

        if (!nonExistingTags.isEmpty()) {
            throw new RuntimeBusinessException(
                    format(ERR_TAGS_NOT_FOUND, nonExistingTags, orgId)
                    , "INVLAID PRODUCT DATA"
                    , NOT_ACCEPTABLE
            );
        }
    }

    //TODO Check Duplication DataImportServiceImpl
    private Map<String, TagsEntity> getTagsNamesMap(Long orgId, Set<String> tagsNames) {

        Set<String> tagNamesInLowerCase = toLowerCase(tagsNames);
        return findTagsNameLowerCaseInAndOrganizationId(tagNamesInLowerCase, orgId)
                .stream()
                .collect(toMap(t -> t.getName().toLowerCase(), Function.identity(), this::pickLowerId));
    }

    //TODO Check Duplication DataImportServiceImpl
    private TagsEntity pickLowerId(TagsEntity tag1, TagsEntity tag2) {

        return Stream.of(tag1, tag2)
                .min(Comparator.comparing(TagsEntity::getId))
                .get();
    }

    //TODO Check Duplication DataImportServiceImpl
    private Set<TagsEntity> findTagsNameLowerCaseInAndOrganizationId(Set<String> tagNamesInLowerCase, Long orgId) {

        if (tagNamesInLowerCase == null || tagNamesInLowerCase.isEmpty()) {
            return emptySet();
        }
        return tagsRepo.findByNameLowerCaseInAndOrganizationEntity_Id(tagNamesInLowerCase, orgId);
    }

    //TODO Check Duplication DataImportServiceImpl
    private Set<String> toLowerCase(Set<String> tagsNames) {

        return tagsNames
                .stream()
                .map(String::toLowerCase)
                .collect(toSet());
    }

    //TODO Check Duplication DataImportServiceImpl
    private ProductDataImportContext saveVariants(ProductDataImportContext context, Long orgId) {

        if (context.getContext().getImportMetaData().isInsertNewProducts()) {
            saveNewProductsVariants(context, orgId);
        }

        if (context.getContext().getImportMetaData().isUpdateProduct()) {
            saveExistingProductsVariants(context, orgId);
        }

        return context;
    }

    //TODO Check Duplication DataImportServiceImpl
    private void saveNewProductsVariants(ProductDataImportContext context, Long orgId) {

        log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>> saving New variants ....");
        List<ProductData> productsData = context.getProductsData().getNewProductsData();
        saveVariants(productsData, orgId);
    }

    //TODO Check Duplication DataImportServiceImpl
    private void saveExistingProductsVariants(ProductDataImportContext context, Long orgId) {

        log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>> saving Existing variants ....");
        List<ProductData> productsData = context.getProductsData().getExistingProductsData();
        saveVariants(productsData, orgId);
    }

    //TODO Check Duplication DataImportServiceImpl
    private void saveVariants(List<ProductData> productsData, Long orgId) {

        try {
            List<VariantDTOWithExternalIdAndStock> variants = getVariants(productsData);
            List<Long> savedVariants = productService.updateVariantBatch(variants);

            for (int i = 0; i < variants.size(); i++) {
                Long variantId = savedVariants.get(i);
                VariantDTOWithExternalIdAndStock variant = variants.get(i);
                saveExternalMapping(variant, variantId, orgId);
                variant.getStock().setVariantId(variantId);
            }
        } catch (BusinessException e) {
            log.error("Save Variants ", e);
            throw new RuntimeBusinessException(e);
        }
    }

    //TODO Check Duplication DataImportServiceImpl
    private void saveExternalMapping(VariantDTOWithExternalIdAndStock variant, Long variantId, Long orgId) throws BusinessException {

        if (!anyIsNull(variant.getExternalId(), variantId)) {
            integrationService.addMappedValue(orgId, PRODUCT_VARIANT, variantId.toString(), variant.getExternalId());
        }
    }

    //TODO Check Duplication DataImportServiceImpl
    private List<VariantDTOWithExternalIdAndStock> getVariants(List<ProductData> products) {

        return
                products
                        .stream()
                        .map(ProductData::getVariants)
                        .flatMap(List::stream)
                        .collect(toList());
    }

}
