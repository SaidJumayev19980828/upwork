package com.nasnav.service.handler.dataimport;

import com.nasnav.commons.model.dataimport.ProductImportDTO;
import com.nasnav.commons.model.handler.*;
import com.nasnav.commons.utils.EntityUtils;
import com.nasnav.constatnts.EntityConstants;
import com.nasnav.dto.ProductImportMetadata;
import com.nasnav.dto.ProductUpdateDTO;
import com.nasnav.dto.StockUpdateDTO;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.BrandsEntity;
import com.nasnav.service.handler.Handler;
import com.nasnav.service.helpers.CachingHelper;
import com.nasnav.service.model.DataImportCachedData;
import com.nasnav.service.model.VariantBasicData;
import com.nasnav.service.model.VariantIdentifier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;

import static com.nasnav.commons.utils.EntityUtils.firstExistingValueOf;
import static com.nasnav.commons.utils.EntityUtils.noneIsNull;
import static com.nasnav.commons.utils.StringUtils.generateUUIDToken;
import static com.nasnav.commons.utils.StringUtils.isNotBlankOrNull;
import static com.nasnav.constatnts.EntityConstants.Operation.UPDATE;
import static com.nasnav.constatnts.error.dataimport.ErrorMessages.ERR_BRAND_NAME_NOT_EXIST;
import static java.lang.String.format;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.*;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;

@Service(HandlerChainFactory.PRODUCT_DATA_LIST_CONVERTER_IMPORT_DATA)
@RequiredArgsConstructor
@Slf4j
public class ProductDataListConverterHandler implements Handler<ImportDataCommand> {

    private final CachingHelper cachingHelper;

    @Override
    public void handle(final ImportDataCommand importDataCommand, HandlerChainProcessStatus status) throws Exception {

        ProductDataLists productsData = toProductDataList(importDataCommand.getProductsData(), importDataCommand.getImportMetadata(),
                importDataCommand.getCache());

        importDataCommand.setProductsDataLists(productsData);
    }

    @Override
    public String getName() {

        return HandlerChainFactory.PRODUCT_DATA_LIST_CONVERTER_IMPORT_DATA;
    }

