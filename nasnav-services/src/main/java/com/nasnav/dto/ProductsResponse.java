package com.nasnav.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Data
public class ProductsResponse {

    private Long total;
    private List<ProductRepresentationObject> products;
    
    
    public ProductsResponse() {
    	total = 0L;
    	products = new ArrayList<>();
    }

}
