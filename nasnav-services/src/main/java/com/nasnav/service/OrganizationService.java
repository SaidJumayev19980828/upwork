package com.nasnav.service;

import com.nasnav.dto.*;
import com.nasnav.dto.request.organization.OrganizationCreationDTO;
import com.nasnav.dto.request.organization.OrganizationModificationDTO;
import com.nasnav.dto.request.organization.SettingDTO;
import com.nasnav.enumerations.ProductFeatureType;
import com.nasnav.enumerations.Settings;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.persistence.ProductFeaturesEntity;
import com.nasnav.request.SitemapParams;
import com.nasnav.response.OrganizationResponse;
import com.nasnav.response.ProductFeatureUpdateResponse;
import com.nasnav.response.ProductImageUpdateResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface OrganizationService {
    List<OrganizationRepresentationObject> listOrganizations();

    OrganizationRepresentationObject getOrganizationByName(String organizationName) throws BusinessException;

    OrganizationRepresentationObject getOrganizationById(Long organizationId);

    List<ExtraAttributesRepresentationObject> getOrganizationExtraAttributesById(Long organizationId);

    OrganizationResponse createOrganization(OrganizationCreationDTO json) throws BusinessException;

    OrganizationResponse updateOrganizationData(OrganizationModificationDTO json, MultipartFile file) throws BusinessException;

    List<Organization_BrandRepresentationObject> getOrganizationBrands(Long orgId);

    OrganizationResponse validateAndUpdateBrand(BrandDTO json, MultipartFile logo, MultipartFile banner, MultipartFile cover);

    OrganizationResponse createOrganizationBrand(BrandDTO json, MultipartFile logo, MultipartFile banner, MultipartFile cover) throws BusinessException;

    List<ProductFeatureDTO> getProductFeatures(Long orgId);

    ProductFeatureUpdateResponse updateProductFeature(ProductFeatureUpdateDTO featureDto) throws BusinessException;

    ProductImageUpdateResponse updateOrganizationImage(MultipartFile file, OrganizationImageUpdateDTO imgMetaData) throws BusinessException;

    boolean deleteImage(Long imgId) throws BusinessException;

    Pair getOrganizationAndSubdirsByUrl(String urlString);

    void deleteExtraAttribute(Integer attrId);

    List<ExtraAttributeDefinitionDTO> getExtraAttributes();

    void deleteSetting(String settingName);

    void updateSetting(SettingDTO settingDto);

    Map<String,String> getOrganizationSettings(Long orgId);

    Optional<String> getOrganizationSettingValue(Long orgId, Settings setting);

    List<ShopRepresentationObject> getOrganizationShops();

    String getOrgLogo(Long orgId);

    ResponseEntity<?> getOrgSiteMap(String userToken, SitemapParams params) throws IOException;

    List<String> getSubscribedUsers();

    void removeSubscribedUser(String email);

    LinkedHashMap<String, Map<String, String>> getOrganizationPaymentGateways(Long orgId, String deliveryService);

    List<ProductFeatureType> getProductFeatureTypes();

    String getAdditionalDataExtraAttrName(ProductFeaturesEntity feature);

    Optional<Integer> getAdditionalDataExtraAttrId(ProductFeaturesEntity feature);

    void removeProductFeature(Integer featureId);
}
