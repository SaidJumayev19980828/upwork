package com.nasnav.service;

import static com.nasnav.cache.Caches.BRANDS;
import static com.nasnav.cache.Caches.ORGANIZATIONS_BY_ID;
import static com.nasnav.cache.Caches.ORGANIZATIONS_BY_NAME;
import static com.nasnav.cache.Caches.ORGANIZATIONS_DOMAINS;
import static com.nasnav.cache.Caches.ORGANIZATIONS_EXTRA_ATTRIBUTES;
import static com.nasnav.commons.utils.StringUtils.encodeUrl;
import static com.nasnav.commons.utils.StringUtils.isBlankOrNull;
import static com.nasnav.commons.utils.StringUtils.validateName;
import static com.nasnav.constatnts.EntityConstants.NASNAV_DOMAIN;
import static com.nasnav.constatnts.EntityConstants.NASORG_DOMAIN;
import static com.nasnav.exceptions.ErrorCodes.ORG$EXTRATTR$0001;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.cache.annotation.CacheResult;

import com.nasnav.dto.response.OrgThemeRepObj;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.*;
import org.apache.http.client.utils.URIBuilder;
import org.json.JSONObject;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.nasnav.constatnts.EntityConstants.Operation;
import com.nasnav.dao.*;
import com.nasnav.dto.BrandDTO;
import com.nasnav.dto.ExtraAttributeDTO;
import com.nasnav.dto.ExtraAttributeDefinitionDTO;
import com.nasnav.dto.ExtraAttributesRepresentationObject;
import com.nasnav.dto.OrganizationDTO;
import com.nasnav.dto.OrganizationImageUpdateDTO;
import com.nasnav.dto.OrganizationImagesRepresentationObject;
import com.nasnav.dto.OrganizationRepresentationObject;
import com.nasnav.dto.OrganizationThemesRepresentationObject;
import com.nasnav.dto.Organization_BrandRepresentationObject;
import com.nasnav.dto.Pair;
import com.nasnav.dto.ProductFeatureDTO;
import com.nasnav.dto.ProductFeatureUpdateDTO;
import com.nasnav.dto.SocialRepresentationObject;
import com.nasnav.dto.ThemeDTO;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.response.OrganizationResponse;
import com.nasnav.response.ProductFeatureUpdateResponse;
import com.nasnav.response.ProductImageUpdateResponse;
import com.nasnav.service.helpers.OrganizationServiceHelper;


@Service
public class OrganizationService {
    @Autowired
    private OrganizationRepository organizationRepository;
    @Autowired
    private SocialRepository socialRepository;
    @Autowired
    private OrganizationThemeRepository organizationThemeRepository;
    @Autowired
    private BrandsRepository brandsRepository;
    @Autowired
    private ExtraAttributesRepository extraAttributesRepository;
    @Autowired
    private OrganizationServiceHelper helper;
    @Autowired
    private FileService fileService;
    @Autowired
    private OrganizationImagesRepository organizationImagesRepository;
    @Autowired
    private ShopsRepository shopsRepository;
    @Autowired
	private ProductFeaturesRepository featureRepo;
    @Autowired
    private EmployeeUserRepository empRepo;
    @Autowired
    private OrganizationDomainsRepository orgDomainsRep;
	@Autowired
    private OrganizationRepository orgRepo;
    @Autowired
    private SecurityService securityService;
    @Autowired
    private OrganizationThemeSettingsRepository orgThemesSettingsRepo;
    @Autowired
    private ThemesRepository themesRepo;
    @Autowired
    private ProductExtraAttributesEntityRepository productExtraAttrRepo;
    @Autowired
    private ExtraAttributesRepository extraAttrRepo;


    public List<OrganizationRepresentationObject> listOrganizations() {
        return organizationRepository.findAll()
                            .stream()
                            .map(org -> (OrganizationRepresentationObject) org.getRepresentation())
                            .collect(toList());
    }


    
    @CacheResult(cacheName = ORGANIZATIONS_BY_NAME)
    public OrganizationRepresentationObject getOrganizationByName(String organizationName) throws BusinessException {
        OrganizationEntity organizationEntity = organizationRepository.findByPname(organizationName);
        if (organizationEntity == null)
            organizationEntity = organizationRepository.findOneByNameIgnoreCase(organizationName);

        if (organizationEntity == null)
            throw new BusinessException("Organization not found", "", NOT_FOUND);

        return getOrganizationAdditionalData(organizationEntity);
    }
    
    
    
