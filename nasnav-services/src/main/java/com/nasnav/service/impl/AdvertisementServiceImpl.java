package com.nasnav.service.impl;

import com.nasnav.dao.AdvertisementRepository;
import com.nasnav.dao.ProductRepository;
import com.nasnav.dto.response.AdvertisementDTO;
import com.nasnav.mappers.AdvertisementMapper;
import com.nasnav.persistence.AdvertisementEntity;
import com.nasnav.service.AdvertisementService;
import com.nasnav.service.ProductImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static com.nasnav.commons.utils.PagingUtils.getQueryPage;

@Service
@RequiredArgsConstructor
public class AdvertisementServiceImpl implements AdvertisementService {
    private final AdvertisementRepository advertisementRepository;
    private final ProductImageService imageService;
    private final ProductRepository productRepository;
    private final AdvertisementMapper advertisementMapper;

    @Transactional
    @Override
    public PageImpl<AdvertisementDTO> findAllAdvertisements(Integer start, Integer count) {
        PageRequest page = getQueryPage(start, count);
        Page<AdvertisementEntity> all = advertisementRepository.findAll(page);
        List<AdvertisementDTO> dtos = all.getContent().stream().map(advertisementMapper::toDto).collect(Collectors.toList());
        return new PageImpl<>(dtos, all.getPageable(), all.getTotalElements());
    }

    @Override
    public AdvertisementDTO create(AdvertisementDTO advertisementDTO) {
        AdvertisementEntity entity = advertisementMapper.toEntity(advertisementDTO);
        if (advertisementDTO.getProduct() != null) {
            entity.setProduct(productRepository.getById(advertisementDTO.getProduct().getId()));
        }
        AdvertisementEntity savedEntity = advertisementRepository.save(entity);
        AdvertisementDTO dto = advertisementMapper.toDto(savedEntity);
        if (dto.getProduct() != null) {
            dto.getProduct().setImageUrl(imageService.getProductCoverImage(dto.getProduct().getId()));
        }
        return dto;
    }

    @Override
    public AdvertisementDTO findOneByPostId(Long postId) {
        AdvertisementEntity entity = advertisementRepository.findAdvertisementEntitiesByPostId(postId);
        AdvertisementDTO dto = advertisementMapper.toDto(entity);
        if (dto != null) dto.getProduct().setImageUrl(imageService.getProductCoverImage(entity.getProduct().getId()));
        return dto;
    }
}
