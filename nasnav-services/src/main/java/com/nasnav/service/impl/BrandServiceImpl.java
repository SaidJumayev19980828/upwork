package com.nasnav.service.impl;

import com.nasnav.commons.utils.CustomOffsetAndLimitPageRequest;
import com.nasnav.dao.BrandsRepository;
import com.nasnav.dao.ProductRepository;
import com.nasnav.dto.Organization_BrandRepresentationObject;
import com.nasnav.dto.request.BrandIdAndPriority;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.BrandsEntity;
import com.nasnav.service.BrandService;
import com.nasnav.service.SecurityService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.cache.annotation.CacheResult;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import java.util.*;

import static com.nasnav.cache.Caches.*;
import static com.nasnav.commons.utils.PagingUtils.getQueryPage;
import static com.nasnav.exceptions.ErrorCodes.*;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class BrandServiceImpl implements BrandService {

    @PersistenceContext
    @Autowired
    private EntityManager em;

    @Autowired
    private BrandsRepository brandsRepository;
    @Autowired
    private ProductRepository productRepo;

    @Autowired
    private SecurityService securityService;

    
    @Override
    @CacheResult(cacheName = BRANDS)
    public Organization_BrandRepresentationObject getBrandById(Long brandId, boolean yeshteryState){
        if (brandId == null)
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, P$PRO$0005);
        Optional<BrandsEntity> brand = yeshteryState ? brandsRepository.findYeshteryBrandById(brandId) : brandsRepository.findById(brandId);

        return brand
                .map(b -> (Organization_BrandRepresentationObject) b.getRepresentation())
                .orElseThrow(() -> new RuntimeBusinessException(NOT_FOUND,GEN$0001,"brand", brandId));
    }


    @Override
    @CacheEvict(allEntries = true, cacheNames = {BRANDS, ORGANIZATIONS_BY_NAME, ORGANIZATIONS_BY_ID})
    public void deleteBrand(Long brandId) throws BusinessException {
        Long orgId = securityService.getCurrentUserOrganizationId();

        if (!brandsRepository.existsByIdAndOrganizationEntity_IdAndRemoved(brandId, orgId, 0)) {
            throw new BusinessException(String.format("Provided brand_id %d doesn't match any existing brand!", brandId),
                    "INVALID_PARAM: brand_id", NOT_ACCEPTABLE);
        }

        List<Long> linkedProducts = productRepo.findByBrandId(brandId);
        if (linkedProducts.size() > 0) {
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, P$BRA$0003, brandId, linkedProducts.toString());
        }

        brandsRepository.setBrandHidden(brandId);
    }

    @Override
    public PageImpl<Organization_BrandRepresentationObject> getYeshteryBrands(Integer start, Integer count, Long orgId, Set<Long> brandIds) {
        if (start < 0) {
            start = 0;
        }
        if (count <= 0) {
            count = 10;
        }
        PageRequest page = getQueryPage(start, count);
        if ((brandIds != null && !brandIds.isEmpty()) && orgId == null) {
            return brandsRepository.findByIdInAndOrganizationEntity_YeshteryState(brandIds, page);
        } else if ((brandIds != null && !brandIds.isEmpty()) && orgId != null) {
            return brandsRepository.findByIdInAndYeshteryOrganization(brandIds, orgId, page);
        } else if ((brandIds == null || brandIds.isEmpty()) && orgId != null) {
            return brandsRepository.findByYeshteryOrganization(orgId, page);
        }
        return brandsRepository.findByOrganizationEntity_YeshteryState(page);

    }

    @Override
    public PageImpl<Organization_BrandRepresentationObject> getOrganizationBrands(List<Long> orgIds, Integer minPriority ,Integer start ,Integer count){
        Pageable page =new CustomOffsetAndLimitPageRequest(start,count);
        PageImpl<BrandsEntity> brandsPageable=
        brandsRepository.findByOrganizationEntity_IdInAndRemovedAndPriorityGreaterThanEqualOrderByPriorityDesc(orgIds, 0, minPriority,page);
        List<Organization_BrandRepresentationObject> brands = brandsPageable.getContent().stream()
                .map(brand -> (Organization_BrandRepresentationObject) brand.getRepresentation())
                .collect(toList());
        return new PageImpl<>(brands, brandsPageable.getPageable(), brandsPageable.getTotalElements());
    }


    @Override
    public void changeBrandsPriority(List<BrandIdAndPriority> dto) {
        Map<Long, Integer> brandsPrioritiesMap = dto.stream().collect(toMap(BrandIdAndPriority::getId, BrandIdAndPriority::getPriority));
        List<BrandsEntity> entities = brandsRepository.findByIdInAndRemoved( brandsPrioritiesMap.keySet(), 0);

        entities.stream()
                .filter(brand -> brandsPrioritiesMap.get(brand.getId()) != null)
                .forEach(brand -> brand.setPriority(brandsPrioritiesMap.get(brand.getId())));

        brandsRepository.saveAll(entities);
    }

}
