package com.nasnav.service;

import com.nasnav.AppConfig;
import com.nasnav.constatnts.EntityConstants.Operation;
import com.nasnav.dao.*;
import com.nasnav.dto.*;
import com.nasnav.dto.request.organization.OrganizationCreationDTO;
import com.nasnav.dto.request.organization.OrganizationModificationDTO;
import com.nasnav.dto.request.organization.SettingDTO;
import com.nasnav.dto.response.OrgThemeRepObj;
import com.nasnav.dto.response.YeshteryOrganizationDTO;
import com.nasnav.enumerations.ExtraAttributeType;
import com.nasnav.enumerations.ProductFeatureType;
import com.nasnav.enumerations.Settings;
import com.nasnav.enumerations.SettingsType;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.payments.mastercard.MastercardAccount;
import com.nasnav.payments.misc.Tools;
import com.nasnav.payments.rave.RaveAccount;
import com.nasnav.payments.upg.UpgAccount;
import com.nasnav.persistence.*;
import com.nasnav.request.SitemapParams;
import com.nasnav.response.OrganizationResponse;
import com.nasnav.response.ProductFeatureUpdateResponse;
import com.nasnav.response.ProductImageUpdateResponse;
import com.nasnav.service.helpers.OrganizationServiceHelper;
import com.nasnav.service.model.IdAndNamePair;
import org.apache.http.client.utils.URIBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.cache.annotation.CacheResult;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.*;

import static com.nasnav.cache.Caches.*;
import static com.nasnav.commons.utils.CollectionUtils.setOf;
import static com.nasnav.commons.utils.EntityUtils.anyIsNull;
import static com.nasnav.commons.utils.EntityUtils.isNullOrEmpty;
import static com.nasnav.commons.utils.StringUtils.*;
import static com.nasnav.constatnts.EntityConstants.NASNAV_DOMAIN;
import static com.nasnav.constatnts.EntityConstants.NASORG_DOMAIN;
import static com.nasnav.enumerations.ExtraAttributeType.INVISIBLE;
import static com.nasnav.enumerations.ExtraAttributeType.getExtraAttributeType;
import static com.nasnav.enumerations.ProductFeatureType.*;
import static com.nasnav.enumerations.SettingsType.PRIVATE;
import static com.nasnav.enumerations.SettingsType.PUBLIC;
import static com.nasnav.exceptions.ErrorCodes.*;
import static com.nasnav.payments.cod.CodCommons.isCodAvailableForService;
import static com.nasnav.payments.misc.Gateway.*;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyMap;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.HttpStatus.*;


@Service
public class OrganizationServiceImpl implements OrganizationService {
    public static final String EXTRA_ATTRIBUTE_ID = "extra_attribute_id";
    public static final Set<Integer> FEATURE_TYPE_WITH_EXTRA_DATA = setOf(IMG_SWATCH.getValue(), COLOR.getValue());
    @Autowired
    private OrganizationRepository organizationRepository;
    @Autowired
    private CountryRepository countryRepo;
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
    private ProductRepository productRepo;
    @Autowired
    private UserSubscriptionRepository subsRepo;
    @Autowired
    private TagsRepository tagsRepo;
    @Autowired
    private TagGraphNodeRepository tagGraphNodeRepo;
    @Autowired
    private ExtraAttributesRepository extraAttrRepo;
    @Autowired
    private SettingRepository settingRepo;
    @Autowired
    private OrganizationImagesRepository orgImagesRepo;
    @Autowired
    private VariantFeatureValuesRepository variantFeatureValuesRepo;
    @Autowired
    private OrganizationPaymentGatewaysRepository orgPaymentGatewaysRep;
    @Autowired UserTokenRepository userTokenRepo;
    @Autowired
    private ShopService shopService;
    @Autowired
    private DomainService domainService;
    @Autowired
    private AppConfig config;
    @Autowired
    private ProductService productService;

    private final Logger classLogger = LogManager.getLogger(OrganizationServiceImpl.class);

    @Override
    public List<OrganizationRepresentationObject> listOrganizations() {
        return organizationRepository.findAll()
                            .stream()
                            .map(org -> (OrganizationRepresentationObject) org.getRepresentation())
                            .collect(toList());
    }


    
    @Override
    @CacheResult(cacheName = ORGANIZATIONS_BY_NAME)
    public OrganizationRepresentationObject getOrganizationByName(String organizationName, Integer yeshteryState) throws BusinessException {

        OrganizationEntity organizationEntity;
        if (yeshteryState == 1) {
            organizationEntity = organizationRepository.findByPnameAndYeshteryState(organizationName, yeshteryState);
        } else {
            organizationEntity = organizationRepository.findByPname(organizationName);
        }
        if (organizationEntity == null)
            organizationEntity = organizationRepository.findOneByNameIgnoreCase(organizationName);

        if (organizationEntity == null)
            throw new BusinessException("Organization not found", "", NOT_FOUND);

        return getOrganizationAdditionalData(organizationEntity);
    }
    
    
    
    @Override
    @CacheResult(cacheName = ORGANIZATIONS_BY_ID)
    public OrganizationRepresentationObject getOrganizationById(Long organizationId, Integer yeshteryState) {
        OrganizationEntity organizationEntity;
        if (yeshteryState == 1) {
            organizationEntity = organizationRepository.findByIdAndYeshteryState(organizationId, yeshteryState)
                    .orElseThrow(() -> new RuntimeBusinessException(NOT_FOUND, G$ORG$0001, organizationId));
        } else {
            organizationEntity = organizationRepository.findById(organizationId)
                    .orElseThrow(() -> new RuntimeBusinessException(NOT_FOUND, G$ORG$0001, organizationId));
        }

        return getOrganizationAdditionalData(organizationEntity);
    }

    
    
    
    