    //TODO Check Duplication DataImportServiceImpl
    private ProductDataLists toProductDataList(List<ProductImportDTO> rows
            , ProductImportMetadata importMetaData, DataImportCachedData cache) throws  RuntimeBusinessException {

        List<ProductData> allProductData =
                IntStream
                        .range(0, rows.size())
                        .mapToObj(i -> new IndexedProductImportDTO(i, rows.get(i)))
//	    		.parallel() //transactions are not shared among threads
                        .collect(groupingBy(row -> firstExistingValueOf(row.getProductGroupKey(), row.getName(), generateUUIDToken())))
                        .values()
                        .stream()
                        .filter(EntityUtils::noneIsEmpty)
                        .map(singleProductRows -> toProductData(singleProductRows, importMetaData, cache))
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

    //TODO Check Duplication DataImportServiceImpl
    private boolean isNewProductsInsertAllowed(ProductImportMetadata importMetaData, ProductData product) {

        return importMetaData.isInsertNewProducts() && !product.isExisting();
    }

    //TODO Check Duplication DataImportServiceImpl
    private boolean isUpdateProductsAllowed(ProductImportMetadata importMetaData, ProductData product) {
        return (importMetaData.isUpdateProduct() || importMetaData.isUpdateStocks())
                && product.isExisting();
    }

    //TODO Check Duplication DataImportServiceImpl
    private ProductData toProductData(List<? extends ProductImportDTO> productDataRows, ProductImportMetadata importMetaData, DataImportCachedData cache) {

        ProductImportDTO pivotProductRow = getPivotProductDataRow(productDataRows, cache);
        ProductUpdateDTO productDto = createProductDto(pivotProductRow, cache);
        List<VariantDTOWithExternalIdAndStock> productVariantsData =
                getVariantsData(productDataRows, importMetaData, cache, productDto);

        ProductData data = new ProductData();
        data.setOriginalRowData(productDataRows.toString());
        data.setProductDto(productDto);
        data.setVariants(productVariantsData);
        data.setTagsNames(ofNullable(pivotProductRow.getTags()).orElse(emptySet()));

        return data;
    }

    //TODO Check Duplication DataImportServiceImpl
    private <T extends ProductImportDTO> ProductImportDTO getPivotProductDataRow(List<T> productDataRows, DataImportCachedData cache) {

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

    //TODO Check Duplication DataImportServiceImpl
    private boolean hasExistingVariant(ProductImportDTO productDataRow, DataImportCachedData cache) {

        return ofNullable(productDataRow)
                .map(this::toVariantIdentifier)
                .flatMap(identifier -> cachingHelper.getVariantFromCache(identifier, cache.getVariantsCache()))
                .isPresent();
    }

    //TODO Check Duplication DataImportServiceImpl
    private VariantIdentifier toVariantIdentifier(ProductImportDTO row) {

        VariantIdentifier identifier = new VariantIdentifier();
        String variantId = ofNullable(row.getVariantId()).map(String::valueOf).orElse(null);
        identifier.setVariantId(variantId);
        identifier.setExternalId(row.getExternalId());
        identifier.setBarcode(row.getBarcode());
        return identifier;
    }

    //TODO Check Duplication DataImportServiceImpl
    private ProductUpdateDTO createProductDto(ProductImportDTO row, DataImportCachedData cache) {

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

    //TODO Check Duplication DataImportServiceImpl
    private Optional<Long> getProductId(ProductImportDTO row, DataImportCachedData cache) {
        return cachingHelper
                .getVariantFromCache(toVariantIdentifier(row), cache.getVariantsCache())
                .map(VariantBasicData::getProductId);
    }

    //TODO Check Duplication DataImportServiceImpl
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

    //TODO Check Duplication DataImportServiceImpl
    private List<VariantDTOWithExternalIdAndStock> getVariantsData(List<? extends ProductImportDTO> productDataRows,
                                                                   ProductImportMetadata importMetaData, DataImportCachedData cache, ProductUpdateDTO product) {
        return productDataRows
                .stream()
                .map(row -> createVariantDto(row, importMetaData, cache, product))
                .collect(toList());
    }

    //TODO Check Duplication DataImportServiceImpl
    private VariantDTOWithExternalIdAndStock createVariantDto(ProductImportDTO row, ProductImportMetadata importMetaData, DataImportCachedData cache, ProductUpdateDTO product) {

        Map<String, String> featureNameToIdMapping = cache.getFeatureNameToIdMapping();

        Map<String, String> features = getFeatures(row, featureNameToIdMapping);
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
        variant.setFeatures(features);

        if (extraAtrributes != null) {
            variant.setVariantExtraAttr(extraAtrributes);
        }

        Optional<VariantBasicData> variantBasicData =
                cachingHelper
                        .getVariantFromCache(toVariantIdentifier(row), cache.getVariantsCache());
        if (variantBasicData.isPresent()) {
            setVariantDtoAsUpdated(variant, variantBasicData.get(), features);
        }

        return variant;
    }

    //TODO Check Duplication DataImportServiceImpl
    private String getExtraAttrAsJsonString(ProductImportDTO row) {
        return ofNullable(row.getExtraAttributes())
                .map(JSONObject::new)
                .map(JSONObject::toString)
                .orElse(null);
    }

    //TODO Check Duplication DataImportServiceImpl
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

    //TODO Check Duplication DataImportServiceImpl
    private Map<String, String> getFeatures(ProductImportDTO row, Map<String, String> featureNameToIdMapping) {
        return ofNullable(row.getFeatures())
                .map(map -> toFeaturesIdToValueMap(map, featureNameToIdMapping))
                .orElse(new HashMap<>());
    }

    //TODO Check Duplication DataImportServiceImpl
    private Map<String,String> toFeaturesIdToValueMap(Map<String,String> featuresNameToValueMap, Map<String,String> nameToIdMap){
        return ofNullable(featuresNameToValueMap)
                .orElse(emptyMap())
                .entrySet()
                .stream()
                .filter(ent -> noneIsNull(ent.getKey(), ent.getValue()))
                .collect(toMap(ent -> nameToIdMap.get(ent.getKey())
                        , Map.Entry::getValue));
    }

    //TODO Check Duplication DataImportServiceImpl
    private void setVariantDtoAsUpdated(VariantDTOWithExternalIdAndStock variant, VariantBasicData variantEntity, Map<String,String> features) {
        variant.setVariantId(variantEntity.getVariantId());
        variant.setOperation(EntityConstants.Operation.UPDATE);
        variant.getStock().setVariantId(variantEntity.getVariantId());
        if (isNotBlankOrNull(features)) {
            variant.setFeatures(features);	//features are updated only if they are passed to setFeatures in the DTO
        }
    }

}
