package com.nasnav.service;

import java.util.NoSuchElementException;

import javax.validation.Valid;

import org.springframework.web.multipart.MultipartFile;

import com.nasnav.dto.ProductListImportDTO;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.exceptions.ImportProductException;
import com.nasnav.service.model.importproduct.context.ImportProductContext;

public interface AutoDataImportService {
  ImportProductContext importProductList(@Valid MultipartFile file, @Valid ProductListImportDTO importMetaData) throws BusinessException, ImportProductException;

  CsvExcelDataImportService findUniqueSupportingService(@Valid MultipartFile file) throws IllegalArgumentException, NoSuchElementException;
}
