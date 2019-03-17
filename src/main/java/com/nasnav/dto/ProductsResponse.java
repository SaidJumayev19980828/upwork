package com.nasnav.dto;

import lombok.Data;

import java.util.List;

@Data
public class ProductsResponse {

    private Long total;
    private List<ProductRepresentationObject> products;

}
