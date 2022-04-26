package com.nasnav.commons.converters.impl;

import com.nasnav.commons.converters.DtoToCsvRowMapper;
import com.nasnav.dto.VariantWithNoImagesDTO;
import com.nasnav.service.model.importproduct.csv.CsvRow;

import java.util.Map;

import static com.nasnav.service.CsvExcelDataImportService.IMG_DATA_TO_COLUMN_MAPPING;

public class VariantNoImagesDTOToCsvRowMapper extends DtoToCsvRowMapper {

    public VariantNoImagesDTOToCsvRowMapper() {
        this.COLUMN_MAPPING = IMG_DATA_TO_COLUMN_MAPPING;
    }

    @Override
    public CsvRow map(Object dto) {
        VariantWithNoImagesDTO variantWithNoImagesDTO = (VariantWithNoImagesDTO) dto;
        CsvRow csvRow = new CsvRow();

        csvRow.setProductId(variantWithNoImagesDTO.getProductId());
        csvRow.setVariantId(variantWithNoImagesDTO.getVariantId());
        csvRow.setBarcode(variantWithNoImagesDTO.getBarcode());
        csvRow.setName(variantWithNoImagesDTO.getProductName());
        csvRow.setExternalId(variantWithNoImagesDTO.getExternalId());

        return csvRow;
    }

    @Override
    public Map<String, String> getColumnMapping() {
        return this.COLUMN_MAPPING;
    }
}
