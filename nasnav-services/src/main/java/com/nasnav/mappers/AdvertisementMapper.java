package com.nasnav.mappers;

import com.nasnav.dto.response.AdvertisementDTO;
import com.nasnav.persistence.AdvertisementEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AdvertisementMapper {
    @Mapping(source = "organization.id", target = "orgId")
    AdvertisementDTO toDto(AdvertisementEntity entity);

    @Mapping(source = "orgId", target = "organization.id")
    AdvertisementEntity toEntity(AdvertisementDTO dto);
}
