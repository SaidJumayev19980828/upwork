package com.nasnav.service.helpers;

import org.apache.poi.ss.usermodel.ConditionalFormattingRule;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.PatternFormatting;
import org.apache.poi.ss.usermodel.SheetConditionalFormatting;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFDataValidation;
import org.apache.poi.xssf.usermodel.XSSFDataValidationConstraint;
import org.apache.poi.xssf.usermodel.XSSFDataValidationHelper;
import org.apache.poi.xssf.usermodel.XSSFSheet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ExcelDataValidator {
    private static final Integer QUANTITY_COL_NUMBER = 0;
    private static final Integer DISCOUNT_COL_NUMBER = 2;
    private static final Integer PRODUCT_NAME_COL_NUMBER = 7;
    private static final Integer PRICE_COL_NUMBER = 11;
    private static final Integer BRAND_COL_NUMBER = 14;
    private int sheetLastRowNumber;
    private XSSFDataValidationHelper xssfDVHelper;
    List<XSSFDataValidation> validations;

    public List<XSSFDataValidation> getExcelDataValidations(XSSFSheet sheet){
        validations = new ArrayList<>();
        xssfDVHelper = new XSSFDataValidationHelper(sheet);

        addQuantityConstraint();
        addDiscountConstraint();
        addPriceConstraint();

        return validations;
    }

    private void addQuantityConstraint() {
        XSSFDataValidationConstraint quantityConstraint = (XSSFDataValidationConstraint)
                xssfDVHelper.createNumericConstraint(
                        XSSFDataValidationConstraint.ValidationType.INTEGER,
                        XSSFDataValidationConstraint.OperatorType.GREATER_OR_EQUAL,
                        "0", "");

        CellRangeAddressList quantityColumn = getCellRangeAddressListForColumn(QUANTITY_COL_NUMBER);

        XSSFDataValidation quantityValidation = (XSSFDataValidation) xssfDVHelper.createValidation(
                quantityConstraint,
                quantityColumn);

        quantityValidation.setShowErrorBox(true);
        validations.add(quantityValidation);
    }

    private void addDiscountConstraint(){
        XSSFDataValidationConstraint discountConstraint = (XSSFDataValidationConstraint)
                xssfDVHelper.createNumericConstraint(
                        XSSFDataValidationConstraint.ValidationType.DECIMAL,
                        XSSFDataValidationConstraint.OperatorType.GREATER_OR_EQUAL,
                        "0", "");

        CellRangeAddressList discountColumn = getCellRangeAddressListForColumn(DISCOUNT_COL_NUMBER);

        XSSFDataValidation discountValidation = (XSSFDataValidation)xssfDVHelper.createValidation(
                discountConstraint,
                discountColumn);

        discountValidation.setShowErrorBox(true);
        validations.add(discountValidation);
    }

    private void addPriceConstraint(){
        XSSFDataValidationConstraint priceConstraint = (XSSFDataValidationConstraint)
                xssfDVHelper.createNumericConstraint(
                        XSSFDataValidationConstraint.ValidationType.DECIMAL,
                        XSSFDataValidationConstraint.OperatorType.GREATER_THAN,
                        "0", "");

        CellRangeAddressList priceColumn = getCellRangeAddressListForColumn(PRICE_COL_NUMBER);

        XSSFDataValidation priceValidation = (XSSFDataValidation)xssfDVHelper.createValidation(
                priceConstraint,
                priceColumn);

        priceValidation.setShowErrorBox(true);
        validations.add(priceValidation);
    }

    private CellRangeAddressList getCellRangeAddressListForColumn(Integer columnNum){
        return new CellRangeAddressList(
                    -1,
                    -1,
                    columnNum,
                    columnNum);
    }

    public void setSheetConditionalFormatting(XSSFSheet sheet){
        SheetConditionalFormatting conditionalFormatting = sheet.getSheetConditionalFormatting();

        // Is blank rule
        ConditionalFormattingRule isBlankRule = conditionalFormatting.createConditionalFormattingRule("=ISBLANK(A1)");
        PatternFormatting isBlankFill = isBlankRule.createPatternFormatting();
        isBlankFill.setFillBackgroundColor(IndexedColors.GREY_25_PERCENT.index);
        isBlankFill.setFillPattern(PatternFormatting.SOLID_FOREGROUND);

        sheetLastRowNumber = sheet.getWorkbook().getSpreadsheetVersion().getLastRowIndex();

        CellRangeAddressList appliedRanges = getAppliedRangesForConditionalFormatting(
                QUANTITY_COL_NUMBER,
                DISCOUNT_COL_NUMBER,
                PRODUCT_NAME_COL_NUMBER,
                PRICE_COL_NUMBER,
                BRAND_COL_NUMBER);


        conditionalFormatting.addConditionalFormatting(appliedRanges.getCellRangeAddresses(), isBlankRule);
    }

    private CellRangeAddressList getAppliedRangesForConditionalFormatting(Integer ...columns){
        CellRangeAddressList appliedRanges = new CellRangeAddressList();
        Arrays.stream(columns)
                .forEach(column -> {
                    appliedRanges.addCellRangeAddress(
                            0,
                            column,
                            sheetLastRowNumber,
                            column);
                });

        return appliedRanges;
    }

}
