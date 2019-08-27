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
import org.springframework.security.core.SpringSecurityCoreVersion;
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

    public OrganizationResponse createOrganization(OrganizationDTO.OrganizationCreationDTO json) throws BusinessException {
        if (json.name == null) {
            throw new BusinessException("MISSING_PARAM: name","Required Organization name is empty", HttpStatus.NOT_ACCEPTABLE);
        } else if (!StringUtils.validateName(json.name)) {
            throw new BusinessException("INVALID_PARAM: name", "Required Organization name is invalid", HttpStatus.NOT_ACCEPTABLE);
        }
        if (json.pname == null) {
            throw new BusinessException("MISSING_PARAM: p_name", "Required Organization p_name is empty", HttpStatus.NOT_ACCEPTABLE);
        } else if (!json.pname.equals(StringUtils.encodeUrl(json.pname))) {
            throw new BusinessException("INVALID_PARAM: p_name", "Required Organization p_name is invalid", HttpStatus.NOT_ACCEPTABLE);
        }
        OrganizationEntity organizationEntity = organizationRepository.findByPname(json.pname);
        if (organizationEntity != null) {
            throw new BusinessException("INVALID_PARAM: p_name",
                    "Provided p_name is already used by another organization (id: " + organizationEntity.getId() +
                                ", name: " + organizationEntity.getName() + ")", HttpStatus.NOT_ACCEPTABLE);
        }
        OrganizationEntity newOrg = new OrganizationEntity();
        newOrg.setName(json.name);
        newOrg.setPname(json.pname);
        newOrg.setCreatedAt(new Date());
        newOrg.setCreatedAt(new Date());
        organizationRepository.save(newOrg);
        return new OrganizationResponse(newOrg.getId(), 0);
    }

    public OrganizationResponse updateOrganizationData(String userToken,
                                   OrganizationDTO.OrganizationModificationDTO json, MultipartFile file) throws BusinessException {
        if (json.organizationId == null) {
            throw new BusinessException("MISSING_PARAM: org_id", "Required org_id is missing", HttpStatus.NOT_ACCEPTABLE);
        }
        if (!organizationRepository.existsById(json.organizationId)) {
            throw new BusinessException("INVALID_PARAM: org_id", "Provided org_id is not matching any organization", HttpStatus.NOT_ACCEPTABLE);
        }
        if (!employeeUserRepository.findByAuthenticationToken(userToken).get().getOrganizationId().equals(json.organizationId)){
            throw new BusinessException("INSUFFICIENT_RIGHTS", "EmployeeUser is not admin of organization", HttpStatus.NOT_ACCEPTABLE);
        }
        OrganizationEntity organization = organizationRepository.findById(json.organizationId).get();
        if (json.description != null) {
            organization.setDescription(json.description);
        }
        //logo
        OrganizationThemeEntity orgTheme = null;
        if (json.logo != null || file != null) {
            orgTheme = organizationThemeRepository.findOneByOrganizationEntity_Id(json.organizationId);
            if (orgTheme == null) {
                orgTheme = new OrganizationThemeEntity();
                orgTheme.setCreatedAt(new Date());
                orgTheme.setOrganizationEntity(organization);
            }
            if (json.logoEncoding == null) {
                throw new BusinessException("Missing_PARAM: logo_encoding", "Provided logo_encoding is Missing", HttpStatus.NOT_ACCEPTABLE);
            }
            else if (json.logoEncoding.equals("form-data")) {
                String mimeType = file.getContentType();
                if(!mimeType.startsWith("image"))
                    throw new BusinessException("INVALID PARAM:image",
                            "Invalid file type["+mimeType+"]! only MIME 'image' types are accepted!", HttpStatus.NOT_ACCEPTABLE);
                orgTheme.setLogo(fileService.saveFile(file, json.organizationId));
                orgTheme.setUpdatedAt(new Date());
            } else if (json.logoEncoding.equals("base64")){
                orgTheme.setLogo(json.logo);
                orgTheme.setUpdatedAt(new Date());
            } else {
                throw new BusinessException("INVALID_PARAM: logo_encoding", "Provided logo_encoding is Invalid", HttpStatus.NOT_ACCEPTABLE);
            }
        }
        String [] result = helper.addSocialLinks(json, organization);
        if (result[0].equals("1"))
            throw new BusinessException(result[1], result[2], HttpStatus.NOT_ACCEPTABLE);
        if (!orgTheme.getLogo().equals(null)) {
            organizationThemeRepository.save(orgTheme);
        }
        organization.setUpdatedAt(new Date());
        organizationRepository.save(organization);
        return new OrganizationResponse();
    }

    public List<Organization_BrandRepresentationObject> getOrganizationBrands(Long orgId){
        List<Organization_BrandRepresentationObject> brands = null;
        if (orgId == null)
            return brands;
        List<BrandsEntity> brandsEntityList = brandsRepository.findByOrganizationEntity_Id(orgId);
        brands = brandsEntityList.stream().map(brand -> (Organization_BrandRepresentationObject) brand.getRepresentation())
                 .collect(Collectors.toList());
        return brands;
    }

    public OrganizationResponse createOrganizationBrand(String userToken, BrandDTO json, MultipartFile logo,
                                        MultipartFile banner) throws BusinessException {
        BrandsEntity brand = new BrandsEntity();
        if (json.name == null)
            throw new BusinessException("MISSING_PARAM: name", "'name' field can't be empty", HttpStatus.NOT_ACCEPTABLE);
        if (!StringUtils.validateName(json.name))
            throw new BusinessException("INVALID_PARAM: name", "Required Brand name is invalid", HttpStatus.NOT_ACCEPTABLE);
        brand.setName(json.name);
        if (json.pname != null) {
            if (!StringUtils.encodeUrl(json.pname).equals(json.pname)) {
                throw new BusinessException("INVALID_PARAM: p_name", "Required Organization p_name is invalid",
                        HttpStatus.NOT_ACCEPTABLE);
            }
        } else {
            brand.setPname(StringUtils.encodeUrl(json.name));
        }
        Long orgId = employeeUserRepository.getByAuthenticationToken(userToken).getOrganizationId();
        if (logo != null) {
            String mimeType = logo.getContentType();
            if(!mimeType.startsWith("image"))
                throw new BusinessException("INVALID PARAM:image",
                        "Invalid file type["+mimeType+"]! only MIME 'image' types are accepted!", HttpStatus.NOT_ACCEPTABLE);
            brand.setLogo(fileService.saveFile(logo, orgId));
        }
        if (banner != null) {
            String mimeType = banner.getContentType();
            if(!mimeType.startsWith("image"))
                throw new BusinessException("INVALID PARAM:image",
                        "Invalid file type["+mimeType+"]! only MIME 'image' types are accepted!", HttpStatus.NOT_ACCEPTABLE);
            brand.setBannerImage(fileService.saveFile(banner, orgId));
        }
        brand.setCreatedAt(new Date());
        brand.setUpdatedAt(new Date());
        brand.setOrganizationEntity(organizationRepository.getOne(orgId));
        brandsRepository.save(brand);
        return new OrganizationResponse(brand.getId(), 1);
    }

    public OrganizationResponse updateOrganizationBrand(String userToken, BrandDTO json, MultipartFile logo,
                                        MultipartFile banner) throws BusinessException {
        if (json.id == null) {
            throw new BusinessException("MISSING_PARAM: brand_id", "'brand_id' property can't be empty", HttpStatus.NOT_ACCEPTABLE);
        }
        if (!brandsRepository.existsById(json.id)) {
            throw new BusinessException("ENTITY NOT FOUND", "No Brand entity found with given id", HttpStatus.NOT_FOUND);
        }
        BrandsEntity brand = brandsRepository.findById(json.id).get();

        if (json.name != null) {
            if (!StringUtils.validateName(json.name))
                throw new BusinessException("INVALID_PARAM: name", "Brand name is invalid", HttpStatus.NOT_ACCEPTABLE);
            brand.setName(json.name);
        }
        if (json.pname != null) {
            if (!StringUtils.encodeUrl(json.pname).equals(json.pname)) {
                throw new BusinessException("INVALID_PARAM: p_name", "Required Organization p_name is invalid",
                        HttpStatus.NOT_ACCEPTABLE);
            }
        } else if (json.name != null) {
            brand.setPname(StringUtils.encodeUrl(json.name));
        }
        Long orgId = employeeUserRepository.getByAuthenticationToken(userToken).getOrganizationId();
        if (logo != null) {
            String mimeType = logo.getContentType();
            if(!mimeType.startsWith("image"))
                throw new BusinessException("INVALID PARAM: logo",
                        "Invalid file type["+mimeType+"]! only MIME 'image' types are accepted!", HttpStatus.NOT_ACCEPTABLE);
            brand.setLogo(fileService.saveFile(logo, orgId));
        }
        if (banner != null) {
            String mimeType = banner.getContentType();
            if(!mimeType.startsWith("image"))
                throw new BusinessException("INVALID PARAM: banner",
                        "Invalid file type["+mimeType+"]! only MIME 'image' types are accepted!", HttpStatus.NOT_ACCEPTABLE);
            brand.setBannerImage(fileService.saveFile(banner, orgId));
        }
        brand.setUpdatedAt(new Date());
        brandsRepository.save(brand);
        return new OrganizationResponse(brand.getId(), 1);
    }
}