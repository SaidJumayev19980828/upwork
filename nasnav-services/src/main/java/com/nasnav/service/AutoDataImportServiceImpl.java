package com.nasnav.service;

import java.util.List;
import java.util.NoSuchElementException;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.google.common.collect.MoreCollectors;
import com.nasnav.dto.ProductListImportDTO;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.exceptions.ErrorCodes;
import com.nasnav.exceptions.ImportProductException;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.service.model.importproduct.context.ImportProductContext;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AutoDataImportServiceImpl implements AutoDataImportService {
  private final List<CsvExcelDataImportService> dataImportServices;

  @Override
  public ImportProductContext importProductList(@Valid MultipartFile file, @Valid ProductListImportDTO importMetaData)
      throws BusinessException, ImportProductException {
    CsvExcelDataImportService supportingService = findUniqueSupportingService(file);
    return supportingService.importProductList(file, importMetaData);
  }

  
  @Override
  public CsvExcelDataImportService findUniqueSupportingService(@Valid MultipartFile file) throws RuntimeBusinessException {
    try {
      return dataImportServices.stream()
          .filter(dataImportService -> dataImportService.isFileSupported(file))
          .collect(MoreCollectors.onlyElement());
    } catch (NoSuchElementException e) {
      throw new  RuntimeBusinessException(HttpStatus.NOT_ACCEPTABLE, ErrorCodes.P$IMPORT$0003);
    } catch (IllegalArgumentException e) {
      throw new  RuntimeBusinessException(HttpStatus.NOT_ACCEPTABLE, ErrorCodes.P$IMPORT$0004);
    }
  }
}
