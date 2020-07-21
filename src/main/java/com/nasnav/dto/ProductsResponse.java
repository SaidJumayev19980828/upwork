package com.nasnav.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

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
