package com.nasnav.service.helpers;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static org.apache.poi.ss.usermodel.DataValidationConstraint.OperatorType.GREATER_OR_EQUAL;
import static org.apache.poi.ss.usermodel.DataValidationConstraint.OperatorType.GREATER_THAN;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;
import static org.springframework.context.annotation.ScopedProxyMode.TARGET_CLASS;

@Component
@Scope(value = SCOPE_PROTOTYPE, proxyMode = TARGET_CLASS)
public class ExcelDataValidator {
    private Integer quantityColumn;
    private Integer discountColumn;
    private Integer priceColumn;
    private XSSFDataValidationHelper xssfDVHelper;
    private XSSFSheet sheet;

    public void addDataValidationsToSheet(XSSFSheet sheet) {
        initialize(sheet);

        addQuantityConstraintToSheet();
        addDiscountConstraintToSheet();
        addPriceConstraintToSheet();
    }

    private void initialize(XSSFSheet sheet){
        xssfDVHelper = new XSSFDataValidationHelper(sheet);
        this.sheet = sheet;

        assignColumnsNumbersFromHeaders();
    }

    private void assignColumnsNumbersFromHeaders() {
        XSSFRow row = sheet.getRow(0);

        row.iterator().forEachRemaining(cell -> {
            mapCellIndexToColumnNumber(cell);
        });

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
        }
    }

    private void addQuantityConstraintToSheet() {
        XSSFDataValidationConstraint quantityConstraint = getIntegerConstraint(GREATER_OR_EQUAL);

        XSSFDataValidation quantityValidation = getDataValidationForColumn(quantityColumn, quantityConstraint);

        setCustomErrorMessage(quantityValidation, "Quantities must be greater than or equal zero and non-fraction values");
        sheet.addValidationData(quantityValidation);
    }

    private XSSFDataValidationConstraint getIntegerConstraint(Integer operatorType) {
        return (XSSFDataValidationConstraint) xssfDVHelper.createNumericConstraint(
                                                            XSSFDataValidationConstraint.ValidationType.INTEGER,
                                                            operatorType,
                                                           "0", "");
    }

    private void addDiscountConstraintToSheet() {
        XSSFDataValidationConstraint discountConstraint = getDecimalConstraint(GREATER_OR_EQUAL);

        XSSFDataValidation discountValidation = getDataValidationForColumn(discountColumn, discountConstraint);

        setCustomErrorMessage(discountValidation, "Discounts must be greater than or equal zero");
        sheet.addValidationData(discountValidation);
    }

    private void addPriceConstraintToSheet() {
        XSSFDataValidationConstraint priceConstraint = getDecimalConstraint(GREATER_THAN);

        XSSFDataValidation priceValidation = getDataValidationForColumn(priceColumn, priceConstraint);

        setCustomErrorMessage(priceValidation, "Prices must be greater than zero");
        sheet.addValidationData(priceValidation);
    }

    private XSSFDataValidation getDataValidationForColumn(Integer column, XSSFDataValidationConstraint constraint){
        CellRangeAddressList cellsRange = getCellRangeAddressListForColumn(column);

        return (XSSFDataValidation) xssfDVHelper.createValidation(
                constraint,
                cellsRange);
    }

    private void setCustomErrorMessage(XSSFDataValidation validation, String errorMessage){
        validation.setErrorStyle(DataValidation.ErrorStyle.STOP);
        validation.createErrorBox("Invalid input", errorMessage);
        validation.setShowErrorBox(true);
    }

    private XSSFDataValidationConstraint getDecimalConstraint(int operatorType) {
        return (XSSFDataValidationConstraint) xssfDVHelper.createNumericConstraint(
                XSSFDataValidationConstraint.ValidationType.DECIMAL,
                operatorType,
                "0", "");
    }

    private CellRangeAddressList getCellRangeAddressListForColumn(Integer columnNum) {
        return new CellRangeAddressList(
                -1,
                -1,
                columnNum,
                columnNum);
    }
}