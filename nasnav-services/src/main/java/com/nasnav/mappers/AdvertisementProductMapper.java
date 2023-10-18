package com.nasnav.mappers;

import com.nasnav.dto.response.navbox.AdvertisementProductDTO;
import com.nasnav.persistence.AdvertisementProductEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AdvertisementProductMapper {
    @Mapping(source = "product.id", target = "productId")
    AdvertisementProductDTO toDto(AdvertisementProductEntity entity);

    @Mapping(source = "productId", target = "product.id")
    AdvertisementProductEntity toEntity(AdvertisementProductDTO dto);
}