    @CacheResult(cacheName = ORGANIZATIONS_BY_ID)
    public OrganizationRepresentationObject getOrganizationById(Long organizationId) throws BusinessException {
        OrganizationEntity organizationEntity = organizationRepository.findOneById(organizationId);

        if (organizationEntity == null)
            throw new BusinessException("Organization not found", "", NOT_FOUND);

        return getOrganizationAdditionalData(organizationEntity);
    }

    
    
    
    
    private OrganizationRepresentationObject getOrganizationAdditionalData(OrganizationEntity entity) {
        OrganizationRepresentationObject orgRepObj = ((OrganizationRepresentationObject) entity.getRepresentation());

        SocialEntity socialEntity = socialRepository.findOneByOrganizationEntity_Id(orgRepObj.getId());
        if (socialEntity != null)
            orgRepObj.setSocial((SocialRepresentationObject) socialEntity.getRepresentation());

        OrganizationThemeEntity organizationThemeEntity = organizationThemeRepository.findOneByOrganizationEntity_Id(orgRepObj.getId());
        if (organizationThemeEntity != null)
            orgRepObj.setThemes((OrganizationThemesRepresentationObject) organizationThemeEntity.getRepresentation());

        List<BrandsEntity> brandsEntityList = brandsRepository.findByOrganizationEntity_IdAndRemoved(orgRepObj.getId(), 0);
        if (brandsEntityList != null && !brandsEntityList.isEmpty()) {
            List<Organization_BrandRepresentationObject> repList = brandsEntityList.stream().map(rep -> ((Organization_BrandRepresentationObject) rep.getRepresentation())).collect(toList());
            orgRepObj.setBrands(repList);
        }

        List <OrganizationImagesEntity> orgImgEntities = organizationImagesRepository.findByOrganizationEntityId(orgRepObj.getId());
        if (orgImgEntities != null && !orgImgEntities.isEmpty()) {
            List<OrganizationImagesRepresentationObject> imagesList = orgImgEntities.stream().map(rep -> ((OrganizationImagesRepresentationObject) rep.getRepresentation())).collect(toList());
            orgRepObj.setImages(imagesList);
        }

        orgRepObj.setTheme(getOrganizationThemeDTO(orgRepObj));

        return orgRepObj;
    }

    private OrgThemeRepObj getOrganizationThemeDTO(OrganizationRepresentationObject orgRepObj) {
        if (orgRepObj.getThemeId() == null)
            return null;

        OrgThemeRepObj themeRepObj = new OrgThemeRepObj();

        Optional<ThemeEntity> optionalThemeEntity = themesRepo.findByUid(orgRepObj.getThemeId());

        if (optionalThemeEntity.isPresent()) {
            ThemeEntity themeEntity = optionalThemeEntity.get();
            ThemeDTO themeDTO = (ThemeDTO)themeEntity.getRepresentation();
            BeanUtils.copyProperties(themeDTO, themeRepObj);
            themeRepObj.setDefaultSettings(new JSONObject(themeDTO.getDefaultSettings()).toMap());
        }

        Optional<OrganizationThemesSettingsEntity> optionalThemeSettings =
                orgThemesSettingsRepo.findByOrganizationEntity_IdAndThemeId(orgRepObj.getId(), Integer.parseInt(orgRepObj.getThemeId()));

        if (optionalThemeSettings.isPresent()) {
            OrganizationThemesSettingsEntity themesSettings = optionalThemeSettings.get();
            themeRepObj.setSettings(new JSONObject(themesSettings.getSettings()).toMap());
        }

        return themeRepObj;
    }
    
    @CacheResult(cacheName = ORGANIZATIONS_EXTRA_ATTRIBUTES)
    public List<ExtraAttributesRepresentationObject> getOrganizationExtraAttributesById(Long organizationId){
        List<ExtraAttributesEntity> extraAttributes;
        if (organizationId == null) {
            extraAttributes = extraAttributesRepository.findAll();
        } else {
            extraAttributes = extraAttributesRepository.findByOrganizationId(organizationId);
        }

        return extraAttributes.stream()
                .map(extraAttribute -> (ExtraAttributesRepresentationObject) extraAttribute.getRepresentation())
                .collect(toList());
    }
    
    
    
    
    
