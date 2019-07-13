package com.nasnav.dto;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper=false)
public class ProductRepresentationObject extends BaseRepresentationObject{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @Column(name = "image_url")
    private String imageUrl;
    @Column(name = "p_name")
    private String  pname;
    private BigDecimal price;
    private Boolean available;
    @Column(name = "category_id")
    private Long categoryId;
    @Column(name = "brand_id")
    private Long brandId;
}
