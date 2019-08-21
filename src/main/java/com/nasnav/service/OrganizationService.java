package com.nasnav.service;

import com.nasnav.commons.utils.StringUtils;
import com.nasnav.dao.*;
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
import org.springframework.web.multipart.MultipartFile;

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

    private final OrganizationServiceHelper helper;

    private final FileService fileService;

    private final EmployeeUserRepository employeeUserRepository;

    @Autowired
    public OrganizationService(OrganizationRepository organizationRepository, BrandsRepository brandsRepository, SocialRepository socialRepository,
                               OrganizationThemeRepository organizationThemeRepository,ExtraAttributesRepository extraAttributesRepository,
                               OrganizationServiceHelper helper, FileService fileService, EmployeeUserRepository employeeUserRepository) {
        this.organizationRepository = organizationRepository;
        this.socialRepository = socialRepository;
        this.organizationThemeRepository = organizationThemeRepository;
        this.brandsRepository = brandsRepository;
        this.extraAttributesRepository = extraAttributesRepository;
        this.helper = helper;
        this.fileService = fileService;
        this.employeeUserRepository = employeeUserRepository;
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

    public OrganizationResponse updateOrganizationData(String userToken,
                                   OrganizationDTO.OrganizationModificationDTO json, MultipartFile file) throws BusinessException {
        if (!organizationRepository.existsById(json.organizationId)) {
            return new OrganizationResponse("INVALID_PARAM: org_id", "Provided org_id is not matching any organization");
        }
        if (!employeeUserRepository.findByAuthenticationToken(userToken).get().getOrganizationId().equals(json.organizationId)){
            return new OrganizationResponse("INSUFFICIENT_RIGHTS", "EmployeeUser is not admin of organization");
        }
        OrganizationEntity organization = organizationRepository.findById(json.organizationId).get();
        if (json.description != null) {
            organization.setDescription(json.description);
        }
        //logo
        if (json.logo != null || file != null) {
            if (json.logoEncoding == null) {
                return new OrganizationResponse("Missing_PARAM: logo_encoding", "Provided logo_encoding is Missing");
            }
            else if (json.logoEncoding.equals("form-data")) {
                String mimeType = file.getContentType();
                if(!mimeType.startsWith("image"))
                    return new OrganizationResponse("MISSIG PARAM:image",
                            "Invalid file type["+mimeType+"]! only MIME 'image' types are accepted!");
                organization.setLogo(fileService.saveFile(file, json.organizationId));
            } else if (json.logoEncoding.equals("base64")){
                organization.setLogo(json.logo);
            } else {
                return new OrganizationResponse("INVALID_PARAM: logo_encoding", "Provided logo_encoding is Invalid");
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