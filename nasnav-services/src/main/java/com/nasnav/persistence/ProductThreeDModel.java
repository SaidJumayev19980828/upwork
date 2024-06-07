package com.nasnav.persistence;

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
    private String size;
    private String imageUrl;
}
