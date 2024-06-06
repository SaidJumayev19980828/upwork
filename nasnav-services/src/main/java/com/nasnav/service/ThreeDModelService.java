package com.nasnav.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nasnav.dto.response.ThreeDModelResponse;
import org.springframework.data.domain.PageImpl;
import org.springframework.web.multipart.MultipartFile;


public interface ThreeDModelService {
    ThreeDModelResponse createNewThreeModel(String jsonString, MultipartFile[] files) throws JsonProcessingException;

    ThreeDModelResponse getThreeDModelByBarcodeOrSKU(String barcode, String sku);

    PageImpl<ThreeDModelResponse> getThreeDModelAll(Integer start, Integer count);

    ThreeDModelResponse getThreeDModel(Long modelId);

    void assignModelToProduct(Long modelId, Long productId);

    ThreeDModelResponse updateThreeDModel(Long modelId, String jsonString, MultipartFile[] files) throws JsonProcessingException;

    void deleteThreeDModel(Long modelId);
}
