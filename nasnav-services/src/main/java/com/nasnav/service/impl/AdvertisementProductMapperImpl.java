package com.nasnav.service.impl;

import com.nasnav.dao.ProductRepository;
import com.nasnav.dto.response.RuleResponseDTO;
import com.nasnav.dto.response.navbox.AdvertisementProductDTO;
import com.nasnav.mappers.BrandsMapper;
import com.nasnav.persistence.AdvertisementProductCompensation;
import com.nasnav.persistence.AdvertisementProductEntity;
import com.nasnav.persistence.CompensationRulesEntity;
import com.nasnav.persistence.ProductEntity;
import com.nasnav.service.AdvertisementProductCustomMapper;
import com.nasnav.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AdvertisementProductMapperImpl implements AdvertisementProductCustomMapper {
    private final BrandsMapper brandsMapper;
    private final ProductService productService;
    private final ProductRepository productRepository;
    @Override
    public List<AdvertisementProductDTO> toDto(List<AdvertisementProductEntity> entities) {
        return entities.stream().map(this::toDto).toList();
    }
    @Override
    public AdvertisementProductDTO toDto(AdvertisementProductEntity entity) {
        AdvertisementProductDTO advertisementProductDTO = new AdvertisementProductDTO();
        advertisementProductDTO.setProductId( entityProductId( entity ) );
        advertisementProductDTO.setId( entity.getId() );
        advertisementProductDTO.setCoins( entity.getCoins() );
        advertisementProductDTO.setLikes( entity.getLikes() );
        advertisementProductDTO.setRules(compensationRules(entity.getCompensationRules()));
        return productDetails(advertisementProductDTO);
    }

    private RuleResponseDTO ruleResponse (CompensationRulesEntity compensationRule){
       return new RuleResponseDTO(compensationRule.getId() , compensationRule.getName() ,
                compensationRule.getDescription()
                );
    }
    private Set<RuleResponseDTO> compensationRules(Set<AdvertisementProductCompensation> compensationRules){
      return  compensationRules.stream().
              map(advertisementProductCompensation ->
                      ruleResponse(advertisementProductCompensation.getCompensationRule()))
              .collect(Collectors.toSet());
    }

    private AdvertisementProductDTO productDetails(AdvertisementProductDTO dto){
        if (dto.getProductId() != null) {
            ProductEntity product = productRepository.getById(dto.getProductId());
            dto.setProductDetailsDTO(productService.toProductDetailsDTO(product, true));
            dto.setBrandsDTO(brandsMapper.toBrandsDTO(product.getBrand()));
        }
        return dto;
    }
    private Long entityProductId(AdvertisementProductEntity advertisementProductEntity) {
        if ( advertisementProductEntity == null ) {
            return null;
        }
        ProductEntity product = advertisementProductEntity.getProduct();
        if ( product == null ) {
            return null;
        }
        return product.getId();
    }
}