    @CacheEvict(allEntries = true, cacheNames = { ORGANIZATIONS_BY_NAME, ORGANIZATIONS_BY_ID})
    public OrganizationResponse createOrganization(OrganizationDTO.OrganizationCreationDTO json) throws BusinessException {
        validateOrganizationName(json);

        OrganizationEntity organization = new OrganizationEntity();
        if (json.id != null) {
            organization = orgRepo.findOneById(json.id);
            if (organization == null)
                throw new BusinessException(String.format("Provided id (%d) doesn't match any existing org!", json.id),
                        "INVALID_PARAM: id", NOT_ACCEPTABLE);
        }

	    OrganizationEntity organizationEntity = organizationRepository.findByPname(json.pname);
	    if (organizationEntity != null) {
		    if (!organization.getId().equals(organizationEntity.getId())) {
			    throw new BusinessException("INVALID_PARAM: p_name",
					    "Provided p_name is already used by another organization (id: " + organizationEntity.getId() +
							    ", name: " + organizationEntity.getName() + ")", NOT_ACCEPTABLE);
		    }
	    }

	    organization.setName(json.name);
        organization.setPname(json.pname);
        if (json.id == null) {
            organization.setThemeId(0);
        }
	    if (json.ecommerce != null) {
		    organization.setEcommerce(json.ecommerce);
	    }
	    if (json.googleToken != null) {
		    organization.setGoogleToken(json.googleToken);
	    }

	    organizationRepository.save(organization);
        return new OrganizationResponse(organization.getId(), 0);
    }


    private void validateOrganizationName(OrganizationDTO.OrganizationCreationDTO json) throws BusinessException {
        if (json.name == null) {
            throw new BusinessException("MISSING_PARAM: name","Required Organization name is empty", NOT_ACCEPTABLE);
        } else if (!validateName(json.name)) {
            throw new BusinessException("INVALID_PARAM: name", "Required Organization name is invalid", NOT_ACCEPTABLE);
        }
        if (json.pname == null) {
            throw new BusinessException("MISSING_PARAM: p_name", "Required Organization p_name is empty", NOT_ACCEPTABLE);
        } else if (!json.pname.equals(encodeUrl(json.pname))) {
            throw new BusinessException("INVALID_PARAM: p_name", "Required Organization p_name is invalid", NOT_ACCEPTABLE);
        }
    }
    
    
    @CacheEvict(allEntries = true, cacheNames = { ORGANIZATIONS_BY_NAME, ORGANIZATIONS_BY_ID})
    public OrganizationResponse updateOrganizationData(OrganizationDTO.OrganizationModificationDTO json, MultipartFile file) throws BusinessException {
        validateOrganizationUpdateData(json);

        OrganizationEntity organization = organizationRepository.findById(json.organizationId).get();
        if (json.description != null) {
            organization.setDescription(json.description);
        }
        if (json.info != null) {
            organization.setExtraInfo(new JSONObject(json.info).toString());
        }
        if (json.themeId != null) {
            organization.setThemeId(json.themeId);
        }

        //logo
        OrganizationThemeEntity orgTheme = null;
        if (file != null) {
            orgTheme = organizationThemeRepository.findOneByOrganizationEntity_Id(json.organizationId);
            if (orgTheme == null) {
                orgTheme = new OrganizationThemeEntity();
                orgTheme.setOrganizationEntity(organization);
            }
            String mimeType = file.getContentType();
            if(!mimeType.startsWith("image"))
                throw new BusinessException("INVALID PARAM:image",
                        "Invalid file type["+mimeType+"]! only MIME 'image' types are accepted!", NOT_ACCEPTABLE);

            orgTheme.setLogo(fileService.saveFile(file, json.organizationId));
        }
        SocialEntity socialEntity = helper.addSocialLinks(json, organization);

        if (socialEntity != null)
            socialRepository.save(socialEntity);

        if (orgTheme != null)
            organizationThemeRepository.save(orgTheme);

        organization = organizationRepository.save(organization);
        return new OrganizationResponse(organization.getId(), 0);
    }

