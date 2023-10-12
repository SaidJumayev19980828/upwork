package com.nasnav.service.impl;

import com.nasnav.dao.AdvertisementProductRepository;
import com.nasnav.dao.ProductRepository;
import com.nasnav.dto.response.navbox.AdvertisementProductDTO;
import com.nasnav.mappers.AdvertisementProductCollectionMapper;
import com.nasnav.mappers.BrandsMapper;
import com.nasnav.persistence.AdvertisementEntity;
import com.nasnav.persistence.AdvertisementProductEntity;
import com.nasnav.persistence.ProductEntity;
import com.nasnav.service.AdvertisementProductService;
import com.nasnav.service.ProductService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class AdvertisementProductServiceImpl implements AdvertisementProductService {
    private final AdvertisementProductRepository advertisementProductRepository;
    private final AdvertisementProductCollectionMapper advertisementProductCollectionMapper;
    private final BrandsMapper brandsMapper;
    private final ProductService productService;
    private final ProductRepository productRepository;

    @Transactional
    public List<AdvertisementProductDTO> save(AdvertisementEntity advertisement, List<AdvertisementProductDTO> advertisementProductDTOS) {
        if (advertisementProductDTOS != null && !advertisementProductDTOS.isEmpty()) {
            List<AdvertisementProductEntity> advertisementProducts = advertisementProductCollectionMapper.toEntity(advertisementProductDTOS)
                    .stream()
                    .map(it -> {
                        it.setAdvertisement(advertisement);
                        it.setProduct(productRepository.getById(it.getProduct().getId()));
                        return it;
                    }).collect(Collectors.toList());
            return advertisementProductCollectionMapper.toDto(advertisementProductRepository.saveAll(advertisementProducts))
                    .stream()
                    .map(it -> {
                        if (it.getProductId() != null) {
                            ProductEntity product = productRepository.getById(it.getProductId());
                            it.setProductDetailsDTO(productService.toProductDetailsDTO(product, true));
                            it.setBrandsDTO(brandsMapper.toBrandsDTO(product.getBrand()));
                        }
                        return it;
                    })
                    .collect(Collectors.toList());
        } else {
            return List.of();
        }
    }

    @Transactional
    @Override
    public void deleteAll(Long advertisementId) {
        advertisementProductRepository.deleteAllByAdvertisement_Id(advertisementId);
    }

}
