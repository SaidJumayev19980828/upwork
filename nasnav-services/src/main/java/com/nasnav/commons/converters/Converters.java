package com.nasnav.commons.converters;

import com.nasnav.commons.converters.impl.ProductImageDTOToCsvRowMapper;
import com.nasnav.commons.converters.impl.VariantNoImagesDTOToCsvRowMapper;
import com.nasnav.dto.ProductImageDTO;
import com.nasnav.dto.VariantWithNoImagesDTO;

public class Converters {

    public static DtoToCsvRowMapper getDtoToCsvRowConverterForBean(Object bean){
        Class<?> beanClass = bean.getClass();

        if(beanClass.equals(ProductImageDTO.class)){
            return new ProductImageDTOToCsvRowMapper();
        }else if (beanClass.equals(VariantWithNoImagesDTO.class)){
            return new VariantNoImagesDTOToCsvRowMapper();
        }else
            return null;
    }
}