    private OrganizationRepresentationObject getOrganizationAdditionalData(OrganizationEntity entity) {
        OrganizationRepresentationObject orgRepObj = ((OrganizationRepresentationObject) entity.getRepresentation());

        setSocialEntity(orgRepObj);
        setThemeSettings(orgRepObj);
        setImages(orgRepObj);
        setPublicSettings(orgRepObj);
        setDomain(orgRepObj);
        orgRepObj.setTheme(getOrganizationThemeDTO(orgRepObj));

        return orgRepObj;
    }

    private void setDomain(OrganizationRepresentationObject obj) {
        String domain = domainService.getOrganizationDomainAndSubDir(obj.getId());
        obj.setDomain(domain);
    }

    private void setSocialEntity(OrganizationRepresentationObject orgRepObj) {
        socialRepository
                .findOneByOrganizationEntity_Id(orgRepObj.getId())
                .map(SocialEntity::getRepresentation)
                .map(SocialRepresentationObject.class::cast)
                .ifPresent(orgRepObj::setSocial);
    }



    private void setThemeSettings(OrganizationRepresentationObject orgRepObj) {
        organizationThemeRepository
                .findOneByOrganizationEntity_Id(orgRepObj.getId())
                .map(OrganizationThemeEntity::getRepresentation)
                .map(OrganizationThemesRepresentationObject.class::cast)
                .ifPresent(orgRepObj::setThemes);
    }



    private void setBrands(OrganizationRepresentationObject orgRepObj) {
        List<BrandsEntity> brandsEntityList = brandsRepository.findByOrganizationEntity_IdAndRemovedOrderByPriorityDesc(orgRepObj.getId(), 0);
        if (!isNullOrEmpty(brandsEntityList)) {
            List<Organization_BrandRepresentationObject> repList = brandsEntityList
                    .stream()
                    .map(rep -> ((Organization_BrandRepresentationObject) rep.getRepresentation()))
                    .collect(toList());
            orgRepObj.setBrands(repList);
        }
    }



    private void setImages(OrganizationRepresentationObject orgRepObj) {
            orgRepObj.setImages(getOrganizationImages(orgRepObj.getId()));
    }

    private List<OrganizationImagesRepresentationObject> getOrganizationImages(Long orgId) {
        List <OrganizationImagesEntity> orgImgEntities =
                organizationImagesRepository.findByOrganizationEntityIdAndShopsEntityNullAndTypeNotIn(orgId, asList(360, 400, 410));
        if (!isNullOrEmpty(orgImgEntities)) {
            return orgImgEntities
                    .stream()
                    .map(rep -> ((OrganizationImagesRepresentationObject) rep.getRepresentation()))
                    .collect(toList());
        }
        return null;
    }


    private void setPublicSettings(OrganizationRepresentationObject orgRepObj) {
        List<SettingEntity> orgSettingsEntities = settingRepo.findByOrganization_IdAndType(orgRepObj.getId(), PUBLIC.getValue());
        if (!isNullOrEmpty(orgSettingsEntities)) {
           Map<String,String> settingsMap =
                   orgSettingsEntities
                    .stream()
                    .map(s -> (SettingDTO)s.getRepresentation())
                    .collect( toMap(SettingDTO::getName, SettingDTO::getValue));
            orgRepObj.setSettings(settingsMap);
        }
    }



    private OrgThemeRepObj getOrganizationThemeDTO(OrganizationRepresentationObject orgRepObj) {
        if (orgRepObj.getThemeId() == null)
            return null;

        OrgThemeRepObj themeRepObj = new OrgThemeRepObj();

        Optional<ThemeEntity> optionalThemeEntity = themesRepo.findByUid(orgRepObj.getThemeId());

        if (optionalThemeEntity.isPresent()) {
            ThemeEntity themeEntity = optionalThemeEntity.get();
            ThemeDTO themeDTO = (ThemeDTO) themeEntity.getRepresentation();
            BeanUtils.copyProperties(themeDTO, themeRepObj);
            themeRepObj.setId(themeDTO.getThemeClassId());
            themeRepObj.setDefaultSettings(new JSONObject(themeDTO.getDefaultSettings()).toMap());

            Optional<OrganizationThemesSettingsEntity> optionalThemeSettings =
                    orgThemesSettingsRepo.findByOrganizationEntity_IdAndThemeId(orgRepObj.getId(), optionalThemeEntity.get().getId());

            if (optionalThemeSettings.isPresent()) {
                OrganizationThemesSettingsEntity themesSettings = optionalThemeSettings.get();
                themeRepObj.setSettings(new JSONObject(themesSettings.getSettings()).toMap());
            }
        }

        return themeRepObj;
    }




    @Override
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
    
    
    
    
    
    @Override
    @CacheEvict(allEntries = true, cacheNames = { ORGANIZATIONS_BY_NAME, ORGANIZATIONS_BY_ID})
    public OrganizationResponse createOrganization(OrganizationCreationDTO json) throws BusinessException {
        OrganizationEntity organization;
        if (json.getId() != null) {
            organization = orgRepo.findOneById(json.getId());
            if (organization == null)
                throw new BusinessException(format("Provided id (%d) doesn't match any existing org!", json.getId()),
                        "INVALID_PARAM: id", NOT_ACCEPTABLE);
            if (json.getName() != null) {
                validateOrganizationName(json);
                organization.setName(json.getName());

            }
            if (json.getPname() != null) {
                validateOrganizationPname(json);
                organization.setPname(json.getPname());
            }
        } else {
            organization = createNewOrganization(json);
        }

        updateAdditionalOrganizationData(json, organization);

	    organizationRepository.save(organization);
        return new OrganizationResponse(organization.getId(), 0);
    }


