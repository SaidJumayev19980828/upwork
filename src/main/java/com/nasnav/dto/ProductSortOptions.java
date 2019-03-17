package com.nasnav.dto;

import lombok.Getter;

public enum ProductSortOptions {

    ID("id"),NAME("name"),P_NAME("pname"),PRICE("price");

    @Getter
    private String value;

    ProductSortOptions(String value){
        this.value = value;
    }

    public static ProductSortOptions getProductSortOptions(String value){

        if(value==null || value.trim().isEmpty())
            return null;

        for (ProductSortOptions productSortOptions : ProductSortOptions.values()){
            if(productSortOptions.getValue().equals(value))
                return productSortOptions;
        }
        return null;
    }
}
