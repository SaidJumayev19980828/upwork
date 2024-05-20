package com.nasnav.service.impl;

import com.nasnav.dao.AdvertisementRepository;
import com.nasnav.dao.OrganizationRepository;
import com.nasnav.dto.response.AdvertisementDTO;
import com.nasnav.dto.response.navbox.AdvertisementProductDTO;
import com.nasnav.persistence.AdvertisementEntity;
import com.nasnav.persistence.AdvertisementProductEntity;
import com.nasnav.persistence.OrganizationEntity;
import com.nasnav.service.AdvertisementProductCustomMapper;
import com.nasnav.service.AdvertisementProductService;
import com.nasnav.service.AdvertisementService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.nasnav.commons.utils.PagingUtils.getQueryPage;

@Service
@RequiredArgsConstructor
public class AdvertisementServiceImpl implements AdvertisementService {
    private final AdvertisementRepository advertisementRepository;
    private final AdvertisementProductService advertisementProductService;
    private final OrganizationRepository organizationRepository;
    private final AdvertisementProductCustomMapper advertisementProductCustomMapper;

    @Transactional
    @Override
    public PageImpl<AdvertisementDTO> findAllAdvertisements(Long orgId, Integer start, Integer count, LocalDateTime fromDate, LocalDateTime toDate,
            String name) {
        PageRequest page = getQueryPage(start, count);
        PageImpl<AdvertisementEntity> all = advertisementRepository.getAllByDateBetweenAndStatusEqualsAndNameIfNotNull(orgId, fromDate, toDate, name,
                page);
        return new PageImpl<>(all.getContent().stream().map(this::toDto).toList(), all.getPageable(), all.getTotalElements());
    }

    @Transactional
    @Override
    public AdvertisementDTO create(AdvertisementDTO advertisementDTO) {
        AdvertisementEntity advertisement = toEntity(advertisementDTO);
        advertisement.setName(advertisementDTO.getName());
        if (advertisementDTO.getOrgId() != null) {
            advertisement.setOrganization(organizationRepository.findOneById(advertisementDTO.getOrgId()));
        }
        Assert.notNull(advertisement.getOrganization(), "organization cant be null");
        Assert.notNull(advertisement.getOrganization().getBankAccount(), "organization should have a bank account");
        AdvertisementEntity advertisementEntity = advertisementRepository.save(advertisement);
        List<AdvertisementProductDTO> advertisementProductDTOS = advertisementProductService.save(advertisementEntity, advertisementDTO.getProducts());
        AdvertisementDTO dto = fromEntityToDto(advertisementEntity);
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
                    AdvertisementEntity advertisement = toEntity(advertisementDTO);
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

    private AdvertisementDTO fromEntityToDto(AdvertisementEntity entity) {
        if ( entity == null ) {
            return null;
        }
        AdvertisementDTO advertisementDTO = new AdvertisementDTO();
        advertisementDTO.setOrgId( entityOrganizationId( entity ) );
        advertisementDTO.setId( entity.getId() );
        advertisementDTO.setName( entity.getName() );
        advertisementDTO.setBannerUrl( entity.getBannerUrl() );
        advertisementDTO.setCreationDate( entity.getCreationDate() );
        advertisementDTO.setFromDate( entity.getFromDate() );
        advertisementDTO.setToDate( entity.getToDate() );

        return advertisementDTO;
    }
    private Long entityOrganizationId(AdvertisementEntity advertisementEntity) {
        if ( advertisementEntity == null ) {
            return null;
        }
        OrganizationEntity organization = advertisementEntity.getOrganization();
        if ( organization == null ) {
            return null;
        }
        return organization.getId();
    }

    private AdvertisementDTO toDto(AdvertisementEntity entity) {
        AdvertisementDTO dto = fromEntityToDto(entity);
        dto.setName(entity.getName());
        dto.setProducts(advertisementProductCustomMapper.toDto(entity.getAdvertisementProducts()));
        return dto;
    }

    private AdvertisementEntity toEntity(AdvertisementDTO dto) {
        AdvertisementEntity advertisementEntity = new AdvertisementEntity();
        advertisementEntity.setOrganization( advertisementDTOToOrganizationEntity( dto ) );
        advertisementEntity.setId( dto.getId() );
        advertisementEntity.setName( dto.getName() );
        advertisementEntity.setBannerUrl( dto.getBannerUrl() );
        advertisementEntity.setFromDate( dto.getFromDate() );
        advertisementEntity.setToDate( dto.getToDate() );
        advertisementEntity.setCreationDate( dto.getCreationDate() );

        return advertisementEntity;
    }

    private OrganizationEntity advertisementDTOToOrganizationEntity(AdvertisementDTO advertisementDTO) {
        if ( advertisementDTO == null ) {
            return null;
        }

        OrganizationEntity organizationEntity = new OrganizationEntity();

        organizationEntity.setId( advertisementDTO.getOrgId() );

        return organizationEntity;
    }
}