    private void validateOrganizationUpdateData(OrganizationDTO.OrganizationModificationDTO json) throws BusinessException {
        if (json.organizationId == null) {
            throw new BusinessException("MISSING_PARAM: org_id", "Required org_id is missing", NOT_ACCEPTABLE);
        }
        if (!organizationRepository.existsById(json.organizationId)) {
            throw new BusinessException("INVALID_PARAM: org_id", "Provided org_id is not matching any organization", NOT_ACCEPTABLE);
        }
        if (!securityService.getCurrentUserOrganizationId().equals(json.organizationId)){
            throw new BusinessException("INSUFFICIENT_RIGHTS", "EmployeeUser is not admin of organization", NOT_ACCEPTABLE);
        }
    }


    public List<Organization_BrandRepresentationObject> getOrganizationBrands(Long orgId){
        List<Organization_BrandRepresentationObject> brands = null;
        if (orgId == null)
            return brands;
        List<BrandsEntity> brandsEntityList = brandsRepository.findByOrganizationEntity_IdAndRemoved(orgId, 0);
        brands = brandsEntityList.stream().map(brand -> (Organization_BrandRepresentationObject) brand.getRepresentation())
                 .collect(toList());
        return brands;
    }




    private OrganizationResponse modifyBrandAdditionalData(BrandsEntity entity, BrandDTO json, MultipartFile logo, MultipartFile banner) throws BusinessException {
        BrandsEntity brand = entity;

        if (json.pname != null) {
            if (!encodeUrl(json.pname).equals(json.pname)) {
                throw new BusinessException("INVALID_PARAM: p_name", "Required Organization p_name is invalid",
                        NOT_ACCEPTABLE);
            }
            brand.setPname(json.pname);
        } else if (json.name != null) {
            brand.setPname(encodeUrl(json.name));
        }

        Long orgId = securityService.getCurrentUserOrganizationId();
        if (logo != null) {
            String mimeType = logo.getContentType();
            if(!mimeType.startsWith("image"))
                throw new BusinessException("INVALID PARAM: logo",
                        "Invalid file type["+mimeType+"]! only MIME 'image' types are accepted!", NOT_ACCEPTABLE);
            brand.setLogo(fileService.saveFile(logo, orgId));
        }
        if (banner != null) {
            String mimeType = banner.getContentType();
            if(!mimeType.startsWith("image"))
                throw new BusinessException("INVALID PARAM: banner",
                        "Invalid file type["+mimeType+"]! only MIME 'image' types are accepted!", NOT_ACCEPTABLE);
            brand.setBannerImage(fileService.saveFile(banner, orgId));
        }

        brandsRepository.save(brand);
        return new OrganizationResponse(brand.getId(), 1);
    }
    
    
    
    @CacheEvict(allEntries = true, cacheNames = {BRANDS ,ORGANIZATIONS_BY_NAME, ORGANIZATIONS_BY_ID})
    public OrganizationResponse validateAndUpdateBrand(BrandDTO json, MultipartFile logo, MultipartFile banner) throws BusinessException {
        if (json.operation != null) {
            if (json.operation.equals("create"))
                return createOrganizationBrand(json, logo, banner);
            else if (json.operation.equals("update"))
                return updateOrganizationBrand(json, logo, banner);
            else
                throw new BusinessException("INVALID_PARAM: operation", "", NOT_ACCEPTABLE);
        }
        else
            throw new BusinessException("MISSING_PARAM: operation", "", NOT_ACCEPTABLE);
    }
    
    
    
    

    public OrganizationResponse createOrganizationBrand(BrandDTO json, MultipartFile logo, MultipartFile banner) throws BusinessException {
        BrandsEntity brand = new BrandsEntity();

        if (json.name == null)
            throw new BusinessException("MISSING_PARAM: name", "'name' field can't be empty", NOT_ACCEPTABLE);

        brand.setName(json.name);

        brand.setOrganizationEntity(securityService.getCurrentUserOrganization());

        return modifyBrandAdditionalData(brand, json, logo, banner);
    }

