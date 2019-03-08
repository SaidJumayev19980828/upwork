package com.nasnav.service;

import com.nasnav.dao.BrandsRepository;
import com.nasnav.dao.OrganizationRepository;
import com.nasnav.dao.OrganizationThemeRepository;
import com.nasnav.dao.SocialRepository;
import com.nasnav.dto.*;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.persistence.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
public class OrganizationServiceImpl implements OrganizationService{

    private final OrganizationRepository organizationRepository;

    private final SocialRepository socialRepository;

    private final OrganizationThemeRepository organizationThemeRepository;

    private final BrandsRepository brandsRepository;

    @Autowired
    public OrganizationServiceImpl(OrganizationRepository organizationRepository,BrandsRepository brandsRepository,SocialRepository socialRepository,OrganizationThemeRepository organizationThemeRepository){
        this.organizationRepository = organizationRepository;
        this.socialRepository = socialRepository;
        this.organizationThemeRepository = organizationThemeRepository;
        this.brandsRepository = brandsRepository;
    }
    @Override
    public OrganizationRepresentationObject getOrganizationByName(String organizationName) throws BusinessException {

        OrganizationEntity organizationEntity = organizationRepository.findOneByNameContainingIgnoreCase(organizationName);

        if(organizationEntity==null)
            throw new BusinessException("Organization not found",null, HttpStatus.NOT_FOUND);

        OrganizationRepresentationObject organizationRepresentationObject = ((OrganizationRepresentationObject)organizationEntity.getRepresentation());

        //TODO add brandRepresentationObjects from other repository
        SocialEntity socialEntity = socialRepository.findOneByOrganizationEntity_Id(organizationRepresentationObject.getId());

        if(socialEntity!=null){
            organizationRepresentationObject.setSocial((SocialRepresentationObject)socialEntity.getRepresentation());
        }

        OrganizationThemeEntity organizationThemeEntity = organizationThemeRepository.findOneByOrganizationEntity_Id(organizationRepresentationObject.getId());

        if(organizationThemeEntity!=null){
            organizationRepresentationObject.setThemes((OrganizationThemesRepresentationObject) organizationThemeEntity.getRepresentation());
        }

        List<BrandsEntity> brandsEntityList = brandsRepository.findByOrganizationEntity_Id(organizationRepresentationObject.getId());

        if(brandsEntityList!=null && !brandsEntityList.isEmpty()){

            List<BrandRepresentationObject> repList = brandsEntityList.stream().map(rep->((BrandRepresentationObject)rep.getRepresentation())).collect(Collectors.toList());
            organizationRepresentationObject.setBrands(repList);
        }
        return  organizationRepresentationObject;
    }

    @Override
    public CategoryRepresentationObject getOrganizationCategories(Long organizationId) {
        return null;
    }

    @Override
    public ShopRepresentationObject getOrganizationShops(Long organizationId) {
        return null;
    }
}
