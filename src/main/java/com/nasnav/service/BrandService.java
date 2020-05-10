package com.nasnav.service;

import static com.nasnav.cache.Caches.BRANDS;
import static com.nasnav.cache.Caches.ORGANIZATIONS_BY_ID;
import static com.nasnav.cache.Caches.ORGANIZATIONS_BY_NAME;

import java.util.List;
import java.util.Optional;

import javax.cache.annotation.CacheResult;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.nasnav.cache.Caches;
import com.nasnav.dao.BrandsRepository;
import com.nasnav.dto.Organization_BrandRepresentationObject;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.persistence.BrandsEntity;

@Service
public class BrandService {

    @Autowired
    private BrandsRepository brandsRepository;

    @Autowired
    private SecurityService securityService;

    
    @CacheResult(cacheName = BRANDS)
    public Organization_BrandRepresentationObject getBrandById(Long brandId){

        Optional<BrandsEntity> brandsEntityOptional = brandsRepository.findById(brandId);

        if(brandsEntityOptional!=null && brandsEntityOptional.isPresent()){
            return  ((Organization_BrandRepresentationObject)brandsEntityOptional.get().getRepresentation());
        }
        return null;
    }


    @CacheEvict(allEntries = true, cacheNames = {BRANDS, ORGANIZATIONS_BY_NAME, ORGANIZATIONS_BY_ID})
    public void deleteBrand(Long brandId) throws BusinessException {
        Long orgId = securityService.getCurrentUserOrganizationId();

        if (!brandsRepository.existsByIdAndOrganizationEntity_Id(brandId, orgId)) {
            throw new BusinessException(String.format("Provided brand_id %d doesn't match any existing brand!", brandId),
                    "INVALID_PARAM: brand_id", HttpStatus.NOT_ACCEPTABLE);
        }

        List<Long> productsList = brandsRepository.getProductsByBrandId(brandId);
        if (productsList.size() > 0) {
            throw new BusinessException("There are products "+productsList.toString()+" linked to this brand",
                    "INVALID_OPERATION", HttpStatus.NOT_ACCEPTABLE);
        }

        List<Long> shopsList = brandsRepository.getShopsByBrandId(brandId);
        if (shopsList.size() > 0) {
            throw new BusinessException("There are shops "+shopsList.toString()+" linked to this brand",
                    "INVALID_OPERATION", HttpStatus.NOT_ACCEPTABLE);
        }

        brandsRepository.deleteById(brandId);
    }

}