    private OrganizationResponse updateOrganizationBrand(BrandDTO json, MultipartFile logo, MultipartFile banner) throws BusinessException {
        if (json.id == null) {
            throw new BusinessException("MISSING_PARAM: brand_id", "'brand_id' property can't be empty", NOT_ACCEPTABLE);
        }
        if (!brandsRepository.existsById(json.id)) {
            throw new BusinessException("ENTITY NOT FOUND", "No Brand entity found with given id", NOT_FOUND);
        }
        BrandsEntity brand = brandsRepository.findById(json.id).get();

        if (json.name != null) {
            brand.setName(json.name);
        }

        return modifyBrandAdditionalData(brand, json, logo, banner);
    }
    
    
    
    
    

	public List<ProductFeatureDTO> getProductFeatures(Long orgId) {
		List<ProductFeaturesEntity> entities = featureRepo.findByOrganizationId(orgId);
		return entities.stream()
					.map(ProductFeatureDTO::new)
					.collect(toList());
	}
	
	
	
	

	public ProductFeatureUpdateResponse updateProductFeature(ProductFeatureUpdateDTO featureDto) throws BusinessException {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		BaseUserEntity user =  empRepo.getOneByEmail(auth.getName());
		Long orgId = user.getOrganizationId();
		
		
		validateProductFeature(featureDto, orgId);
		
		ProductFeaturesEntity entity = saveFeatureToDb(featureDto, orgId);		
		
		return new ProductFeatureUpdateResponse(entity.getId());
	}
	
	
	
	

	private ProductFeaturesEntity saveFeatureToDb(ProductFeatureUpdateDTO featureDto, Long orgId) {
		ProductFeaturesEntity entity = new ProductFeaturesEntity();
		entity.setOrganization( orgRepo.findById(orgId).get() );
		
		Operation opr = featureDto.getOperation();
		
		if( opr.equals( Operation.UPDATE)) {			
			entity = featureRepo.findById( featureDto.getFeatureId()).get();
		}
		
		
		if(featureDto.isUpdated("name")){
			entity.setName( featureDto.getName()); 
		}
		
		setPnameOrGenerateDefault(featureDto, entity, opr);
		
		if(featureDto.isUpdated("description")) {
			entity.setDescription( featureDto.getDescription() );
		}

        if(featureDto.isUpdated("level")) {
            entity.setLevel( featureDto.getLevel() );
        }
		
		entity = featureRepo.save(entity);
		
		return entity;
	}
	
	

	private void setPnameOrGenerateDefault(ProductFeatureUpdateDTO featureDto, ProductFeaturesEntity entity,
			Operation opr) {
		
		if(featureDto.isUpdated("pname") && !isBlankOrNull( featureDto.getPname()) ) {
			entity.setPname(featureDto.getPname() );
		}else if(opr.equals( Operation.CREATE )){
			String defaultPname = encodeUrl(featureDto.getName());
			entity.setPname(defaultPname);
		}
		
	}
	
	
	
	

	private void validateProductFeature(ProductFeatureUpdateDTO featureDto, Long orgId) throws BusinessException {
		if(!featureDto.areRequiredAlwaysPropertiesPresent()) {
			throw new BusinessException(
					"Missing required parameters !" 
					, "MISSING PARAM"
					, NOT_ACCEPTABLE);
		}
		
		Operation opr = featureDto.getOperation();
		
		validateOperation(opr);
		
		if(opr.equals(Operation.CREATE)) {
			validateProductFeatureForCreate(featureDto, orgId);
		}else if(opr.equals(Operation.UPDATE)) {
			validateProductFeatureForUpdate(featureDto, orgId);
		}
	}
	
	
	
	

	private void validateOperation(Operation opr) throws BusinessException {
		if(opr == null) {
			throw new BusinessException(
					"Missing required parameters [operation]!" 
					, "MISSING PARAM:operation"
					, NOT_ACCEPTABLE);
		}
		
		if(!opr.equals(Operation.CREATE) &&
				!opr.equals(Operation.UPDATE)) {
			throw new BusinessException(
					String.format("Invalid parameters [operation], unsupported operation [%s]!", opr.getValue()) 
					, "INVALID PARAM:operation"
					, NOT_ACCEPTABLE);
		}
	}
	
	
	
	

