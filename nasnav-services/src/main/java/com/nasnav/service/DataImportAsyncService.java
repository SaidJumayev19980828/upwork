package com.nasnav.service;

import javax.validation.Valid;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.nasnav.dto.ProductListImportDTO;
import com.nasnav.response.ImportProcessStatusResponse;

public interface DataImportAsyncService {

  ImportProcessStatusResponse importExcelProductList(MultipartFile file, ProductListImportDTO importMetaData,
      Long orgId, Long userId) throws Exception;

  ImportProcessStatusResponse importCsvProductList(MultipartFile file, ProductListImportDTO importMetaData, Long orgId,
      Long userId) throws Exception;

}