package com.nasnav.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nasnav.dto.response.ThreeDModelList;
import com.nasnav.dto.response.ThreeDModelResponse;
import com.nasnav.request.ThreeDModelSearchParam;
import org.springframework.web.multipart.MultipartFile;


public interface ThreeDModelService {
    ThreeDModelResponse createNewThreeModel(String jsonString, MultipartFile[] files) throws JsonProcessingException;

    ThreeDModelResponse getThreeDModelByBarcodeOrSKU(String barcode, String sku);

    ThreeDModelList getThreeDModelAll(ThreeDModelSearchParam searchParam);

    ThreeDModelResponse getThreeDModel(Long modelId);

    void assignModelToProduct(Long modelId, Long productId);

    ThreeDModelResponse updateThreeDModel(Long modelId, String jsonString, MultipartFile[] files) throws JsonProcessingException;

    void deleteThreeDModel(Long modelId);
}
