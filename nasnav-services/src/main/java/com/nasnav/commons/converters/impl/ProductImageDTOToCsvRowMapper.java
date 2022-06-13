package com.nasnav.commons.converters.impl;

import com.nasnav.commons.converters.DtoToCsvRowMapper;
import com.nasnav.dto.ProductImageDTO;
import com.nasnav.service.model.importproduct.csv.CsvRow;

import java.util.Map;

import static com.nasnav.service.CsvExcelDataImportService.IMG_DATA_TO_COLUMN_MAPPING;

public class ProductImageDTOToCsvRowMapper extends DtoToCsvRowMapper {

    public ProductImageDTOToCsvRowMapper() {
        this.COLUMN_MAPPING = IMG_DATA_TO_COLUMN_MAPPING;
    }

    @Override
    public CsvRow map(Object dto) {
        ProductImageDTO productImageDTO = (ProductImageDTO) dto;
        CsvRow csvRow = new CsvRow();

        csvRow.setProductId(productImageDTO.getProductId());
        csvRow.setVariantId(productImageDTO.getVariantId());
        csvRow.setBarcode(productImageDTO.getBarcode());
        csvRow.setImagePath(productImageDTO.getImagePath());

        return csvRow;
    }

    @Override
    public Map<String, String> getColumnMapping() {
        return this.COLUMN_MAPPING;
    }

}
