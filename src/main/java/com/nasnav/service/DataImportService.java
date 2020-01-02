package com.nasnav.service;

import java.util.List;

import com.nasnav.commons.model.dataimport.ProductImportDTO;
import com.nasnav.dto.ProductImportMetadata;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.response.ProductListImportResponse;

public interface DataImportService {

    ProductListImportResponse importProducts(List<ProductImportDTO> productImportDTO, ProductImportMetadata productImportMetadata) throws BusinessException;
}
