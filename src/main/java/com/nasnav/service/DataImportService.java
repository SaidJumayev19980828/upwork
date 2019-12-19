package com.nasnav.service;

import com.nasnav.commons.model.dataimport.ProductImportDTO;
import com.nasnav.dto.ProductListImportDTO;
import com.nasnav.exceptions.BusinessException;

import java.util.List;

public interface DataImportService {
    List<ProductImportData> toProductImportDto (List<ProductImportDTO> rows, ProductListImportDTO importMetaData) throws BusinessException;

    void saveToDB(List<ProductImportData> importedDtos, ProductListImportDTO importMetaData) throws BusinessException;
}
