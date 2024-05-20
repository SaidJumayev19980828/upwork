package com.nasnav.service;

import com.nasnav.dto.response.AdvertisementDTO;
import com.nasnav.persistence.AdvertisementProductEntity;
import org.springframework.data.domain.PageImpl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface AdvertisementService {
    PageImpl<AdvertisementDTO> findAllAdvertisements(Long orgId, Integer start, Integer count, LocalDateTime fromDate,
            LocalDateTime toDate, String name);

    AdvertisementDTO create(AdvertisementDTO advertisementDTO);

    Optional<AdvertisementDTO> findAdvertisementById(Long id);

    void deleteAdvertisementById(Long id);

    List<AdvertisementProductEntity> findAdvertisementProducts(Long advertisementId, Set<Long> productsInPost);

    void update(AdvertisementDTO advertisementDTO);
}
