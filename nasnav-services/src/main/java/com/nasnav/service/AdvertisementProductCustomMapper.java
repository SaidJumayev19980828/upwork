package com.nasnav.service;

import com.nasnav.dto.response.navbox.AdvertisementProductDTO;
import com.nasnav.persistence.AdvertisementProductEntity;

import java.util.List;

public interface AdvertisementProductCustomMapper {
    List<AdvertisementProductDTO> toDto(List<AdvertisementProductEntity> entities);
    AdvertisementProductDTO toDto(AdvertisementProductEntity entity);
}
