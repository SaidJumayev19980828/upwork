package com.nasnav.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nasnav.dto.response.ThreeDModelResponse;
import org.springframework.web.multipart.MultipartFile;


public interface ThreeDModelService {
   ThreeDModelResponse createNewThreeModel(String jsonString, MultipartFile[] files) throws JsonProcessingException;
   ThreeDModelResponse getThreeDModelByBarcodeOrSKU(String barcode ,String sku);
   ThreeDModelResponse getThreeDModel(Long modelId);
   void assignModelToProduct(Long modelId, Long productId);
}
