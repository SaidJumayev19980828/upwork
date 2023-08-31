package com.nasnav.service;

import com.nasnav.dto.response.AdvertisementDTO;
import org.springframework.data.domain.PageImpl;

import java.util.Optional;

public interface AdvertisementService {
    PageImpl<AdvertisementDTO> findAllAdvertisements(String orgId, Integer start, Integer count);

    AdvertisementDTO create(AdvertisementDTO advertisementDTO);

    Optional<AdvertisementDTO> findAdvertisementById(Long id);

    void deleteAdvertisementById(Long id);
}