    private OrganizationEntity createNewOrganization(OrganizationCreationDTO json) throws BusinessException {
        validateOrganizationNameForCreate(json);
        OrganizationEntity organization = organizationRepository.findByPname(json.getPname());
        if (organization != null) {
            throw new BusinessException("INVALID_PARAM: p_name",
                    "Provided p_name is already used by another organization (id: " + organization.getId() +
                            ", name: " + organization.getName() + ")", NOT_ACCEPTABLE);
        }
        organization = new OrganizationEntity();
        organization.setName(json.getName());
        organization.setPname(json.getPname());
        return organization;
    }


    private OrganizationEntity updateAdditionalOrganizationData(OrganizationCreationDTO json, OrganizationEntity organization) {
        if (json.getId() == null) {
            organization.setThemeId(0);
        }
        if (json.getEcommerce() != null) {
            organization.setEcommerce(json.getEcommerce());
        }
        if (json.getGoogleToken() != null) {
            organization.setGoogleToken(json.getGoogleToken());
        }
        if (json.getCurrencyIso() != null) {
            CountriesEntity country = countryRepo.findByIsoCode(json.getCurrencyIso());
            organization.setCountry(country);
        }
        if(nonNull(json.getYeshteryState())){
            organization.setYeshteryState(json.getYeshteryState().getValue());
        }
        return organization;
    }

    private void validateOrganizationNameForCreate(OrganizationCreationDTO json) throws BusinessException {
        if (json.getName() == null) {
            throw new BusinessException("MISSING_PARAM: name", "Required Organization name is empty", NOT_ACCEPTABLE);
        }
        validateOrganizationName(json);

        if (json.getPname() == null) {
            throw new BusinessException("MISSING_PARAM: p_name", "Required Organization p_name is empty", NOT_ACCEPTABLE);
        }
        validateOrganizationPname(json);
    }

    private void validateOrganizationName(OrganizationCreationDTO json) throws BusinessException {
        if (!validateName(json.getName()))
            throw new BusinessException("INVALID_PARAM: name", "Required Organization name is invalid", NOT_ACCEPTABLE);
    }

    private void validateOrganizationPname(OrganizationCreationDTO json) throws BusinessException {
        if (!json.getPname().equals(encodeUrl(json.getPname())))
            throw new BusinessException("INVALID_PARAM: p_name", "Required Organization p_name is invalid", NOT_ACCEPTABLE);
    }
    
    
    @Override
    @CacheEvict(allEntries = true, cacheNames = { ORGANIZATIONS_BY_NAME, ORGANIZATIONS_BY_ID, COUNTRIES})
    @Transactional
    public OrganizationResponse updateOrganizationData(OrganizationModificationDTO json, MultipartFile file) throws BusinessException {
        OrganizationEntity organization = securityService.getCurrentUserOrganization();
        if (json.getDescription() != null) {
            organization.setDescription(json.getDescription());
        }
        if (json.getInfo() != null) {
            organization.setExtraInfo(new JSONObject(json.getInfo()).toString());
        }
        if (json.getThemeId() != null) {
            organization.setThemeId(json.getThemeId());
        }

        if (file != null) {
            OrganizationThemeEntity orgTheme =
                    organizationThemeRepository
                    .findOneByOrganizationEntity_Id(organization.getId())
                    .orElseGet(OrganizationThemeEntity::new);

            orgTheme.setOrganizationEntity(organization);
            String mimeType = file.getContentType();
            if(!mimeType.startsWith("image"))
                throw new BusinessException("INVALID PARAM:image",
                        "Invalid file type["+mimeType+"]! only MIME 'image' types are accepted!", NOT_ACCEPTABLE);

            orgTheme.setLogo(fileService.saveFile(file, organization.getId()));
            organizationThemeRepository.save(orgTheme);
        }

        helper
        .createSocialEntity(json, organization)
        .ifPresent(socialRepository::save);

        organization = organizationRepository.save(organization);

        return new OrganizationResponse(organization.getId(), 0);
    }


    private OrganizationResponse modifyBrandAdditionalData(BrandsEntity entity, BrandDTO json, MultipartFile logo,
                                                           MultipartFile banner, MultipartFile cover) {
        BrandsEntity brand = entity;

        if (json.pname != null) {
            if (!encodeUrl(json.pname).equals(json.pname)) {
                throw new RuntimeBusinessException(NOT_ACCEPTABLE, G$PRAM$0002, "name");
            }
            brand.setPname(json.pname);
        } else if (json.name != null) {
            brand.setPname(encodeUrl(json.name));
        }

        if (json.priority != null) {
            entity.setPriority(json.getPriority());
        }

        Long orgId = securityService.getCurrentUserOrganizationId();
        if (logo != null) {
            validateImageMimetype(logo);
            brand.setLogo(fileService.saveFile(logo, orgId));
        }
        if (banner != null) {
            validateImageMimetype(banner);
            brand.setBannerImage(fileService.saveFile(banner, orgId));
        }

        if (cover != null) {
            validateImageMimetype(cover);
            brand.setCoverUrl(fileService.saveFile(cover, orgId));
        }

        brandsRepository.save(brand);
        return new OrganizationResponse(brand.getId(), 1);
    }


