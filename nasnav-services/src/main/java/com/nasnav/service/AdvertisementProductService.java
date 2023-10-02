package com.nasnav.service;

import com.nasnav.dto.response.navbox.AdvertisementProductDTO;
import com.nasnav.persistence.AdvertisementEntity;

import java.util.List;

public interface AdvertisementProductService {
    List<AdvertisementProductDTO> save(AdvertisementEntity advertisement, List<AdvertisementProductDTO> advertisementProductDTOS);
}
