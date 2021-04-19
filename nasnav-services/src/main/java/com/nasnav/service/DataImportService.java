package com.nasnav.service;

import com.nasnav.commons.model.dataimport.ProductImportDTO;
import com.nasnav.dto.ProductImportMetadata;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.exceptions.ImportProductException;
import com.nasnav.service.model.importproduct.context.ImportProductContext;

import java.util.List;

public interface DataImportService {

	ImportProductContext importProducts(List<ProductImportDTO> productImportDTO, ProductImportMetadata productImportMetadata) throws BusinessException, ImportProductException;
}