    private void validateImageMimetype(MultipartFile image) {
        String mimeType = image.getContentType();
        if(!mimeType.startsWith("image"))
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, GEN$0018, mimeType);
    }
    
    
    @Override
    @CacheEvict(allEntries = true, cacheNames = {BRANDS ,ORGANIZATIONS_BY_NAME, ORGANIZATIONS_BY_ID})
    public OrganizationResponse validateAndUpdateBrand(BrandDTO json, MultipartFile logo, MultipartFile banner, MultipartFile cover) {
        if (isBlankOrNull(json.operation) || !setOf("create", "update").contains(json.operation)) {
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, P$PRO$0007);
        }
        if (json.operation.equals("create")) {
            return createOrganizationBrand(json, logo, banner, cover);
        }
        return updateOrganizationBrand(json, logo, banner, cover);
    }
    
    
    
    

    @Override
    public OrganizationResponse createOrganizationBrand(BrandDTO json, MultipartFile logo, MultipartFile banner, MultipartFile cover) {
        BrandsEntity brand = new BrandsEntity();

        if (json.name == null)
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, G$PRAM$0002, "name");

        brand.setName(json.name);

        brand.setOrganizationEntity(securityService.getCurrentUserOrganization());

        return modifyBrandAdditionalData(brand, json, logo, banner, cover);
    }

    private OrganizationResponse updateOrganizationBrand(BrandDTO json, MultipartFile logo, MultipartFile banner, MultipartFile cover) {
        Long orgId = securityService.getCurrentUserOrganizationId();
        if (json.id == null) {
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, P$PRO$0005);
        }

        BrandsEntity brand = brandsRepository.findByIdAndOrganizationEntity_Id(json.id, orgId)
                .orElseThrow(() -> new RuntimeBusinessException(NOT_FOUND, P$BRA$0001, json.id));

        if (json.name != null) {
            brand.setName(json.name);
        }

        return modifyBrandAdditionalData(brand, json, logo, banner, cover);
    }
    
    
    
    
    

	@Override
    public List<ProductFeatureDTO> getProductFeatures(Long orgId) {
		return featureRepo
                .findByOrganizationId(orgId)
                .stream()
                .map(this::toProductFeatureDTO)
                .collect(toList());
	}


    private ProductFeatureDTO toProductFeatureDTO(ProductFeaturesEntity entity) {
        ProductFeatureType type =
                ProductFeatureType
                .getProductFeatureType(entity.getType())
                .orElse(STRING);
        Map<String, ?> extraData =
                ofNullable(entity.getExtraData())
                .map(JSONObject::new)
                .map(JSONObject::toMap)
                .orElse(emptyMap());
        ProductFeatureDTO dto = new ProductFeatureDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setPname(entity.getPname());
        dto.setDescription(entity.getDescription());
        dto.setLevel(entity.getLevel());
        dto.setType(type);
        dto.setExtraData(extraData);
        return dto;
    }



    @Override
    @Transactional
    public ProductFeatureUpdateResponse updateProductFeature(ProductFeatureUpdateDTO featureDto) {
        OrganizationEntity org = securityService.getCurrentUserOrganization();
		Long orgId = org.getId();

		validateProductFeature(featureDto, orgId);
		ProductFeaturesEntity entity = saveFeatureToDb(featureDto, org);

		return new ProductFeatureUpdateResponse(entity.getId());
	}




    @Override
    @Transactional(rollbackFor = Throwable.class)
    public void removeProductFeature(Integer  featureId){
        Long orgId = securityService.getCurrentUserOrganizationId();
        featureRepo
            .findByIdAndOrganization_Id(featureId, orgId)
            .map(this::validateProductFeatureToDelete)
            .map(this::doRemoveProductFeature)
            .orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, P$FTR$0001, featureId));
    }

    @Override
    public List<YeshteryOrganizationDTO> getYeshteryOrganizations(List<Long> categoryIds) {
        List<OrganizationEntity> orgs;
        if (categoryIds != null && !categoryIds.isEmpty()) {
            orgs = organizationRepository.findYeshteryOrganizationsFilterByCategory(categoryIds);
        }
        else {
            orgs = organizationRepository.findYeshteryOrganizations();
        }
        return orgs
                .stream()
                .map(this::toYeshteryOrganizationDto)
                .collect(toList());
    }

    @Override
    public List<OrganizationEntity> getYeshteryOrgs() {
        return organizationRepository.findByYeshteryState(1);
    }

    private YeshteryOrganizationDTO toYeshteryOrganizationDto(OrganizationEntity org) {
        YeshteryOrganizationDTO dto = new YeshteryOrganizationDTO();
        dto.setId(org.getId());
        dto.setName(org.getName());
        dto.setDescription(org.getDescription());
        dto.setImages(getOrganizationImages(org.getId()));
        dto.setShops(getOrganizationShopsDto(org));
        return dto;
    }

    private List<ShopRepresentationObject> getOrganizationShopsDto(OrganizationEntity org) {
        return org.getShops()
                .stream()
                .filter(shop -> shop.getRemoved() == 0)
                .map(s -> (ShopRepresentationObject) s.getRepresentation())
                .collect(toList());
    }

    private ProductFeaturesEntity doRemoveProductFeature(ProductFeaturesEntity feature) {
        variantFeatureValuesRepo.deleteByFeature_Id(feature.getId());
        featureRepo.delete(feature);
        return feature;
    }



    private ProductFeaturesEntity validateProductFeatureToDelete(ProductFeaturesEntity feature) {
        if(variantsHasTheFeature(feature)){
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, P$FTR$0002, feature.getId());
        }
        return feature;
    }



    private boolean variantsHasTheFeature(ProductFeaturesEntity feature) {
        return productService.getVariantsWithFeature(feature).size() > 0;
    }



    private Optional<ExtraAttributesEntity> createExtraAttributesIfNeeded(ProductFeaturesEntity entity) {
        Integer type = entity.getType();
       if(nonNull(type) && FEATURE_TYPE_WITH_EXTRA_DATA.contains(type)){
           return getSavedExtraAttrInFeatureConfig(entity)
                    .or(() -> findExistingExtraAttrInDb(entity))
                    .or(() -> doCreateExtraAttribute(entity));
       }
       return Optional.empty();
    }



    private Optional<ExtraAttributesEntity> findExistingExtraAttrInDb(ProductFeaturesEntity entity) {
        var orgId = securityService.getCurrentUserOrganizationId();
        String name = getAdditionalDataExtraAttrName(entity);
        return extraAttributesRepository.findByNameAndOrganizationId(name, orgId);
    }


    private Optional<ExtraAttributesEntity> getSavedExtraAttrInFeatureConfig(ProductFeaturesEntity entity) {
        Long orgId = securityService.getCurrentUserOrganizationId();
        return getAdditionalDataExtraAttrId(entity)
                .flatMap(id -> extraAttributesRepository.findByIdAndOrganizationId(id, orgId));
    }



    private Optional<ExtraAttributesEntity> doCreateExtraAttribute(ProductFeaturesEntity entity) {
        Long orgId = securityService.getCurrentUserOrganizationId();
        String name = getAdditionalDataExtraAttrName(entity);

        ExtraAttributesEntity attr = new ExtraAttributesEntity();
        attr.setType(INVISIBLE.getValue());
        attr.setName(name);
        attr.setOrganizationId(orgId);
        return Optional.of(extraAttributesRepository.save(attr));
    }



    private void addExtraAttrToFeatureExtraData(ProductFeaturesEntity entity, ExtraAttributesEntity attr) {
        String featureExtraDataBefore = ofNullable(entity.getExtraData()).orElse("{}");
        String featureExtraDataAfter =
                new JSONObject(featureExtraDataBefore)
                        .put(EXTRA_ATTRIBUTE_ID, attr.getId())
                        .toString();
        entity.setExtraData(featureExtraDataAfter);
    }



    @Override
    public String getAdditionalDataExtraAttrName(ProductFeaturesEntity feature) {
        String typeName =
                ofNullable(feature.getType())
                .flatMap(ProductFeatureType::getProductFeatureType)
                .map(ProductFeatureType::name)
                .orElseThrow(() -> new RuntimeBusinessException(INTERNAL_SERVER_ERROR, P$FTR$0001, feature.getType()));
        return format("$%s$%s", feature.getPname(), typeName);
    }



    @Override
    public Optional<Integer> getAdditionalDataExtraAttrId(ProductFeaturesEntity feature){
        return ofNullable(feature.getExtraData())
                .map(JSONObject::new)
                .filter(json -> json.has(EXTRA_ATTRIBUTE_ID))
                .map(json -> json.getInt(EXTRA_ATTRIBUTE_ID));
    }



    private ProductFeaturesEntity saveFeatureToDb(ProductFeatureUpdateDTO featureDto, OrganizationEntity org) {
		ProductFeaturesEntity entity = new ProductFeaturesEntity();
		entity.setOrganization( org );
		
		Operation opr = featureDto.getOperation();
		
		if( opr.equals( Operation.UPDATE)) {			
			entity = featureRepo.findByIdAndOrganization_Id( featureDto.getFeatureId(), org.getId() )
                    .orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, ORG$FTR$0003, featureDto.getFeatureId()));
		}
		if(featureDto.isUpdated("name")){
			entity.setName( featureDto.getName()); 
		}
		
		setPnameOrGenerateDefault(featureDto, entity, opr);
		
		if(featureDto.isUpdated("description")) {
			entity.setDescription( featureDto.getDescription() );
		}
        if(featureDto.isUpdated("level")) {
            if (opr.equals( Operation.CREATE) && featureDto.getLevel() == 0) {
                entity.setLevel(0);
            } else {
                entity.setLevel( featureDto.getLevel() );
            }
        }
        if(featureDto.isUpdated("type")) {
            var type = ofNullable(featureDto.getType()).orElse(STRING).getValue();
            entity.setType(type);
            var attr = createExtraAttributesIfNeeded(entity);
            if(attr.isPresent()){
                addExtraAttrToFeatureExtraData(entity, attr.get());
            }
        }

		return featureRepo.save(entity);
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
	
	
	
	

	private void validateProductFeature(ProductFeatureUpdateDTO featureDto, Long orgId) {
		if(!featureDto.areRequiredAlwaysPropertiesPresent()) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, GEN$0022);
		}

		Operation opr = featureDto.getOperation();
		validateOperation(featureDto.getOperation());
		
		if(opr.equals(Operation.CREATE)) {
			validateProductFeatureForCreate(featureDto, orgId);
		}else if(opr.equals(Operation.UPDATE)) {
			validateProductFeatureForUpdate(featureDto);
		}
	}

	private void validateOperation(Operation opr) {
		if(opr == null) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, G$PRAM$0001, "operation");
		}
		if(!opr.equals(Operation.CREATE) && !opr.equals(Operation.UPDATE)) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, P$PRO$0007);
		}
	}

	private void validateProductFeatureForUpdate(ProductFeatureUpdateDTO featureDto) {
		if(!featureDto.areRequiredForUpdatePropertiesProvided()) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, GEN$0022);
		}
		if(featureDto.isUpdated("name") && isBlankOrNull(featureDto.getName())) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, ORG$FTR$0001);
		}		
	}

	private void validateProductFeatureForCreate(ProductFeatureUpdateDTO featureDto, Long orgId){
		if(!featureDto.areRequiredForCreatePropertiesProvided()) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, GEN$0022);
		}
		if(isBlankOrNull(featureDto.getName())) {
		    throw new RuntimeBusinessException(NOT_ACCEPTABLE, ORG$FTR$0001);
		}
        if(featureRepo.existsByNameAndOrganizationId(featureDto.getName(), orgId)) {
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, ORG$FTR$0002);
        }
	}



	@Override
    @Transactional
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

        BaseUserEntity user =  securityService.getCurrentUser();
        if (!user.getOrganizationId().equals(imgMetaData.getOrganizationId()))
            throw new BusinessException("INSUFFICIENT RIGHTS","User doesn't belong to organization",
                    FORBIDDEN);
    }

    private ProductImageUpdateResponse createNewOrganizationImg(MultipartFile file, OrganizationImageUpdateDTO imgMetaData) throws BusinessException {
        if(!imgMetaData.areRequiredForCreatePropertiesProvided()) {
            throw new BusinessException(
                    format("Missing required parameters! required parameters for adding new image are: %s", imgMetaData.getRequiredPropertiesForDataCreate())
                    , "MISSING PARAM", NOT_ACCEPTABLE);
        }

        validateOrganizationImageUpdateData(imgMetaData);

        if(file == null || file.isEmpty() || file.getContentType() == null)
            throw new BusinessException("No image file provided!", "MISSIG PARAM:image", NOT_ACCEPTABLE);

        String mimeType = file.getContentType();
        if(!mimeType.startsWith("image") && !mimeType.startsWith("video"))
            throw new BusinessException(format("Invalid file type[%s]! only MIME ['image','video] types are accepted!", mimeType)
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
            throw new BusinessException(format("Missing required parameters! required parameters for updating existing image are: %s",
                    imgMetaData.getRequiredPropertyNamesForDataUpdate()), "MISSING PARAM", NOT_ACCEPTABLE);

        validateOrganizationImageUpdateData(imgMetaData);

        Long imgId = imgMetaData.getImageId();

        if( !organizationImagesRepository.existsById(imgId))
            throw new BusinessException(
                    format("No organization image exists with id: %d !", imgId), "INVALID PARAM:image_id", NOT_ACCEPTABLE);

        if(file != null) {
            String mimeType = file.getContentType();
            if (!mimeType.startsWith("image"))
                throw new BusinessException(format("Invalid file type[%]! only MIME 'image' types are accepted!", mimeType)
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
                throw new BusinessException(format("No shop exists with id: %d !", shopId), "INVALID PARAM: shop_id", NOT_ACCEPTABLE);
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


    
    
    
    @Override
    @CacheEvict(allEntries = true, cacheNames = { ORGANIZATIONS_BY_NAME, ORGANIZATIONS_BY_ID})
    public void deleteImage(Long imgId, String url) {
        Long orgId = securityService.getCurrentUserOrganizationId();
        OrganizationImagesEntity img = new OrganizationImagesEntity();
        if (imgId != null) {
            img = organizationImagesRepository
                    .findByIdAndOrganizationEntity_Id(imgId, orgId)
                    .orElseThrow(()-> new RuntimeBusinessException(NOT_ACCEPTABLE, ORG$IMG$0001, imgId));
        } else if (url != null){
            img = organizationImagesRepository
                    .findByUriAndOrganizationEntity_Id(url, orgId)
                    .orElseThrow(()-> new RuntimeBusinessException(NOT_ACCEPTABLE, ORG$IMG$0002, url));
        }
        organizationImagesRepository.deleteById(img.getId());

        fileService.deleteFileByUrl(img.getUri());
    }
    
    
    @Override
    @CacheResult(cacheName = ORGANIZATIONS_DOMAINS)
    public Pair getOrganizationAndSubdirsByUrl(String urlString, Integer yeshteryState) {
        urlString = urlString.startsWith("http") ? urlString: "http://"+urlString;
        URIBuilder url = domainService.validateDomainCharacters(urlString);

        String domain = ofNullable(url.getHost()).orElse("");

	    String subDir = null;
	    if (url.getPath() != null && url.getPath().length() > 1) {
		    String[] subdirectories = url.getPath().split("/");
		    if (subdirectories.length > 1 && subdirectories[1].length() > 0) {
		    	subDir = subdirectories[1];
		    }
	    }
	    
	    OrganizationDomainsEntity orgDom = null;
	    if (domain.endsWith(NASNAV_DOMAIN) || domain.endsWith(NASORG_DOMAIN)) {
	    	// try to check if we have full domain matching first without subdomain
		    orgDom = orgDomainsRep.findByDomainAndSubdir(domain,null);
		    if (orgDom == null) {
			    orgDom = orgDomainsRep.findByDomainAndSubdir(domain, subDir);
		    } else {
		    	// the check succeeded with subdir = null
			   subDir = null;
		    }
	    } else {
	    	orgDom = orgDomainsRep.findByDomain(domain);
	    	subDir = null;
	    }
	    if (orgDom != null)
            if (orgDom.getOrganizationEntity().getYeshteryState() == 0 && yeshteryState == 1) {
                return new Pair(0L, 0L);
            }
	    
//	    System.out.println("## domain: " + domain + ", subDir: " + subDir + ", orgDom: " + orgDom);
	    
		return (orgDom == null) ? new Pair(0L, 0L) : new Pair(orgDom.getOrganizationEntity().getId(), subDir == null ? 0L : 1L);
    }


    @Override
    public void deleteExtraAttribute(Integer attrId) {
        Long orgId = securityService.getCurrentUserOrganizationId();
        if (!extraAttributesRepository.existsByIdAndOrganizationId(attrId, orgId))
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, ORG$EXTRATTR$0001, attrId);

        productExtraAttrRepo.deleteByIdAndOrganizationId(attrId, orgId);

        extraAttributesRepository.deleteByIdAndOrganizationId(attrId, orgId);
    }



    
    
	@Override
    public List<ExtraAttributeDefinitionDTO> getExtraAttributes() {
		Long orgId = securityService.getCurrentUserOrganizationId();
		return extraAttrRepo
				.findByOrganizationId(orgId)
				.stream()
				.map(this::createExtraAttributeDTO)
				.collect(toList());
	}
	
	
	
	
	
	private ExtraAttributeDefinitionDTO createExtraAttributeDTO(ExtraAttributesEntity entity) {
        ExtraAttributeType type =
                getExtraAttributeType(entity.getType())
                .orElse(ExtraAttributeType.STRING);
        Boolean invisible = Objects.equals(type, INVISIBLE);
		ExtraAttributeDefinitionDTO dto = new ExtraAttributeDTO();
		dto.setIconUrl(entity.getIconUrl());
		dto.setId(entity.getId());
		dto.setName(entity.getName());
		dto.setType(type);
		dto.setInvisible(invisible);
		return dto;
	}




    @Override
    @CacheEvict(allEntries = true, cacheNames = { ORGANIZATIONS_BY_NAME, ORGANIZATIONS_BY_ID})
	public void deleteSetting(String settingName) {
		Long orgId = securityService.getCurrentUserOrganizationId();
		settingRepo.deleteBySettingNameAndOrganization_Id(settingName, orgId);
	}




    @Override
    @CacheEvict(allEntries = true, cacheNames = { ORGANIZATIONS_BY_NAME, ORGANIZATIONS_BY_ID, COUNTRIES})
	public void updateSetting(SettingDTO settingDto) {
		validateSetting(settingDto);
		
		Long orgId = securityService.getCurrentUserOrganizationId();
		SettingEntity setting = 
				settingRepo
				.findBySettingNameAndOrganization_Id(settingDto.getName(), orgId)
				.orElseGet(() -> new SettingEntity());
		createSettingEntity(setting, settingDto);
		settingRepo.save(setting);		
	}



	private void validateSetting(SettingDTO settingDto) {
		if(anyIsNull(settingDto, settingDto.getName(), settingDto.getValue())) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, G$PRAM$0001, settingDto.toString());
		}else if(!isValidSettingName(settingDto)) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, ORG$SETTING$0001, settingDto.getName());
		}
	}



	private boolean isValidSettingName(SettingDTO settingDto) {
        boolean notEmpty =
                ofNullable(settingDto.getName())
                        .map(name -> !name.isEmpty())
                        .orElse(false);
		boolean existsInSettingEnum =
                stream(Settings.values())
				.map(Settings::name)
				.anyMatch(name -> Objects.equals(name, settingDto.getName()));
		return notEmpty && (isPublicSetting(settingDto) || existsInSettingEnum);
	}



    private boolean isPublicSetting(SettingDTO settingDto) {
        return ofNullable(settingDto)
                .map(SettingDTO::getType)
                .map(type -> Objects.equals(type, PUBLIC.getValue()))
                .orElse(false);
    }




    private SettingEntity createSettingEntity(SettingEntity entity, SettingDTO settingDto) {
		OrganizationEntity organization = securityService.getCurrentUserOrganization();
        Integer type =
                ofNullable(settingDto.getType())
                        .map(SettingsType::getSettingsType)
                        .orElse(PRIVATE)
                        .getValue();
		entity.setSettingName(settingDto.getName());
		entity.setSettingValue(settingDto.getValue());
		entity.setOrganization(organization);
		entity.setType(type);
		return entity;
	}
	
	
	
	
	
	@Override
    public Map<String,String> getOrganizationSettings(Long orgId){
		return settingRepo
				.findByOrganization_Id(orgId)
				.stream()
				.collect(toMap(SettingEntity::getSettingName, SettingEntity::getSettingValue));
	}



    @Override
    public Optional<String> getOrganizationSettingValue(Long orgId, Settings setting) {
        return settingRepo
                .findBySettingNameAndOrganization_Id(setting.name(), orgId)
                .map(SettingEntity::getSettingValue);
    }


    @Override
    public List<ShopRepresentationObject> getOrganizationShops() {
		Long orgId = securityService.getCurrentUserOrganizationId();
		return shopService.getOrganizationShops(orgId, true);
	}

	@Override
    public String getOrgLogo(Long orgId) {
        return orgImagesRepo
                .findByOrganizationEntityIdAndShopsEntityNullAndTypeOrderByIdDesc(orgId, 1)
                .stream()
                .findFirst()
                .map(OrganizationImagesEntity::getUri)
                .orElse("nasnav-logo.png");
    }


    @Override
    public ResponseEntity<?> getOrgSiteMap(String userToken, SitemapParams params) throws IOException {
        Pair domain = getOrganizationAndSubdirsByUrl(params.getUrl(), 0);
        Long orgId = domain.getFirst();
        if (orgId.intValue() == 0) {
            return createEmptyResponseEntity();
        }
        if (!isBlankOrNull(userToken)) {
            Long userOrgId = userTokenRepo.findEmployeeOrgIdByToken(userToken);
            if (userOrgId == null || !userOrgId.equals(orgId)) {
                return createEmptyResponseEntity();
            }
        }
        return createSiteMapResponse(orgId, params);
    }


    private ResponseEntity<?> createSiteMapResponse(Long orgId, SitemapParams params) throws IOException {
        List<String> allUrls = createSiteMap(orgId, params);
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        Writer outputWriter = new OutputStreamWriter(outStream);
        for(String u : allUrls) {
            outputWriter.write(u + '\n');
        }
        outputWriter.flush();
        outputWriter.close();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/plain"))
                .header(CONTENT_DISPOSITION, "attachment; filename=sitemap.txt")
                .body(outStream.toString());
    }


    private ResponseEntity<?> createEmptyResponseEntity() {
        return ResponseEntity.notFound().build();
    }


    private List<String> createSiteMap(Long orgId, SitemapParams params) {
        List<String> allUrls = new ArrayList<>();
        String baseUrl = domainService.getOrganizationDomainAndSubDir(orgId);
        if(params.isInclude_products()) {
            addPairsAsUrls(baseUrl, productRepo.getProductIdAndNamePairs(orgId), "products", allUrls);
        }
        if (params.isInclude_collections()) {
            addPairsAsUrls(baseUrl, productRepo.getCollectionIdAndNamePairs(orgId), "collections", allUrls);
        }
        if (params.isInclude_brands()) {
            addPairsAsUrls(baseUrl, brandsRepository.getBrandIdAndNamePairs(orgId), "brands", allUrls);
        }
        if (params.isInclude_tags()) {
            addPairsAsUrls(baseUrl, tagsRepo.getTagIdAndNamePairs(orgId), "categories", allUrls);
        }
        if (params.isInclude_tags_tree()) {
            addPairsAsUrls(baseUrl, tagGraphNodeRepo.getTagNodeIdAndNamePairs(orgId), "categories", allUrls);
        }
        return allUrls;
    }

    private void addPairsAsUrls(String domain, List<IdAndNamePair> idAndNamePairs, String entityType, List<String> urlList) {
        idAndNamePairs
                .stream()
                .map(p -> domain+"/"+entityType+"/"+p.getName()+"/"+p.getId())
                .forEach(urlList::add);
    }


    @Override
    public List<String> getSubscribedUsers() {
        OrganizationEntity org = securityService.getCurrentUserOrganization();
        return subsRepo.findEmailsByOrganizationAndTokenNull(org);

    }


    @Override
    public void removeSubscribedUser(String email) {
        OrganizationEntity org = securityService.getCurrentUserOrganization();
        subsRepo.deleteByEmailAndOrganizationAndTokenNull(email, org);
    }

    @Override
    public LinkedHashMap<String, Map<String, String>> getOrganizationPaymentGateways(Long orgId, String deliveryService) {
        List<OrganizationPaymentGatewaysEntity> gateways = orgPaymentGatewaysRep.findAllByOrganizationId(orgId);
        if (gateways == null || gateways.size() == 0) {
            // no specific gateways defined for this org, use the default ones
            gateways = orgPaymentGatewaysRep.findAllByOrganizationIdIsNull();
        }
        LinkedHashMap<String, Map<String, String>> response = new LinkedHashMap();
        for (OrganizationPaymentGatewaysEntity gateway: gateways) {
            Map<String, String> body = new HashMap();
            if (deliveryService != null) {
                // For now - hardcoded rule for not allowing CoD for Pickup service (to prevent misuse)
                if (COD.getValue().equalsIgnoreCase(gateway.getGateway()) ) {
                    if (isCodAvailableForService(deliveryService)) {
                        body.put("icon", domainService.getBackendUrl()+"/icons/cod.svg");
                        response.put(gateway.getGateway(), body);
                    } else {
                        continue;
                    }
                }
            }
            if (MASTERCARD.getValue().equalsIgnoreCase(gateway.getGateway())) {
                MastercardAccount account = new MastercardAccount();
                account.init(Tools.getPropertyForAccount(gateway.getAccount(), classLogger, config.paymentPropertiesDir), gateway.getId());
                body.put("script", account.getScriptUrl());
                body.put("icon", domainService.getBackendUrl()+account.getIcon());
            } else if (RAVE.getValue().equalsIgnoreCase(gateway.getGateway())) {
                RaveAccount raveAccount = new RaveAccount(Tools.getPropertyForAccount(gateway.getAccount(), classLogger, config.paymentPropertiesDir), gateway.getId());
                body.put("script", raveAccount.getScriptUrl());
                body.put("icon", domainService.getBackendUrl()+raveAccount.getIcon());
            } else if (UPG.getValue().equalsIgnoreCase(gateway.getGateway())) {
                UpgAccount account = new UpgAccount();
                account.init(Tools.getPropertyForAccount(gateway.getAccount(), classLogger, config.paymentPropertiesDir));
                body.put("script", account.getUpgScriptUrl());
                body.put("icon", domainService.getBackendUrl()+account.getIcon());
            }
            response.put(gateway.getGateway(), body);
        }
        return response;
    }



    @Override
    public List<ProductFeatureType> getProductFeatureTypes() {
        return asList(ProductFeatureType.values());
    }
}
