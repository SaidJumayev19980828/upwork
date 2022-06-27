package com.nasnav.commons.model.handler;


import com.nasnav.service.model.importproduct.context.ImportProductContext;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Optional;

@Data
@AllArgsConstructor
public class ProductDataImportContext{
    private ProductDataLists productsData;
    private ImportProductContext context;

    public static Optional<ProductDataImportContext> of(ProductDataLists productData
            , ImportProductContext context){
        return Optional.of(new ProductDataImportContext(productData, context));
    }
}