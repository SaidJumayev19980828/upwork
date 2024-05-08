package com.nasnav.persistence;

import com.nasnav.dto.request.product.ThreeDModelDTO;
import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "product_3d_model")
@Data
public class ProductThreeDModel {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String name;
    private String description;
    private String barcode;
    private String sku;
    private String color;
    private String model;
    private Long size;
    private String imageUrl;
    public static ProductThreeDModel getProductThreeDModel(ThreeDModelDTO threeDModelDTO) {
        ProductThreeDModel productThreeDModel =new ProductThreeDModel();
        productThreeDModel.setName(threeDModelDTO.getName());
        productThreeDModel.setDescription(threeDModelDTO.getDescription());
        productThreeDModel.setBarcode(threeDModelDTO.getBarcode());
        productThreeDModel.setSku(threeDModelDTO.getSku());
        productThreeDModel.setColor(threeDModelDTO.getColor());
        productThreeDModel.setModel(threeDModelDTO.getModel());
        productThreeDModel.setSize(threeDModelDTO.getSize());
        productThreeDModel.setImageUrl(threeDModelDTO.getImage());
        return productThreeDModel;
    }

}
