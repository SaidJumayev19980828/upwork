package com.nasnav.service.handler.dataimport;

import com.nasnav.commons.model.dataimport.ProductImportDTO;
import com.nasnav.commons.model.handler.HandlerChainProcessStatus;
import com.nasnav.commons.model.handler.ImportDataCommand;
import com.nasnav.dao.BrandsRepository;
import com.nasnav.dao.ProductFeaturesRepository;
import com.nasnav.persistence.BrandsEntity;
import com.nasnav.persistence.ProductFeaturesEntity;
import com.nasnav.service.handler.Handler;
import com.nasnav.service.helpers.CachingHelper;
import com.nasnav.service.model.DataImportCachedData;
import com.nasnav.service.model.VariantCache;
import com.nasnav.service.model.VariantIdentifier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static java.util.Comparator.comparing;
import static java.util.Optional.ofNullable;
import static java.util.function.BinaryOperator.minBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@Service(HandlerChainFactory.CREATE_CACHE_PRODUCT_IMPORT_DATA)
@RequiredArgsConstructor
@Slf4j
public class CreateCacheProductImportDataHandler implements Handler<ImportDataCommand> {

    private final ProductFeaturesRepository featureRepo;

    private final BrandsRepository brandRepo;

    private final CachingHelper cachingHelper;

    @Override
    public void handle(final ImportDataCommand importDataCommand, HandlerChainProcessStatus status) throws Exception {

        importDataCommand.setCache(createRequiredDataCache(importDataCommand.getProductsData(),importDataCommand.getOrgId()));
    }

    @Override
    public String getName() {

        return HandlerChainFactory.CREATE_CACHE_PRODUCT_IMPORT_DATA;
    }

    //TODO Check Duplication DataImportServiceImpl
    private DataImportCachedData createRequiredDataCache(List<ProductImportDTO> rows,Long orgId) {

        Map<String, String> featureNameToIdMapping = getFeatureNameToIdMap(orgId);
        Map<String, BrandsEntity> brandNameToIdMapping = getBrandsMapping(orgId);
        VariantCache variantCache = createVariantsCache(rows);
        return new DataImportCachedData(featureNameToIdMapping, brandNameToIdMapping, variantCache);
    }

    //TODO Check Duplication DataImportServiceImpl
    private Map<String, String> getFeatureNameToIdMap(Long orgId) {

        return
                featureRepo
                        .findByOrganizationId(orgId)
                        .stream()
                        .collect(toMap(ProductFeaturesEntity::getName, this::getFeatureIdAsStr));
    }

    //TODO Check Duplication DataImportServiceImpl
    private Map<String, BrandsEntity> getBrandsMapping(Long orgId) {

        return brandRepo
                .findByOrganizationEntity_IdAndRemovedOrderByPriorityDesc(orgId, 0)
                .stream()
                .collect(toMap(
                        brand -> brand.getName().toUpperCase()
                        , brand -> brand
                        , minBy(comparing(BrandsEntity::getId))));
    }

    //TODO Check Duplication DataImportServiceImpl
    private VariantCache createVariantsCache(List<ProductImportDTO> rows) {

        List<VariantIdentifier> variantIdentifiers = toVariantIdentifiers(rows);
        return cachingHelper.createVariantCache(variantIdentifiers);
    }

    //TODO Check Duplication DataImportServiceImpl
    private List<VariantIdentifier> toVariantIdentifiers(List<ProductImportDTO> rows) {

        return rows
                .stream()
                .map(this::toVariantIdentifier)
                .collect(toList());
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
    private String getFeatureIdAsStr(ProductFeaturesEntity feature) {

        return String.valueOf(feature.getId());
    }

}
