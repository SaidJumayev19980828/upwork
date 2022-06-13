package com.nasnav.commons.model.handler;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;


//TODO Check Duplication DataImportServiceImpl

@Data
@AllArgsConstructor
public class ProductDataLists {

    private List<ProductData> allProductsData;

    private List<ProductData> newProductsData;

    private List<ProductData> existingProductsData;

}