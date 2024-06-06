package com.nasnav.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nasnav.persistence.ProductThreeDModel;
import lombok.Data;


import java.util.List;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ThreeDModelResponse {
    private Long modelId;
    private String name;
    private String description;
    private String barcode;
    private String sku;
    private String color;
    private String model;
    private String imageUrl;
    private Long size;
    private List<String> urls;

    public static ThreeDModelResponse get3dModelResponse(ProductThreeDModel threeDModel, List<String> filesUrls) {
        ThreeDModelResponse threeDModelResponse = new ThreeDModelResponse();
        threeDModelResponse.setModelId(threeDModel.getId());
        threeDModelResponse.setName(threeDModel.getName());
        threeDModelResponse.setDescription(threeDModel.getDescription());
        threeDModelResponse.setModel(threeDModel.getModel());
        threeDModelResponse.setSize(threeDModel.getSize());
        threeDModelResponse.setColor(threeDModel.getColor());
        threeDModelResponse.setBarcode(threeDModel.getBarcode());
        threeDModelResponse.setSku(threeDModel.getSku());
        threeDModelResponse.setUrls(filesUrls);
        threeDModelResponse.setImageUrl(threeDModel.getImageUrl());
        return threeDModelResponse;
    }
}
