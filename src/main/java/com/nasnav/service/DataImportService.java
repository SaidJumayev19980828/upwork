package com.nasnav.service;

import java.util.List;

import com.nasnav.commons.model.dataimport.ProductImportDTO;
import com.nasnav.dto.ProductImportMetadata;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.exceptions.ImportProductException;
import com.nasnav.service.model.importproduct.context.ImportProductContext;

public interface DataImportService {

	ImportProductContext importProducts(List<ProductImportDTO> productImportDTO, ProductImportMetadata productImportMetadata) throws BusinessException, ImportProductException;
}
