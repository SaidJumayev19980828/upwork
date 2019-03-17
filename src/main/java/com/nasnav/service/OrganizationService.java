package com.nasnav.service;

import com.nasnav.dao.BrandsRepository;
import com.nasnav.dao.OrganizationRepository;
import com.nasnav.dao.OrganizationThemeRepository;
import com.nasnav.dao.SocialRepository;
import com.nasnav.dto.OrganizationRepresentationObject;
import com.nasnav.dto.OrganizationThemesRepresentationObject;
import com.nasnav.dto.Organization_BrandRepresentationObject;
import com.nasnav.dto.SocialRepresentationObject;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.persistence.BrandsEntity;
import com.nasnav.persistence.OrganizationEntity;
import com.nasnav.persistence.OrganizationThemeEntity;
import com.nasnav.persistence.SocialEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
public class OrganizationService {

    private final OrganizationRepository organizationRepository;

    private final SocialRepository socialRepository;

    private final OrganizationThemeRepository organizationThemeRepository;

    private final BrandsRepository brandsRepository;

    @Autowired
    public OrganizationService(OrganizationRepository organizationRepository, BrandsRepository brandsRepository, SocialRepository socialRepository, OrganizationThemeRepository organizationThemeRepository) {
        this.organizationRepository = organizationRepository;
        this.socialRepository = socialRepository;
        this.organizationThemeRepository = organizationThemeRepository;
        this.brandsRepository = brandsRepository;
    }

   public OrganizationRepresentationObject getOrganizationByName(String organizationName) throws BusinessException {

        OrganizationEntity organizationEntity = organizationRepository.findOneByNameContainingIgnoreCase(organizationName);

        if (organizationEntity == null)
            throw new BusinessException("Organization not found", null, HttpStatus.NOT_FOUND);

        OrganizationRepresentationObject organizationRepresentationObject = ((OrganizationRepresentationObject) organizationEntity.getRepresentation());

        //TODO add brandRepresentationObjects from other repository
        SocialEntity socialEntity = socialRepository.findOneByOrganizationEntity_Id(organizationRepresentationObject.getId());

        if (socialEntity != null) {
            organizationRepresentationObject.setSocial((SocialRepresentationObject) socialEntity.getRepresentation());
        }

        OrganizationThemeEntity organizationThemeEntity = organizationThemeRepository.findOneByOrganizationEntity_Id(organizationRepresentationObject.getId());

        if (organizationThemeEntity != null) {
            organizationRepresentationObject.setThemes((OrganizationThemesRepresentationObject) organizationThemeEntity.getRepresentation());
        }

        List<BrandsEntity> brandsEntityList = brandsRepository.findByOrganizationEntity_Id(organizationRepresentationObject.getId());

        if (brandsEntityList != null && !brandsEntityList.isEmpty()) {

            List<Organization_BrandRepresentationObject> repList = brandsEntityList.stream().map(rep -> ((Organization_BrandRepresentationObject) rep.getRepresentation())).collect(Collectors.toList());
            organizationRepresentationObject.setBrands(repList);
        }
        return organizationRepresentationObject;
    }

    public OrganizationRepresentationObject getOrganizationById(Long organizationId) throws BusinessException {

        OrganizationEntity organizationEntity = organizationRepository.findOneById(organizationId);

        if (organizationEntity == null)
            throw new BusinessException("Organization not found", null, HttpStatus.NOT_FOUND);

        OrganizationRepresentationObject organizationRepresentationObject = ((OrganizationRepresentationObject) organizationEntity.getRepresentation());

        //TODO add brandRepresentationObjects from other repository
        SocialEntity socialEntity = socialRepository.findOneByOrganizationEntity_Id(organizationRepresentationObject.getId());

        if (socialEntity != null) {
            organizationRepresentationObject.setSocial((SocialRepresentationObject) socialEntity.getRepresentation());
        }

        OrganizationThemeEntity organizationThemeEntity = organizationThemeRepository.findOneByOrganizationEntity_Id(organizationRepresentationObject.getId());

        if (organizationThemeEntity != null) {
            organizationRepresentationObject.setThemes((OrganizationThemesRepresentationObject) organizationThemeEntity.getRepresentation());
        }

        List<BrandsEntity> brandsEntityList = brandsRepository.findByOrganizationEntity_Id(organizationRepresentationObject.getId());

        if (brandsEntityList != null && !brandsEntityList.isEmpty()) {

            List<Organization_BrandRepresentationObject> repList = brandsEntityList.stream().map(rep -> ((Organization_BrandRepresentationObject) rep.getRepresentation())).collect(Collectors.toList());
            organizationRepresentationObject.setBrands(repList);
        }
        return organizationRepresentationObject;
    }

}