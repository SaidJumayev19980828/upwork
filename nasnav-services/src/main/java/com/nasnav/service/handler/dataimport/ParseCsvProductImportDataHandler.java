package com.nasnav.service.handler.dataimport;

import com.nasnav.commons.model.dataimport.ProductImportDTO;
import com.nasnav.commons.model.handler.HandlerChainProcessStatus;
import com.nasnav.commons.model.handler.ImportDataCommand;
import com.nasnav.commons.model.handler.RowParseErrorHandler;
import com.nasnav.dao.ProductFeaturesRepository;
import com.nasnav.dto.ProductImportMetadata;
import com.nasnav.dto.ProductListImportDTO;
import com.nasnav.exceptions.ImportProductException;
import com.nasnav.persistence.ProductFeaturesEntity;
import com.nasnav.service.CsvExcelDataImportService;
import com.nasnav.service.handler.Handler;
import com.nasnav.service.helpers.ProductCsvRowProcessor;
import com.nasnav.service.model.importproduct.context.ImportProductContext;
import com.nasnav.service.model.importproduct.csv.CsvRow;
import com.nasnav.service.model.importproduct.context.Error;
import com.univocity.parsers.common.fields.ColumnMapping;
import com.univocity.parsers.common.processor.BeanListProcessor;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

@Service(HandlerChainFactory.PARSE_CSV_PRODUCT_IMPORT_DATA)
@RequiredArgsConstructor
@Slf4j
public class ParseCsvProductImportDataHandler implements Handler<ImportDataCommand> {

    private final ProductFeaturesRepository featureRepo;

    public void handle(ImportDataCommand importDataCommand, HandlerChainProcessStatus status) throws Exception {

        ProductImportMetadata importMetadata = getImportMetaData(importDataCommand.getImportMetaDataDto());
        ImportProductContext initialContext = new ImportProductContext(emptyList(), importMetadata);

        List<CsvRow> rows = parseCsvFile(importDataCommand.getFile(), importDataCommand.getImportMetaDataDto(), initialContext
        ,importDataCommand.getOrgId());
        List<ProductImportDTO> productsData =
                rows
                        .stream()
                        .map(CsvRow::toProductImportDto)
                        .collect(toList());

        importDataCommand.setProductsData(productsData);
        importDataCommand.setImportMetadata(importMetadata);
    }

    @Override
    public String getName() {

        return HandlerChainFactory.PARSE_CSV_PRODUCT_IMPORT_DATA;
    }


    //TODO Check Duplication AbstractCsvExcelDataImportService
    private ProductImportMetadata getImportMetaData(ProductListImportDTO csvImportMetaData) {

        var importMetadata = new ProductImportMetadata();

        importMetadata.setDryrun(csvImportMetaData.isDryrun());
        importMetadata.setUpdateProduct(csvImportMetaData.isUpdateProduct());
        importMetadata.setUpdateStocks(csvImportMetaData.isUpdateStocks());
        importMetadata.setShopIds(csvImportMetaData.getShopIds());
        importMetadata.setCurrency(csvImportMetaData.getCurrency());
        importMetadata.setEncoding(csvImportMetaData.getEncoding());
        importMetadata.setDeleteOldProducts(csvImportMetaData.isDeleteOldProducts());
        importMetadata.setResetTags(csvImportMetaData.isResetTags());
        importMetadata.setInsertNewProducts(csvImportMetaData.isInsertNewProducts());

        return importMetadata;
    }


    private List<CsvRow> parseCsvFile(byte[] file, ProductListImportDTO metaData, ImportProductContext context,Long orgId) throws ImportProductException {
        List<ProductFeaturesEntity> orgFeatures = new ArrayList<>();
        metaData.getShopIds().forEach(shopId -> {
            orgFeatures.addAll(featureRepo.findByShopId(shopId));
        });

        ByteArrayInputStream in = new ByteArrayInputStream(file);
        BeanListProcessor<CsvRow> rowProcessor = createRowProcessor(orgFeatures,orgId);
        RowParseErrorHandler rowParsingErrHandler = new RowParseErrorHandler(context);
        CsvParserSettings settings = createParsingSettings(rowProcessor, rowParsingErrHandler);

        CsvParser parser = new CsvParser(settings);

        runCsvParser(in, rowParsingErrHandler, parser, metaData.getEncoding());

        return rowProcessor.getBeans();
    }


    private void runCsvParser(ByteArrayInputStream in, RowParseErrorHandler rowParsingErrHandler, CsvParser parser, String encoding)
            throws ImportProductException {

        ImportProductContext context = rowParsingErrHandler.getImportContext();
        try {
            parser.parse(in, encoding);
        } catch (Exception e) {
            log.error("Run CSV Parser", e);
            context.getErrors().add(new Error(e.getMessage(), null));
            throw new ImportProductException(e, context);
        }

        if (!context.isSuccess()) {
            throw new ImportProductException(context);
        }
    }

    private CsvParserSettings createParsingSettings(BeanListProcessor<CsvRow> rowProcessor,
                                                    RowParseErrorHandler rowParsingErrHandler) {

        CsvParserSettings settings = new CsvParserSettings();
        settings.setLineSeparatorDetectionEnabled(true);
        settings.setHeaderExtractionEnabled(true);
        settings.setProcessor(rowProcessor);
        settings.setProcessorErrorHandler(rowParsingErrHandler);
        settings.setMaxCharsPerColumn(-1);
        return settings;
    }


    private BeanListProcessor<CsvRow> createRowProcessor(List<ProductFeaturesEntity> orgFeatures,Long orgId) {

        List<String> defaultTemplateHeaders = getProductImportTemplateHeadersWithoutExtraAttributes(orgId);

        ColumnMapping mapper = createAttrToColMapping();

        BeanListProcessor<CsvRow> rowProcessor =
                new ProductCsvRowProcessor<>(CsvRow.class, orgFeatures, defaultTemplateHeaders);
        rowProcessor.setColumnMapper(mapper);
        rowProcessor.setStrictHeaderValidationEnabled(true);
        return rowProcessor;
    }

    public List<String> getProductImportTemplateHeadersWithoutExtraAttributes(Long orgId) {

        var features =
                featureRepo
                        .findByOrganizationId(orgId)
                        .stream()
                        .map(ProductFeaturesEntity::getName)
                        .sorted()
                        .collect(toList());

        List<String> baseHeaders = new ArrayList<>(CsvExcelDataImportService.CSV_BASE_HEADERS);
        baseHeaders.addAll(features);
        return baseHeaders;
    }


    private ColumnMapping createAttrToColMapping() {

        ColumnMapping mapping = new ColumnMapping();
        mapping.attributesToColumnNames(CsvExcelDataImportService.PRODUCT_DATA_TO_COLUMN_MAPPING);

        return mapping;
    }

}
