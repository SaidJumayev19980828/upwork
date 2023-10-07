package com.nasnav.service.handler.dataimport;

import com.nasnav.commons.model.dataimport.ProductImportDTO;
import com.nasnav.commons.model.handler.HandlerChainProcessStatus;
import com.nasnav.commons.model.handler.ImportDataCommand;
import com.nasnav.commons.utils.FunctionalUtils;
import com.nasnav.dao.ExtraAttributesRepository;
import com.nasnav.dao.ProductFeaturesRepository;
import com.nasnav.dto.ProductImportMetadata;
import com.nasnav.dto.ProductListImportDTO;
import com.nasnav.exceptions.ImportProductException;
import com.nasnav.persistence.ExtraAttributesEntity;
import com.nasnav.persistence.ProductFeaturesEntity;
import com.nasnav.service.CsvExcelDataImportService;
import com.nasnav.service.handler.Handler;
import com.nasnav.service.model.importproduct.context.ImportProductContext;
import com.nasnav.service.model.importproduct.csv.CsvRow;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toUnmodifiableMap;

@Service(HandlerChainFactory.PARSE_EXCEL_PRODUCT_IMPORT_DATA)
@RequiredArgsConstructor
@Slf4j
public class ParseExcelProductImportDataHandler implements Handler<ImportDataCommand> {

    private static final Map<String, String> HEADER_NAME_TO_BEAN_PROPERTY_MAPPING =
            getHeaderNameToBeanPropertyMapping();

    private final ExtraAttributesRepository extraAttrRepo;

    private final ProductFeaturesRepository featureRepo;

    public void handle(ImportDataCommand importDataCommand, HandlerChainProcessStatus status) throws Exception {

        ProductImportMetadata importMetadata = getImportMetaData(importDataCommand.getImportMetaDataDto());
        ImportProductContext initialContext = new ImportProductContext(emptyList(), importMetadata);

        List<CsvRow> rows = parseExcelFile(importDataCommand.getFile(), initialContext, importDataCommand.getOrgId());
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

        return HandlerChainFactory.PARSE_EXCEL_PRODUCT_IMPORT_DATA;
    }


    //TODO Check Duplication AbstractCsvExcelDataImportService
    private ProductImportMetadata getImportMetaData(ProductListImportDTO csvImportMetaData) {

        var importMetadata = new ProductImportMetadata();

        importMetadata.setDryrun(csvImportMetaData.isDryrun());
        importMetadata.setUpdateProduct(csvImportMetaData.isUpdateProduct());
        importMetadata.setUpdateStocks(csvImportMetaData.isUpdateStocks());
        importMetadata.setShopId(csvImportMetaData.getShopId());
        importMetadata.setCurrency(csvImportMetaData.getCurrency());
        importMetadata.setEncoding(csvImportMetaData.getEncoding());
        importMetadata.setDeleteOldProducts(csvImportMetaData.isDeleteOldProducts());
        importMetadata.setResetTags(csvImportMetaData.isResetTags());
        importMetadata.setInsertNewProducts(csvImportMetaData.isInsertNewProducts());

        return importMetadata;
    }

    //TODO Check Duplication ExcelDataImportServiceImpl
    private List<CsvRow> parseExcelFile(byte[] file, ImportProductContext initialContext, final Long orgId) throws ImportProductException {

        List<CsvRow> lines;
        try {
            Workbook wb = WorkbookFactory.create(new ByteArrayInputStream(file));
            Sheet sheet = wb.getSheetAt(0);
            validateFileHeader(sheet.getRow(0),orgId);
            lines = readImpDataLines(sheet,orgId);

            wb.close();
        } catch (ImportProductException ex) {
            log.error("Parse Excel file  ", ex);
            throw new ImportProductException(ex, ex.getContext());
        } catch (Exception e) {
            log.error("Parse Excel file  ", e);
            throw new ImportProductException(e, initialContext);
        }

        return lines;
    }

    //TODO Check Duplication ExcelDataImportServiceImpl
    private void validateFileHeader(Row row, Long orgId) throws Exception {

        List<String> headers = new ArrayList<>();
        for (Cell cell : row) {
            headers.add(cell.getStringCellValue());
        }
        List<String> originalHeaders = new ArrayList<>(CsvExcelDataImportService.CSV_BASE_HEADERS);
        String headerNotFound = originalHeaders.stream().filter(header -> !headers.contains(header)).map(Object::toString).collect(Collectors.joining(","));
        if (!headerNotFound.isEmpty()) {
            ImportProductContext context = new ImportProductContext();
            context.logNewError("The following table header(s) not found : [ " + headerNotFound + " ]", 1);
            throw new ImportProductException(context);
        }
    }

    //TODO Check Duplication ExcelDataImportServiceImpl
    private List<CsvRow> readImpDataLines(Sheet sheet, Long orgId) throws InvocationTargetException, IllegalAccessException {

        List<CsvRow> lines = new ArrayList<>();
        List<String> featuresNames = featureRepo.findByOrganizationId(orgId)
                .stream().map(ProductFeaturesEntity::getName)
                .collect(toList());
        List<String> extraAttributesNames = extraAttrRepo.findByOrganizationId(orgId)
                .stream().map(ExtraAttributesEntity::getName)
                .collect(toList());

        for (Row row : sheet) {
            CsvRow line = new CsvRow();
            Map<String, String> features = new HashMap<>();
            Map<String, String> extraAttributes = new HashMap<>();
            if (row.getRowNum() == 0) {
                continue; // skip header row
            }
            for (Cell cell : row) {
                String headerName = sheet.getRow(0).getCell(cell.getColumnIndex()).getStringCellValue();
                var propertyName = getColumnHeaderMapping(headerName);
                Object value = getCellValue(cell);
                if (value != null) {
                    BeanUtils.setProperty(line, propertyName, value);
                    if (featuresNames.contains(propertyName)) {
                        features.put(propertyName, value.toString());
                    }
                    if (extraAttributesNames.contains(propertyName)) {
                        extraAttributes.put(propertyName, value.toString());
                    }
                }
            }
            line.setFeatures(features);
            line.setExtraAttributes(extraAttributes);
            lines.add(line);
        }
        return lines;
    }

    //TODO Check Duplication ExcelDataImportServiceImpl
    private static Object getCellValue(Cell cell) {

        switch (cell.getCellType()) {
            case NUMERIC: {
                double number = cell.getNumericCellValue();
                if (number == (int) number) {
                    return (int) number;
                }
                return BigDecimal.valueOf(cell.getNumericCellValue());
            }
            case STRING:
                return cell.getStringCellValue();
            case BOOLEAN:
                return Boolean.toString(cell.getBooleanCellValue());
        }
        return null;
    }

    //TODO Check Duplication ExcelDataImportServiceImpl
    private String getColumnHeaderMapping(String headerName) {

        return HEADER_NAME_TO_BEAN_PROPERTY_MAPPING.getOrDefault(headerName, headerName);
    }

    //TODO Check Duplication ExcelDataImportServiceImpl
    private static Map<String, String> getHeaderNameToBeanPropertyMapping() {

        return CsvExcelDataImportService.PRODUCT_DATA_TO_COLUMN_MAPPING
                .entrySet()
                .stream()
                .collect(toUnmodifiableMap(Map.Entry::getValue, Map.Entry::getKey, FunctionalUtils::getFirst));
    }

}
