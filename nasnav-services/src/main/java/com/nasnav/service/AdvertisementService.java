package com.nasnav.service;

import com.nasnav.dto.response.AdvertisementDTO;
import org.springframework.data.domain.PageImpl;

public interface AdvertisementService {
    PageImpl<AdvertisementDTO> findAllAdvertisements(Integer start, Integer count);

    AdvertisementDTO create(AdvertisementDTO advertisementDTO);

    AdvertisementDTO findOneByPostId(Long postId);
}
