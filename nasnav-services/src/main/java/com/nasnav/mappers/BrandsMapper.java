package com.nasnav.mappers;

import com.nasnav.dto.response.BrandsDTO;
import com.nasnav.persistence.BrandsEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BrandsMapper {

    BrandsDTO toBrandsDTO(BrandsEntity entity);
}