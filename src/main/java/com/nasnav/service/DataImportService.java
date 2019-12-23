package com.nasnav.service;

import com.nasnav.commons.model.dataimport.ProductImportDTO;
import com.nasnav.dto.ProductImportMetadata;
import com.nasnav.dto.ProductListImportDTO;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.response.ProductListImportResponse;

import java.util.List;

public interface DataImportService {

    ProductListImportResponse importProducts(List<ProductImportDTO> productImportDTO, ProductImportMetadata productImportMetadata) throws BusinessException;
}
