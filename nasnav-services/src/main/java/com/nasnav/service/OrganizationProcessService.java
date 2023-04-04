package com.nasnav.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.nasnav.dto.ProductListImportDTO;
import com.nasnav.response.ImportProcessStatusResponse;

public interface OrganizationProcessService {

  List<ImportProcessStatusResponse> getProcessesStatus();

  ImportProcessStatusResponse getProcessStatus(String processId);

  Object getProcessResult(String processId);

  ImportProcessStatusResponse cancelProcess(String processId);

  void clearProcess(String processId);

  void clearAllProcess();

  ImportProcessStatusResponse importExcelProductList(MultipartFile file, ProductListImportDTO importMetaData)
      throws Exception;

  ImportProcessStatusResponse importCsvProductList(MultipartFile file, ProductListImportDTO importMetaData)
      throws Exception;

}