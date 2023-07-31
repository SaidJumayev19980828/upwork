package com.nasnav.service.impl;

import com.nasnav.dao.AdvertisementRepository;
import com.nasnav.dao.ProductRepository;
import com.nasnav.dto.ProductBaseInfo;
import com.nasnav.dto.response.AdvertisementDTO;
import com.nasnav.persistence.AdvertisementEntity;
import com.nasnav.persistence.ProductEntity;
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

    @Transactional
    @Override
    public PageImpl<AdvertisementDTO> findAllAdvertisements(Integer start, Integer count) {
        PageRequest page = getQueryPage(start, count);
        Page<AdvertisementEntity> all = advertisementRepository.findAll(page);
        List<AdvertisementDTO> dtos = all.getContent().stream().map(this::toDto).collect(Collectors.toList());
        return new PageImpl<>(dtos, all.getPageable(), all.getTotalElements());
    }

    @Override
    public AdvertisementDTO create(AdvertisementDTO advertisementDTO) {
        AdvertisementEntity entity = toEntity(advertisementDTO);
        AdvertisementEntity savedEntity = advertisementRepository.save(entity);
        return toDto(savedEntity);
    }

    @Override
    public AdvertisementDTO findOneByPostId(Long postId) {
        AdvertisementEntity entity = advertisementRepository.findAdvertisementEntitiesByPostId(postId);
        return toDto(entity);
    }

    private AdvertisementEntity toEntity(AdvertisementDTO advertisementDTO) {
        AdvertisementEntity entity = new AdvertisementEntity();

        entity.setCoins(advertisementDTO.getCoins());
        entity.setLikes(advertisementDTO.getLikes());
        entity.setFromDate(advertisementDTO.getFromDate());
        entity.setToDate(advertisementDTO.getToDate());

        ProductEntity byId = productRepository.getById(advertisementDTO.getProduct().getId());
        entity.setProduct(byId);

        return entity;
    }

    private AdvertisementDTO toDto(AdvertisementEntity entity) {
        AdvertisementDTO dto = new AdvertisementDTO();

        dto.setId(entity.getId());
        dto.setLikes(entity.getLikes());
        dto.setCoins(entity.getCoins());
        dto.setFromDate(entity.getFromDate());
        dto.setToDate(entity.getToDate());

        dto.setProduct(toDto(entity.getProduct()));

        return dto;
    }

    private ProductBaseInfo toDto(ProductEntity entity) {
        ProductBaseInfo dto = new ProductBaseInfo();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setImageUrl(imageService.getProductCoverImage(entity.getId()));
        return dto;
    }

}
