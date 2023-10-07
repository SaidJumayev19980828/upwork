package com.nasnav.mappers;

import com.nasnav.dto.response.navbox.AdvertisementProductDTO;
import com.nasnav.persistence.AdvertisementProductEntity;
import org.mapstruct.Mapper;

import java.util.List;


@Mapper(componentModel = "spring", uses = {AdvertisementProductMapper.class})
public interface AdvertisementProductCollectionMapper {
    List<AdvertisementProductDTO> toDto(List<AdvertisementProductEntity> entities);

    List<AdvertisementProductEntity> toEntity(List<AdvertisementProductDTO> dtos);

}