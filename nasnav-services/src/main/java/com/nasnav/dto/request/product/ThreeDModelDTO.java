package com.nasnav.dto.request.product;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;

import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nasnav.persistence.ProductThreeDModel;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ThreeDModelDTO {
    @JsonIgnore
    private Long id;
    private String name;
    private String description;
    private String barcode;
    private String sku;
    private String color;
    private String model;
    private String image;
    private Long size;

    public void toEntity(ProductThreeDModel productThreeDModel) {
        if (this.id != null) {
            productThreeDModel.setId(this.id);
        }
        productThreeDModel.setName(this.name);
        productThreeDModel.setDescription(this.description);
        productThreeDModel.setBarcode(this.barcode);
        productThreeDModel.setSku(this.sku);
        productThreeDModel.setColor(this.color);
        productThreeDModel.setModel(this.model);
        productThreeDModel.setImageUrl(this.image);
        productThreeDModel.setSize(this.size);
    }
}
