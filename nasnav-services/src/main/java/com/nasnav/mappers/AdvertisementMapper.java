package com.nasnav.mappers;

import com.nasnav.dto.response.AdvertisementDTO;
import com.nasnav.persistence.AdvertisementEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AdvertisementMapper {
    AdvertisementDTO toDto(AdvertisementEntity entity);

    AdvertisementEntity toEntity(AdvertisementDTO dto);
}