	private void validateProductFeatureForUpdate(ProductFeatureUpdateDTO featureDto, Long userOrgId) throws BusinessException {
		if(!featureDto.areRequiredForUpdatePropertiesProvided()) {
			throw new BusinessException(
					"Missing required parameters !" 
					, "MISSING PARAM"
					, NOT_ACCEPTABLE);
		}
		
		Integer id = featureDto.getFeatureId();
		Optional<ProductFeaturesEntity> featureOptional= featureRepo.findById( id );
		
		if( !featureOptional.isPresent()) {
			throw new BusinessException(
					String.format("Invalid parameters [feature_id], no feature exists with id [%d]!", id) 
					, "INVALID PARAM:feature_id"
					, NOT_ACCEPTABLE);
		}	
		
		Long featureOrgId = featureOptional.map(ProductFeaturesEntity::getOrganization)
										   .map(OrganizationEntity::getId)
										   .orElseThrow(
												   () -> new BusinessException(
															String.format("Feature of id[%d], Doesn't follow any organization!", id) 
															, "INTERNAL SERVER ERROR"
															, INTERNAL_SERVER_ERROR)
												   );
		   
		
		if(!Objects.equals(featureOrgId, userOrgId)) {
			throw new BusinessException(
					String.format("Feature of id[%d], can't be changed a user from organization with id[%d]!", featureDto.getFeatureId() , userOrgId) 
					, "INVALID PARAM:feature_id"
					, FORBIDDEN);
		}
		
		if(featureDto.isUpdated("name") &&
				isBlankOrNull(featureDto.getName())) {
			throw new BusinessException(
					 "Invalid parameters [name], the feature name can't be null nor Empty!" 
					, "INVALID PARAM:name"
					, NOT_ACCEPTABLE);
		}		
	}
	
	
	

	private void validateProductFeatureForCreate(ProductFeatureUpdateDTO featureDto, Long orgId) throws BusinessException {
		if(!featureDto.areRequiredForCreatePropertiesProvided()) {
			throw new BusinessException(
					"Missing required parameters !" 
					, "MISSING PARAM"
					, NOT_ACCEPTABLE);
		}
		
		
		if(!organizationRepository.existsById( orgId )) {
			throw new BusinessException(
					String.format("Invalid parameters [organization_id], no organization exists with id [%d]!", orgId) 
									, "INVALID PARAM:organization_id"
									, NOT_ACCEPTABLE);
		}
		
		if(isBlankOrNull(featureDto.getName())) {
			throw new BusinessException(
					 "Invalid parameters [name], the feature name can't be null nor Empty!" 
					, "INVALID PARAM:name"
					, NOT_ACCEPTABLE);
		}
	}


	@CacheEvict(allEntries = true, cacheNames = { ORGANIZATIONS_BY_NAME, ORGANIZATIONS_BY_ID})
	public ProductImageUpdateResponse updateOrganizationImage(MultipartFile file, OrganizationImageUpdateDTO imgMetaData) throws BusinessException {
        if(imgMetaData == null)
            throw new BusinessException("No Metadata provided for organization image!", "INVALID PARAM", NOT_ACCEPTABLE);

        if(!imgMetaData.isRequiredPropertyProvided("operation"))
            throw new BusinessException("No operation provided!", "MISSING PARAM:operation", NOT_ACCEPTABLE);


        ProductImageUpdateResponse response;

        if (imgMetaData.getOperation().equals( Operation.CREATE )) {
            response = createNewOrganizationImg(file, imgMetaData);
        } else {
            response = UpdatedOrganizationImg(file, imgMetaData);
        }
        return response;
    }

    private void validateOrganizationImageUpdateData(OrganizationImageUpdateDTO imgMetaData) throws BusinessException {
        Optional<OrganizationEntity> org = organizationRepository.findById(imgMetaData.getOrganizationId());
        if (!org.isPresent())
            throw new BusinessException("INVAILD PARAM: org_id","provided org_id doesn't have corresponding organization",
                    NOT_ACCEPTABLE);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        BaseUserEntity user =  empRepo.getOneByEmail(auth.getName());
        if (!user.getOrganizationId().equals(imgMetaData.getOrganizationId()))
            throw new BusinessException("INSUFFICIENT RIGHTS","User doesn't belong to organization",
                    FORBIDDEN);
    }

