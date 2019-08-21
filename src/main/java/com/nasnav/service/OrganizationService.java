package com.nasnav.service;

import com.nasnav.commons.utils.StringUtils;
import com.nasnav.dao.BrandsRepository;
import com.nasnav.dao.OrganizationRepository;
import com.nasnav.dao.OrganizationThemeRepository;
import com.nasnav.dao.SocialRepository;
import com.nasnav.dao.ExtraAttributesRepository;
import com.nasnav.dto.*;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.persistence.BrandsEntity;
import com.nasnav.persistence.OrganizationEntity;
import com.nasnav.persistence.OrganizationThemeEntity;
import com.nasnav.persistence.SocialEntity;
import com.nasnav.persistence.ExtraAttributesEntity;
import com.nasnav.response.OrganizationResponse;
import com.nasnav.service.helpers.OrganizationServiceHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class OrganizationService {

    private final OrganizationRepository organizationRepository;

    private final SocialRepository socialRepository;

    private final OrganizationThemeRepository organizationThemeRepository;

    private final BrandsRepository brandsRepository;

    private final ExtraAttributesRepository extraAttributesRepository;

    private OrganizationServiceHelper helper;

    @Autowired
    public OrganizationService(OrganizationRepository organizationRepository, BrandsRepository brandsRepository, SocialRepository socialRepository,
                               OrganizationThemeRepository organizationThemeRepository,ExtraAttributesRepository extraAttributesRepository,
                               OrganizationServiceHelper helper) {
        this.organizationRepository = organizationRepository;
        this.socialRepository = socialRepository;
        this.organizationThemeRepository = organizationThemeRepository;
        this.brandsRepository = brandsRepository;
        this.extraAttributesRepository = extraAttributesRepository;
        this.helper = helper;
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


    public List<ExtraAttributesRepresentationObject> getOrganizationExtraAttributesById(Long organizationId){
        List<ExtraAttributesEntity> extraAttributes = null;
        if (organizationId == null) {
            extraAttributes = extraAttributesRepository.findAll();
        } else {
            extraAttributes = extraAttributesRepository.findByOrganizationId(organizationId);
        }
        List<ExtraAttributesRepresentationObject> response;
        response = extraAttributes.stream()
                .map(extraAttribute -> (ExtraAttributesRepresentationObject) extraAttribute.getRepresentation())
                .collect(Collectors.toList());
        return response;
    }

    public OrganizationResponse createOrganization(OrganizationDTO.OrganizationCreationDTO json){
        if (json.name == null) {
            return new OrganizationResponse("MISSING_PARAM: name","Required Organization name is empty");
        } else if (!StringUtils.validateName(json.name)) {
            return new OrganizationResponse("INVALID_PARAM: name", "Required Organization name is invalid");
        }
        if (json.pname == null) {
            return new OrganizationResponse("MISSING_PARAM: p_name", "Required Organization p_name is empty");
        } else if (!json.pname.equals(StringUtils.encodeUrl(json.pname))) {
            return new OrganizationResponse("INVALID_PARAM: p_name", "Required Organization p_name is invalid");
        }
        OrganizationEntity organizationEntity = organizationRepository.findByPname(json.pname);
        if (organizationEntity != null) {
            return new OrganizationResponse("INVALID_PARAM: p_name",
                    "Provided p_name is already used by another organization (id: " + organizationEntity.getId() +
                                ", name: " + organizationEntity.getName() + ")");
        }
        OrganizationEntity newOrg = new OrganizationEntity();
        newOrg.setName(json.name);
        newOrg.setPname(json.pname);
        newOrg.setCreatedAt(new Date());
        newOrg.setCreatedAt(new Date());
        organizationRepository.save(newOrg);
        return new OrganizationResponse(newOrg.getId());
    }

    public OrganizationResponse updateOrganizationData(OrganizationDTO.OrganizationModificationDTO json) {
        if (!organizationRepository.existsById(json.organizationId)) {
            return new OrganizationResponse("INVALID_PARAM: org_id", "Provided org_id is not matching any organization");
        }
        OrganizationEntity organization = organizationRepository.findById(json.organizationId).get();
        if (json.description != null) {
            organization.setDescription(json.description);
        }
        //logo
        if (json.logo != null) {
            if (json.logoEncoding == null) {

            }
            else if (json.logoEncoding.equals("form-data")) {

            } else if (json.logoEncoding.equals("base64")){

            } else {

            }
        }
        String [] result = helper.addSocialLinks(json, organization);
        if (result[0].equals("1"))
            return new OrganizationResponse(result[1], result[2]);

        organization.setUpdatedAt(new Date());
        organizationRepository.save(organization);
        return new OrganizationResponse();
    }
}