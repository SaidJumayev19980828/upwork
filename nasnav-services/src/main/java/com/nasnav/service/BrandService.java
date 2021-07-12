package com.nasnav.service;

import com.nasnav.dao.BrandsRepository;
import com.nasnav.dao.ProductRepository;
import com.nasnav.dto.BaseRepresentationObject;
import com.nasnav.dto.Organization_BrandRepresentationObject;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.exceptions.RuntimeBusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import javax.cache.annotation.CacheResult;

import static com.nasnav.cache.Caches.*;
import static com.nasnav.commons.utils.PagingUtils.getQueryPage;
import static com.nasnav.exceptions.ErrorCodes.*;
import static java.util.stream.Collectors.toList;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class BrandService {

    @Autowired
    private BrandsRepository brandsRepository;
    @Autowired
    private ProductRepository productRepo;

    @Autowired
    private SecurityService securityService;

    
    @CacheResult(cacheName = BRANDS)
    public Organization_BrandRepresentationObject getBrandById(Long brandId){
        if (brandId == null)
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, P$PRO$0005);

        return brandsRepository.findById(brandId)
                .map(brand -> (Organization_BrandRepresentationObject) brand.getRepresentation())
                .orElseThrow(() -> new RuntimeBusinessException(NOT_FOUND,GEN$0001,"brand", brandId));
    }


    @CacheEvict(allEntries = true, cacheNames = {BRANDS, ORGANIZATIONS_BY_NAME, ORGANIZATIONS_BY_ID})
    public void deleteBrand(Long brandId) throws BusinessException {
        Long orgId = securityService.getCurrentUserOrganizationId();

        if (!brandsRepository.existsByIdAndOrganizationEntity_IdAndRemoved(brandId, orgId, 0)) {
            throw new BusinessException(String.format("Provided brand_id %d doesn't match any existing brand!", brandId),
                    "INVALID_PARAM: brand_id", NOT_ACCEPTABLE);
        }

        Long linkedProductsCount = productRepo.countByBrandId(brandId);
        if (linkedProductsCount > 0) {
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, P$BRA$0003, brandId, linkedProductsCount);
        }

        brandsRepository.setBrandHidden(brandId);
    }

    public PageImpl<Organization_BrandRepresentationObject> getYeshteryBrands(Integer start, Integer count) {
        if (start < 0) {
            start = 0;
        }
        if (count <= 0) {
            count = 10;
        }
        PageRequest page = getQueryPage(start, count);
        return brandsRepository.findByOrganizationEntity_YeshteryState(page);
    }

}
