package com.nasnav.service;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.nasnav.commons.utils.StringUtils;
import com.nasnav.constatnts.EntityConstants.Operation;
import com.nasnav.dao.BrandsRepository;
import com.nasnav.dao.EmployeeUserRepository;
import com.nasnav.dao.ExtraAttributesRepository;
import com.nasnav.dao.OrganizationImagesRepository;
import com.nasnav.dao.OrganizationRepository;
import com.nasnav.dao.OrganizationThemeRepository;
import com.nasnav.dao.ProductFeaturesRepository;
import com.nasnav.dao.ShopsRepository;
import com.nasnav.dao.SocialRepository;
import com.nasnav.dto.BrandDTO;
import com.nasnav.dto.ExtraAttributesRepresentationObject;
import com.nasnav.dto.OrganizationDTO;
import com.nasnav.dto.OrganizationImageUpdateDTO;
import com.nasnav.dto.OrganizationImagesRepresentationObject;
import com.nasnav.dto.OrganizationRepresentationObject;
import com.nasnav.dto.OrganizationThemesRepresentationObject;
import com.nasnav.dto.Organization_BrandRepresentationObject;
import com.nasnav.dto.ProductFeatureDTO;
import com.nasnav.dto.ProductFeatureUpdateDTO;
import com.nasnav.dto.SocialRepresentationObject;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.persistence.BaseUserEntity;
import com.nasnav.persistence.BrandsEntity;
import com.nasnav.persistence.ExtraAttributesEntity;
import com.nasnav.persistence.OrganizationEntity;
import com.nasnav.persistence.OrganizationImagesEntity;
import com.nasnav.persistence.OrganizationThemeEntity;
import com.nasnav.persistence.ProductFeaturesEntity;
import com.nasnav.persistence.ShopsEntity;
import com.nasnav.persistence.SocialEntity;
import com.nasnav.response.OrganizationResponse;
import com.nasnav.response.ProductFeatureUpdateResponse;
import com.nasnav.response.ProductImageUpdateResponse;
import com.nasnav.service.helpers.OrganizationServiceHelper;


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

    private final OrganizationImagesRepository organizationImagesRepository;

    @Autowired
    private ShopsRepository shopsRepository;

    @Autowired
	private ProductFeaturesRepository featureRepo;
    
    
    @Autowired
    private EmployeeUserRepository empRepo;
    
    
    @Autowired 
    private OrganizationRepository orgRepo;
    
    @Autowired
    private SecurityService securityService;

    @Autowired
    public OrganizationService(OrganizationRepository organizationRepository, BrandsRepository brandsRepository, SocialRepository socialRepository,
                               OrganizationThemeRepository organizationThemeRepository,ExtraAttributesRepository extraAttributesRepository,
                               OrganizationServiceHelper helper, FileService fileService, EmployeeUserRepository employeeUserRepository,
                               OrganizationImagesRepository organizationImagesRepository) {
        this.organizationRepository = organizationRepository;
        this.socialRepository = socialRepository;
        this.organizationThemeRepository = organizationThemeRepository;
        this.brandsRepository = brandsRepository;
        this.extraAttributesRepository = extraAttributesRepository;
        this.helper = helper;
        this.fileService = fileService;
        this.employeeUserRepository = employeeUserRepository;
        this.organizationImagesRepository = organizationImagesRepository;
    }

    public OrganizationRepresentationObject getOrganizationByName(String organizationName) throws BusinessException {
        OrganizationEntity organizationEntity = organizationRepository.findByPname(organizationName);
        if (organizationEntity == null)
            organizationEntity = organizationRepository.findOneByNameIgnoreCase(organizationName);

        if (organizationEntity == null)
            throw new BusinessException("Organization not found", null, HttpStatus.NOT_FOUND);

        return getOrganizationAdditionalData(organizationEntity);
    }

    public OrganizationRepresentationObject getOrganizationById(Long organizationId) throws BusinessException {
        OrganizationEntity organizationEntity = organizationRepository.findOneById(organizationId);

        if (organizationEntity == null)
            throw new BusinessException("Organization not found", null, HttpStatus.NOT_FOUND);

        return getOrganizationAdditionalData(organizationEntity);
    }

    private OrganizationRepresentationObject getOrganizationAdditionalData(OrganizationEntity entity) {
        OrganizationRepresentationObject orgRepObj = ((OrganizationRepresentationObject) entity.getRepresentation());

        //TODO add brandRepresentationObjects from other repository
        SocialEntity socialEntity = socialRepository.findOneByOrganizationEntity_Id(orgRepObj.getId());
        if (socialEntity != null)
            orgRepObj.setSocial((SocialRepresentationObject) socialEntity.getRepresentation());

        OrganizationThemeEntity organizationThemeEntity = organizationThemeRepository.findOneByOrganizationEntity_Id(orgRepObj.getId());
        if (organizationThemeEntity != null)
            orgRepObj.setThemes((OrganizationThemesRepresentationObject) organizationThemeEntity.getRepresentation());

        List<BrandsEntity> brandsEntityList = brandsRepository.findByOrganizationEntity_Id(orgRepObj.getId());
        if (brandsEntityList != null && !brandsEntityList.isEmpty()) {
            List<Organization_BrandRepresentationObject> repList = brandsEntityList.stream().map(rep -> ((Organization_BrandRepresentationObject) rep.getRepresentation())).collect(Collectors.toList());
            orgRepObj.setBrands(repList);
        }

        List <OrganizationImagesEntity> orgImgEntities = organizationImagesRepository.findByOrganizationEntityId(orgRepObj.getId());
        if (orgImgEntities != null && !orgImgEntities.isEmpty()) {
            List<OrganizationImagesRepresentationObject> imagesList = orgImgEntities.stream().map(rep -> ((OrganizationImagesRepresentationObject) rep.getRepresentation())).collect(Collectors.toList());
            orgRepObj.setImages(imagesList);
        }

        return orgRepObj;
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
        newOrg.setThemeId(0);
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
                        "Invalid file type["+mimeType+"]! only MIME 'image' types are accepted!", HttpStatus.NOT_ACCEPTABLE);

            orgTheme.setLogo(fileService.saveFile(file, json.organizationId));
        }
        SocialEntity socialEntity = helper.addSocialLinks(json, organization);

        if (socialEntity != null)
            socialRepository.save(socialEntity);

        if (orgTheme != null)
            organizationThemeRepository.save(orgTheme);

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

    public OrganizationResponse createOrganizationBrand(BrandDTO json, MultipartFile logo,
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
            brand.setPname(json.pname);
        } else {
            brand.setPname(StringUtils.encodeUrl(json.name));
        }
        
        Long orgId = securityService.getCurrentUserOrganizationId();
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

    public OrganizationResponse updateOrganizationBrand(BrandDTO json, MultipartFile logo,
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
        
        Long orgId = securityService.getCurrentUserOrganizationId();
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
    
    
    
    
    

	public List<ProductFeatureDTO> getProductFeatures(Long orgId) {
		List<ProductFeaturesEntity> entities = featureRepo.findByOrganizationId(orgId);
		return entities.stream()
					.map(ProductFeatureDTO::new)
					.collect(Collectors.toList());
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
		
		entity = featureRepo.save(entity);
		
		return entity;
	}
	
	

	private void setPnameOrGenerateDefault(ProductFeatureUpdateDTO featureDto, ProductFeaturesEntity entity,
			Operation opr) {
		
		if(featureDto.isUpdated("pname") && !StringUtils.isBlankOrNull( featureDto.getPname()) ) {
			entity.setPname(featureDto.getPname() );
		}else if(opr.equals( Operation.CREATE )){
			String defaultPname = StringUtils.encodeUrl(featureDto.getName());
			entity.setPname(defaultPname);
		}
		
	}
	
	
	
	

	private void validateProductFeature(ProductFeatureUpdateDTO featureDto, Long orgId) throws BusinessException {
		if(!featureDto.areRequiredAlwaysPropertiesPresent()) {
			throw new BusinessException(
					"Missing required parameters !" 
					, "MISSING PARAM"
					, HttpStatus.NOT_ACCEPTABLE);
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
					, HttpStatus.NOT_ACCEPTABLE);
		}
		
		if(!opr.equals(Operation.CREATE) &&
				!opr.equals(Operation.UPDATE)) {
			throw new BusinessException(
					String.format("Invalid parameters [operation], unsupported operation [%s]!", opr.getValue()) 
					, "INVALID PARAM:operation"
					, HttpStatus.NOT_ACCEPTABLE);
		}
	}
	
	
	
	

	private void validateProductFeatureForUpdate(ProductFeatureUpdateDTO featureDto, Long userOrgId) throws BusinessException {
		if(!featureDto.areRequiredForUpdatePropertiesProvided()) {
			throw new BusinessException(
					"Missing required parameters !" 
					, "MISSING PARAM"
					, HttpStatus.NOT_ACCEPTABLE);
		}
		
		Integer id = featureDto.getFeatureId();
		Optional<ProductFeaturesEntity> featureOptional= featureRepo.findById( id );
		
		if( !featureOptional.isPresent()) {
			throw new BusinessException(
					String.format("Invalid parameters [feature_id], no feature exists with id [%d]!", id) 
					, "INVALID PARAM:feature_id"
					, HttpStatus.NOT_ACCEPTABLE);
		}	
		
		Long featureOrgId = featureOptional.map(ProductFeaturesEntity::getOrganization)
										   .map(OrganizationEntity::getId)
										   .orElseThrow(
												   () -> new BusinessException(
															String.format("Feature of id[%d], Doesn't follow any organization!", id) 
															, "INTERNAL SERVER ERROR"
															, HttpStatus.INTERNAL_SERVER_ERROR)
												   );
		   
		
		if(!Objects.equals(featureOrgId, userOrgId)) {
			throw new BusinessException(
					String.format("Feature of id[%d], can't be changed a user from organization with id[%d]!", featureDto.getFeatureId() , userOrgId) 
					, "INVALID PARAM:feature_id"
					, HttpStatus.FORBIDDEN);
		}
		
		if(featureDto.isUpdated("name") &&
				StringUtils.isBlankOrNull(featureDto.getName())) {
			throw new BusinessException(
					 "Invalid parameters [name], the feature name can't be null nor Empty!" 
					, "INVALID PARAM:name"
					, HttpStatus.NOT_ACCEPTABLE);
		}		
	}
	
	
	

	private void validateProductFeatureForCreate(ProductFeatureUpdateDTO featureDto, Long orgId) throws BusinessException {
		if(!featureDto.areRequiredForCreatePropertiesProvided()) {
			throw new BusinessException(
					"Missing required parameters !" 
					, "MISSING PARAM"
					, HttpStatus.NOT_ACCEPTABLE);
		}
		
		
		if(!organizationRepository.existsById( orgId )) {
			throw new BusinessException(
					String.format("Invalid parameters [organization_id], no organization exists with id [%d]!", orgId) 
									, "INVALID PARAM:organization_id"
									, HttpStatus.NOT_ACCEPTABLE);
		}
		
		if(StringUtils.isBlankOrNull(featureDto.getName())) {
			throw new BusinessException(
					 "Invalid parameters [name], the feature name can't be null nor Empty!" 
					, "INVALID PARAM:name"
					, HttpStatus.NOT_ACCEPTABLE);
		}
	}



	public ProductImageUpdateResponse updateOrganizationImage(MultipartFile file, OrganizationImageUpdateDTO imgMetaData) throws BusinessException {
        if(imgMetaData == null)
            throw new BusinessException("No Metadata provided for organization image!", "INVALID PARAM", HttpStatus.NOT_ACCEPTABLE);

        if(!imgMetaData.isRequiredPropertyProvided("operation"))
            throw new BusinessException("No operation provided!", "MISSING PARAM:operation", HttpStatus.NOT_ACCEPTABLE);


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
                    HttpStatus.NOT_ACCEPTABLE);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        BaseUserEntity user =  empRepo.getOneByEmail(auth.getName());
        if (!user.getOrganizationId().equals(imgMetaData.getOrganizationId()))
            throw new BusinessException("INSUFFICIENT RIGHTS","User doesn't belong to organization",
                    HttpStatus.FORBIDDEN);
    }

    private ProductImageUpdateResponse createNewOrganizationImg(MultipartFile file, OrganizationImageUpdateDTO imgMetaData) throws BusinessException {
        if(!imgMetaData.areRequiredForCreatePropertiesProvided()) {
            throw new BusinessException(
                    String.format("Missing required parameters! required parameters for adding new image are: %s", imgMetaData.getRequiredPropertiesForDataCreate())
                    , "MISSING PARAM", HttpStatus.NOT_ACCEPTABLE);
        }

        validateOrganizationImageUpdateData(imgMetaData);

        if(file == null || file.isEmpty() || file.getContentType() == null)
            throw new BusinessException("No image file provided!", "MISSIG PARAM:image", HttpStatus.NOT_ACCEPTABLE);

        String mimeType = file.getContentType();
        if(!mimeType.startsWith("image"))
            throw new BusinessException(String.format("Invalid file type[%]! only MIME 'image' types are accepted!", mimeType)
                    , "MISSIG PARAM:image"
                    , HttpStatus.NOT_ACCEPTABLE);
        String url = fileService.saveFile(file, imgMetaData.getOrganizationId());

        Optional<OrganizationEntity> organizationEntity = orgRepo.findById( imgMetaData.getOrganizationId());

        OrganizationImagesEntity entity = new OrganizationImagesEntity();
        entity.setOrganizationEntity(organizationEntity.get());
        entity.setType(imgMetaData.getType());
        entity.setUri(url);
        if  (imgMetaData.getShopId() != null) {
            Optional<ShopsEntity> shop = shopsRepository.findById(imgMetaData.getShopId());
            if(!shop.isPresent())
                throw new BusinessException("INVALID PARAM: shop_id", "Provided shop_id doesn't match any existing shop", HttpStatus.NOT_ACCEPTABLE);

            else if (!shop.get().getOrganizationEntity().getId().equals(imgMetaData.getOrganizationId()))
                throw new BusinessException("INVALID PARAM: shop_id", "Provided shop_id doesn't belong to organization #"
                        + imgMetaData.getOrganizationId(), HttpStatus.NOT_ACCEPTABLE);

            entity.setShopsEntity(shopsRepository.findById(imgMetaData.getShopId()).get());
        }

        entity = organizationImagesRepository.save(entity);

        return new ProductImageUpdateResponse(entity.getId(), url);
    }


    private ProductImageUpdateResponse UpdatedOrganizationImg(MultipartFile file, OrganizationImageUpdateDTO imgMetaData) throws BusinessException {
        if(!imgMetaData.areRequiredForUpdatePropertiesProvided())
            throw new BusinessException(String.format("Missing required parameters! required parameters for updating existing image are: %s",
                    imgMetaData.getRequiredPropertyNamesForDataUpdate()), "MISSING PARAM", HttpStatus.NOT_ACCEPTABLE);

        validateOrganizationImageUpdateData(imgMetaData);

        Long imgId = imgMetaData.getImageId();

        if( !organizationImagesRepository.existsById(imgId))
            throw new BusinessException(
                    String.format("No organization image exists with id: %d !", imgId), "INVALID PARAM:image_id", HttpStatus.NOT_ACCEPTABLE);

        if(file != null) {
            String mimeType = file.getContentType();
            if (!mimeType.startsWith("image"))
                throw new BusinessException(String.format("Invalid file type[%]! only MIME 'image' types are accepted!", mimeType)
                        , "MISSING PARAM:image", HttpStatus.NOT_ACCEPTABLE);
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
                throw new BusinessException(String.format("No shop exists with id: %d !", shopId), "INVALID PARAM: shop_id", HttpStatus.NOT_ACCEPTABLE);
            if (!shopEntity.get().getOrganizationEntity().getId().equals(imgMetaData.getOrganizationId()))
                throw new BusinessException("shop_id doesn't match current organization", "INVALID PARAM: shop_id", HttpStatus.NOT_ACCEPTABLE);
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


    public boolean deleteImage(Long imgId) throws BusinessException {
        OrganizationImagesEntity img = organizationImagesRepository.findById(imgId)
                        .orElseThrow(()-> new BusinessException("No Image exists with id ["+ imgId+"] !",
                                "INVALID PARAM:image_id", HttpStatus.NOT_ACCEPTABLE));

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
                    , "UNAUTHRORIZED", HttpStatus.FORBIDDEN);
        }
    }
}