    private ProductImageUpdateResponse createNewOrganizationImg(MultipartFile file, OrganizationImageUpdateDTO imgMetaData) throws BusinessException {
        if(!imgMetaData.areRequiredForCreatePropertiesProvided()) {
            throw new BusinessException(
                    String.format("Missing required parameters! required parameters for adding new image are: %s", imgMetaData.getRequiredPropertiesForDataCreate())
                    , "MISSING PARAM", NOT_ACCEPTABLE);
        }

        validateOrganizationImageUpdateData(imgMetaData);

        if(file == null || file.isEmpty() || file.getContentType() == null)
            throw new BusinessException("No image file provided!", "MISSIG PARAM:image", NOT_ACCEPTABLE);

        String mimeType = file.getContentType();
        if(!mimeType.startsWith("image"))
            throw new BusinessException(String.format("Invalid file type[%]! only MIME 'image' types are accepted!", mimeType)
                    , "MISSIG PARAM:image"
                    , NOT_ACCEPTABLE);
        String url = fileService.saveFile(file, imgMetaData.getOrganizationId());

        Optional<OrganizationEntity> organizationEntity = orgRepo.findById( imgMetaData.getOrganizationId());

        OrganizationImagesEntity entity = new OrganizationImagesEntity();
        entity.setOrganizationEntity(organizationEntity.get());
        entity.setType(imgMetaData.getType());
        entity.setUri(url);
        if  (imgMetaData.getShopId() != null) {
            Optional<ShopsEntity> shop = shopsRepository.findById(imgMetaData.getShopId());
            if(!shop.isPresent())
                throw new BusinessException("INVALID PARAM: shop_id", "Provided shop_id doesn't match any existing shop", NOT_ACCEPTABLE);

            else if (!shop.get().getOrganizationEntity().getId().equals(imgMetaData.getOrganizationId()))
                throw new BusinessException("INVALID PARAM: shop_id", "Provided shop_id doesn't belong to organization #"
                        + imgMetaData.getOrganizationId(), NOT_ACCEPTABLE);

            entity.setShopsEntity(shopsRepository.findById(imgMetaData.getShopId()).get());
        }

        entity = organizationImagesRepository.save(entity);

        return new ProductImageUpdateResponse(entity.getId(), url);
    }


    private ProductImageUpdateResponse UpdatedOrganizationImg(MultipartFile file, OrganizationImageUpdateDTO imgMetaData) throws BusinessException {
        if(!imgMetaData.areRequiredForUpdatePropertiesProvided())
            throw new BusinessException(String.format("Missing required parameters! required parameters for updating existing image are: %s",
                    imgMetaData.getRequiredPropertyNamesForDataUpdate()), "MISSING PARAM", NOT_ACCEPTABLE);

        validateOrganizationImageUpdateData(imgMetaData);

        Long imgId = imgMetaData.getImageId();

        if( !organizationImagesRepository.existsById(imgId))
            throw new BusinessException(
                    String.format("No organization image exists with id: %d !", imgId), "INVALID PARAM:image_id", NOT_ACCEPTABLE);

        if(file != null) {
            String mimeType = file.getContentType();
            if (!mimeType.startsWith("image"))
                throw new BusinessException(String.format("Invalid file type[%]! only MIME 'image' types are accepted!", mimeType)
                        , "MISSING PARAM:image", NOT_ACCEPTABLE);
        }

        OrganizationImagesEntity entity = organizationImagesRepository.findById(imgId).get();

        String url = null;
        String oldUrl = null;
        if(file != null && !file.isEmpty()) {
            url = fileService.saveFile(file, imgMetaData.getOrganizationId());
            oldUrl = entity.getUri();
        }

        //to update a value , it should be already present in the JSON
        if(imgMetaData.isUpdated("type"))
            entity.setType( imgMetaData.getType() );

        if(imgMetaData.isUpdated("shopId")) {
            Long shopId = imgMetaData.getShopId();
            Optional<ShopsEntity> shopEntity = shopsRepository.findById( shopId );
            if (!shopEntity.isPresent())
                throw new BusinessException(String.format("No shop exists with id: %d !", shopId), "INVALID PARAM: shop_id", NOT_ACCEPTABLE);
            if (!shopEntity.get().getOrganizationEntity().getId().equals(imgMetaData.getOrganizationId()))
                throw new BusinessException("shop_id doesn't match current organization", "INVALID PARAM: shop_id", NOT_ACCEPTABLE);
            entity.setShopsEntity(shopEntity.get());
        }

        if(url != null)
            entity.setUri(url);

        entity = organizationImagesRepository.save(entity);

        if(url != null && oldUrl != null) {
            fileService.deleteFileByUrl(oldUrl);
        }

        return new ProductImageUpdateResponse(entity.getId(), url);
    }


    
    
    
    @CacheEvict(allEntries = true, cacheNames = { ORGANIZATIONS_BY_NAME, ORGANIZATIONS_BY_ID})
    public boolean deleteImage(Long imgId) throws BusinessException {
        OrganizationImagesEntity img = 
        		organizationImagesRepository
        		.findById(imgId)
                .orElseThrow(()-> new BusinessException(
                					"No Image exists with id ["+ imgId+"] !"
                					,"INVALID PARAM:image_id"
                					, NOT_ACCEPTABLE));
        validateImgToDelete(img);

        organizationImagesRepository.deleteById(imgId);

        fileService.deleteFileByUrl(img.getUri());

        return true;
    }
    
    
    

