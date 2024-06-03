package com.nasnav.commons.model.handler;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;


//TODO Check Duplication DataImportServiceImpl

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductDataLists {
    private List<ProductData> allProductsData = new ArrayList<>();
    private List<ProductData> newProductsData = new ArrayList<>();
    private List<ProductData> existingProductsData = new ArrayList<>();

    public void addAllProductsData(List<ProductData> products) {
        allProductsData.addAll(products);
    }

    public void addNewProductsData(List<ProductData> products) {
        newProductsData.addAll(products);
    }

    public void addExistingProductsData(List<ProductData> products) {
        existingProductsData.addAll(products);
    }

}