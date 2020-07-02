package com.nasnav.service;

import static com.nasnav.cache.Caches.BRANDS;
import static com.nasnav.cache.Caches.ORGANIZATIONS_BY_ID;
import static com.nasnav.cache.Caches.ORGANIZATIONS_BY_NAME;
import static com.nasnav.exceptions.ErrorCodes.GEN$0001;
import static com.nasnav.exceptions.ErrorCodes.P$PRO$0005;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import java.util.List;

import javax.cache.annotation.CacheResult;

import com.nasnav.exceptions.RuntimeBusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import com.nasnav.dao.BrandsRepository;
import com.nasnav.dto.Organization_BrandRepresentationObject;
import com.nasnav.exceptions.BusinessException;

@Service
public class BrandService {

    @Autowired
    private BrandsRepository brandsRepository;

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

        if (!brandsRepository.existsByIdAndOrganizationEntity_Id(brandId, orgId)) {
            throw new BusinessException(String.format("Provided brand_id %d doesn't match any existing brand!", brandId),
                    "INVALID_PARAM: brand_id", NOT_ACCEPTABLE);
        }

        List<Long> productsList = brandsRepository.getProductsByBrandId(brandId);
        if (productsList.size() > 0) {
            throw new BusinessException("There are products "+productsList.toString()+" linked to this brand",
                    "INVALID_OPERATION", NOT_ACCEPTABLE);
        }

        List<Long> shopsList = brandsRepository.getShopsByBrandId(brandId);
        if (shopsList.size() > 0) {
            throw new BusinessException("There are shops "+shopsList.toString()+" linked to this brand",
                    "INVALID_OPERATION", NOT_ACCEPTABLE);
        }

        brandsRepository.deleteById(brandId);
    }

}
