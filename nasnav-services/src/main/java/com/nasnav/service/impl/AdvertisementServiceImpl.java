package com.nasnav.service.impl;

import com.nasnav.dao.AdvertisementRepository;
import com.nasnav.dao.OrganizationRepository;
import com.nasnav.dao.ProductRepository;
import com.nasnav.dto.response.AdvertisementDTO;
import com.nasnav.dto.response.navbox.AdvertisementProductDTO;
import com.nasnav.mappers.AdvertisementMapper;
import com.nasnav.mappers.AdvertisementProductCollectionMapper;
import com.nasnav.mappers.BrandsMapper;
import com.nasnav.persistence.AdvertisementEntity;
import com.nasnav.persistence.AdvertisementProductEntity;
import com.nasnav.persistence.ProductEntity;
import com.nasnav.service.AdvertisementProductService;
import com.nasnav.service.AdvertisementService;
import com.nasnav.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.nasnav.commons.utils.PagingUtils.getQueryPage;

@Service
@RequiredArgsConstructor
public class AdvertisementServiceImpl implements AdvertisementService {
    private final AdvertisementRepository advertisementRepository;
    private final AdvertisementProductService advertisementProductService;
    private final AdvertisementMapper advertisementMapper;
    private final AdvertisementProductCollectionMapper advertisementProductCollectionMapper;
    private final OrganizationRepository organizationRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;
    private final BrandsMapper brandsMapper;

    @Transactional
    @Override
    public PageImpl<AdvertisementDTO> findAllAdvertisements(String orgId, Integer start, Integer count) {
        PageRequest page = getQueryPage(start, count);
        Page<AdvertisementEntity> all = advertisementRepository.findAll((root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            try {
                if (orgId != null) {
                    predicates.add(criteriaBuilder.equal(root.get("organization").get("id"), Long.parseLong(orgId)));
                }
            } catch (Exception e) {
            }
            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        }, page);
        List<AdvertisementDTO> dtos = all.getContent().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return new PageImpl<>(dtos, all.getPageable(), all.getTotalElements());
    }

    @Transactional
    @Override
    public AdvertisementDTO create(AdvertisementDTO advertisementDTO) {
        AdvertisementEntity advertisement = advertisementMapper.toEntity(advertisementDTO);
        advertisement.setName(advertisementDTO.getName());
        if (advertisementDTO.getOrgId() != null) {
            advertisement.setOrganization(organizationRepository.findOneById(advertisementDTO.getOrgId()));
        }
        Assert.notNull(advertisement.getOrganization(), "organization cant be null");
        Assert.notNull(advertisement.getOrganization().getBankAccount(), "organization should have a bank account");
        AdvertisementEntity advertisementEntity = advertisementRepository.save(advertisement);
        List<AdvertisementProductDTO> advertisementProductDTOS = advertisementProductService.save(advertisementEntity, advertisementDTO.getProducts());
        AdvertisementDTO dto = advertisementMapper.toDto(advertisementEntity);
        dto.setProducts(advertisementProductDTOS);
        dto.setName(advertisementEntity.getName());
        return dto;
    }

    @Override
    public Optional<AdvertisementDTO> findAdvertisementById(Long id) {
        return advertisementRepository.findById(id).map(this::toDto);
    }

    @Transactional
    @Override
    public void deleteAdvertisementById(Long id) {
        advertisementRepository.deleteById(id);
    }

    @Override
    public List<AdvertisementProductEntity> findAdvertisementProducts(Long advertisementId, Set<Long> productsInPost) {
        return advertisementRepository.findAdvertisementProducts(advertisementId, productsInPost);
    }

    @Transactional
    @Override
    public void update(AdvertisementDTO advertisementDTO) {
        advertisementRepository.findById(advertisementDTO.getId())
                .ifPresent(e -> {
                    AdvertisementEntity advertisement = advertisementMapper.toEntity(advertisementDTO);
                    advertisement.setName(advertisementDTO.getName());
                    if (advertisementDTO.getOrgId() != null) {
                        advertisement.setOrganization(organizationRepository.findOneById(advertisementDTO.getOrgId()));
                    }
                    Assert.notNull(advertisement.getOrganization(), "organization cant be null");
                    Assert.notNull(advertisement.getOrganization().getBankAccount(), "organization should have a bank account");
                    advertisementProductService.deleteAll(advertisement.getId());
                    advertisement.setCreationDate(e.getCreationDate());
                    AdvertisementEntity advertisementEntity = advertisementRepository.save(advertisement);
                    advertisementProductService.save(advertisementEntity, advertisementDTO.getProducts());
        });
    }

    private AdvertisementDTO toDto(AdvertisementEntity entity) {
        AdvertisementDTO dto = advertisementMapper.toDto(entity);
        dto.setName(entity.getName());
        dto.setProducts(advertisementProductCollectionMapper.toDto(entity.getAdvertisementProducts()).stream().map(itx -> {
            if (itx.getProductId() != null) {
                ProductEntity product = productRepository.getById(itx.getProductId());
                itx.setProductDetailsDTO(productService.toProductDetailsDTO(product, false));
                itx.setBrandsDTO(brandsMapper.toBrandsDTO(product.getBrand()));
            }
            return itx;
        }).collect(Collectors.toList()));
        return dto;
    }
}