    private void validateImgToDelete(OrganizationImagesEntity img) throws BusinessException {
        Long orgId = img.getOrganizationEntity().getId();

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        BaseUserEntity user =  empRepo.getOneByEmail(auth.getName());

        if(!user.getOrganizationId().equals(orgId)) {
            throw new BusinessException(
                    String.format("User from organization of id[%d] have no rights to delete product image of id[%d]",orgId, img.getId())
                    , "UNAUTHRORIZED", FORBIDDEN);
        }
    }

    
    
    
    @CacheResult(cacheName = ORGANIZATIONS_DOMAINS)
    public Pair getOrganizationAndSubdirsByUrl(String urlString) throws BusinessException {
        URIBuilder url = null;

        try {
            urlString = urlString.startsWith("http") ? urlString: "http://"+urlString;
            url = new URIBuilder(urlString);
        } catch (URISyntaxException e) {
            throw new BusinessException("the provided url is mailformed","INVALID_PARAM: url", NOT_ACCEPTABLE);
        }

        String domain = ofNullable(url.getHost()).orElse("");
	    domain = domain.startsWith("www.") ? domain.substring(4) : domain; //getting domain

	    String subDir = null;
	    if (url.getPath() != null && url.getPath().length() > 1) {
		    String[] subdirectories = url.getPath().split("/");
		    if (subdirectories.length > 1 && subdirectories[1].length() > 0) {
		    	subDir = subdirectories[1];
		    }
	    }
	    
	    OrganizationDomainsEntity orgDom = null;
	    if(domain.endsWith(NASNAV_DOMAIN) || domain.endsWith(NASORG_DOMAIN)) {
	    	orgDom = orgDomainsRep.findByDomainAndSubdir(domain,subDir);
	    }else {
	    	orgDom = orgDomainsRep.findByDomain(domain);
	    }    	

		return (orgDom == null) ? new Pair(0L, 0L) : new Pair(orgDom.getOrganizationEntity().getId(), subDir == null ? 0L : 1L);
    }


    public void deleteExtraAttribute(Integer attrId) {
        Long orgId = securityService.getCurrentUserOrganizationId();
        if (!extraAttributesRepository.existsByIdAndOrganizationId(attrId, orgId))
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, ORG$EXTRATTR$0001, attrId);

        productExtraAttrRepo.deleteByIdAndOrganizationId(attrId, orgId);

        extraAttributesRepository.deleteByIdAndOrganizationId(attrId, orgId);
    }



    
    
	public List<ExtraAttributeDefinitionDTO> getExtraAttributes() {
		Long orgId = securityService.getCurrentUserOrganizationId();
		return extraAttrRepo
				.findByOrganizationId(orgId)
				.stream()
				.map(this::createExtraAttributeDTO)
				.collect(toList());
	}
	
	
	
	
	
	private ExtraAttributeDefinitionDTO createExtraAttributeDTO(ExtraAttributesEntity entity) {
		ExtraAttributeDefinitionDTO dto = new ExtraAttributeDTO();
		dto.setIconUrl(entity.getIconUrl());
		dto.setId(entity.getId());
		dto.setName(entity.getName());
		dto.setType(entity.getType());
		return dto;
	}

}
