package com.nasnav.service;

import com.nasnav.dao.BrandsRepository;
import com.nasnav.dto.Organization_BrandRepresentationObject;
import com.nasnav.persistence.BrandsEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class BrandService {

    private final BrandsRepository brandsRepository;

    @Autowired
    public BrandService(BrandsRepository brandsRepository){
        this.brandsRepository = brandsRepository;
    }

    public Organization_BrandRepresentationObject getBrandById(Long brandId){

        Optional<BrandsEntity> brandsEntityOptional = brandsRepository.findById(brandId);

        if(brandsEntityOptional!=null && brandsEntityOptional.isPresent()){
            return  ((Organization_BrandRepresentationObject)brandsEntityOptional.get().getRepresentation());
        }
        return null;
    }
}
