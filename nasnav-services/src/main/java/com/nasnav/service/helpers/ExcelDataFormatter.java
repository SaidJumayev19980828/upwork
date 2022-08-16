package com.nasnav.service.helpers;

import com.nasnav.dao.ExtraAttributesRepository;
import com.nasnav.dao.ProductFeaturesRepository;
import com.nasnav.persistence.ExtraAttributesEntity;
import com.nasnav.persistence.ProductFeaturesEntity;
import com.nasnav.service.SecurityService;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;
import static org.springframework.context.annotation.ScopedProxyMode.TARGET_CLASS;

@Component
@Scope(value = SCOPE_PROTOTYPE, proxyMode = TARGET_CLASS)
public class ExcelDataFormatter {
    @Autowired
    protected SecurityService security;
    @Autowired
    private ProductFeaturesRepository featureRepo;
    @Autowired
    protected ExtraAttributesRepository extraAttrRepo;

    private Integer quantityColumn;
    private Integer discountColumn;
    private Integer priceColumn;
    private Integer brandColumn;
    private Integer productNameColumn;
    private int sheetLastRowNumber;
    private XSSFSheet sheet;
    private SheetConditionalFormatting conditionalFormatting;
    private List<Integer> extraAttributesColumns;
    private Set<String> extraAttributesNames;
    private List<Integer> variantFeaturesColumns;
    private Set<String> variantFeaturesNames;

    public void addConditionalFormattingToSheet(XSSFSheet sheet) {
        initialize(sheet);

        sheetLastRowNumber = sheet.getWorkbook().getSpreadsheetVersion().getLastRowIndex();
        conditionalFormatting = sheet.getSheetConditionalFormatting();

        ConditionalFormattingRule blankCellRule = getBlankCellRule();

        addConditionalFormatting(blankCellRule);
    }

    private ConditionalFormattingRule getBlankCellRule(){
        ConditionalFormattingRule blankCellRule = conditionalFormatting.createConditionalFormattingRule("=ISBLANK(A1)");
        PatternFormatting isBlankFill = blankCellRule.createPatternFormatting();

        isBlankFill.setFillBackgroundColor(IndexedColors.DARK_RED.index);
        isBlankFill.setFillPattern(PatternFormatting.SOLID_FOREGROUND);

        return blankCellRule;
    }

    private void addConditionalFormatting(ConditionalFormattingRule rule){
        CellRangeAddressList appliedRanges = getAppliedRangesForConditionalFormatting();
        conditionalFormatting.addConditionalFormatting(appliedRanges.getCellRangeAddresses(), rule);
    }

    private void initialize(XSSFSheet sheet){
        variantFeaturesColumns = new ArrayList<>();
        extraAttributesColumns = new ArrayList<>();
        this.sheet = sheet;

        assignColumnsNumbersFromHeaders();
    }

    private void assignColumnsNumbersFromHeaders() {
        XSSFRow row = sheet.getRow(0);
        setExtraAttributesNames();
        setVariantFeaturesNames();
        row.iterator().forEachRemaining(cell -> {
            mapCellIndexToColumnNumber(cell);
        });
    }

    private void setExtraAttributesNames(){
        var orgId = security.getCurrentUserOrganizationId();
        extraAttributesNames =
                extraAttrRepo.findByOrganizationId(orgId)
                        .stream()
                        .map(ExtraAttributesEntity::getName)
                        .collect(toSet());
    }

    private void setVariantFeaturesNames(){
        var orgId = security.getCurrentUserOrganizationId();
        variantFeaturesNames =
                featureRepo.findByOrganizationId(orgId)
                        .stream()
                        .map(ProductFeaturesEntity::getName)
                        .collect(toSet());
    }

    private void mapCellIndexToColumnNumber(Cell cell) {
        DataFormatter formatter = new DataFormatter();
        String header = formatter.formatCellValue(cell);

        if(header.equalsIgnoreCase("price")){
            priceColumn = cell.getColumnIndex();
        }else if (header.equalsIgnoreCase("discount")){
            discountColumn = cell.getColumnIndex();
        }else if (header.equalsIgnoreCase("quantity")){
            quantityColumn = cell.getColumnIndex();
        }else if (header.equalsIgnoreCase("product_name")){
            productNameColumn = cell.getColumnIndex();
        }else if (header.equalsIgnoreCase("brand")){
            brandColumn = cell.getColumnIndex();
        }else if (extraAttributesNames.contains(header)){
            extraAttributesColumns.add(cell.getColumnIndex());
        }else if (variantFeaturesNames.contains(header)){
            variantFeaturesColumns.add(cell.getColumnIndex());
        }
    }

    private CellRangeAddressList getAppliedRangesForConditionalFormatting() {
        CellRangeAddressList appliedRanges = new CellRangeAddressList();
        List<Integer> columns =
                Stream.of(quantityColumn,
                                discountColumn,
                                productNameColumn,
                                priceColumn,
                                brandColumn)
                        .collect(toList());

        columns.forEach(column -> {
            appliedRanges.addCellRangeAddress(
                    0,
                    column,
                    sheetLastRowNumber,
                    column);
        });

        return appliedRanges;
    }

    public void addStyleFormattingToSheet(XSSFSheet sheet) {
        initialize(sheet);
        addNumericFormatting(sheet);
        addTextFormatting(sheet);
    }

    private void addTextFormatting(XSSFSheet sheet){
        XSSFWorkbook workbook = sheet.getWorkbook();
        CellStyle textStyle = workbook.createCellStyle();
        textStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("@"));

        extraAttributesColumns.forEach(col -> {
            sheet.setDefaultColumnStyle(col, textStyle);
            changeExistingCellsFormat(sheet, textStyle, col);
        });

        variantFeaturesColumns.forEach(col -> {
            sheet.setDefaultColumnStyle(col, textStyle);
            changeExistingCellsFormat(sheet, textStyle, col);
        });
    }

    private void addNumericFormatting(XSSFSheet sheet){
        XSSFWorkbook workbook = sheet.getWorkbook();
        CellStyle integerStyle = workbook.createCellStyle();
        CellStyle decimalStyle = workbook.createCellStyle();
        integerStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("0"));
        decimalStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("0.00"));

        sheet.setDefaultColumnStyle(quantityColumn, integerStyle);
        changeExistingCellsFormat(sheet, integerStyle, quantityColumn);
        sheet.setDefaultColumnStyle(priceColumn, decimalStyle);
        changeExistingCellsFormat(sheet, decimalStyle, priceColumn);
        sheet.setDefaultColumnStyle(discountColumn, decimalStyle);
        changeExistingCellsFormat(sheet, decimalStyle, discountColumn);
    }

    private void changeExistingCellsFormat(XSSFSheet sheet, CellStyle style, Integer column){
        int lastRowNumber = sheet.getLastRowNum();

        for(int row = 1; row <= lastRowNumber; row++){
            XSSFCell cell = sheet.getRow(row).getCell(column);
            cell.setCellStyle(style);
        }
    }
}
