package com.nasnav.commons.model.handler;


import com.nasnav.commons.model.dataimport.ProductImportDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.lang.reflect.InvocationTargetException;

import static org.apache.commons.beanutils.BeanUtils.copyProperties;


//TODO Check Duplication DataImportServiceImpl

@Data
@EqualsAndHashCode(callSuper = true)
public class IndexedProductImportDTO extends ProductImportDTO {
    private Integer index;

    public IndexedProductImportDTO(Integer index, ProductImportDTO productImportDTO) {
        try {
            copyProperties(this, productImportDTO);
            this.index = index;
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
