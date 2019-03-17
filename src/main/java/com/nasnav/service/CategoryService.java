package com.nasnav.service;

import com.nasnav.dao.BrandsRepository;
import com.nasnav.dao.OrganizationRepository;
import com.nasnav.dao.OrganizationThemeRepository;
import com.nasnav.dao.SocialRepository;
import com.nasnav.dto.CategoryRepresentationObject;
import com.nasnav.dto.ShopRepresentationObject;
import com.nasnav.persistence.BrandsEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CategoryService {

    private final BrandsRepository brandsRepository;

    @Autowired
    public CategoryService(OrganizationRepository organizationRepository, BrandsRepository brandsRepository, SocialRepository socialRepository, OrganizationThemeRepository organizationThemeRepository) {
        this.brandsRepository = brandsRepository;
    }

    public CategoryRepresentationObject getOrganizationCategories(Long organizationId) {

        List<BrandsEntity> brandsEntities = brandsRepository.findByOrganizationEntity_Id(organizationId);

        if(brandsEntities!=null && !brandsEntities.isEmpty()){
            List<String> categories = new ArrayList<>(brandsEntities.size());

//            brandsEntities.forEach(brandsEntity -> categories.add(brandsEntity.getCategories()));
        }

        //TODO brands table fix of categories Varchar [] to bigint []
        return null;
    }

}
