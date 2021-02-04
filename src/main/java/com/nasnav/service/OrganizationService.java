package com.nasnav.service;

import com.nasnav.dto.*;
import com.nasnav.dto.request.organization.SettingDTO;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.response.OrganizationResponse;
import com.nasnav.response.ProductFeatureUpdateResponse;
import com.nasnav.response.ProductImageUpdateResponse;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.cache.annotation.CacheResult;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.nasnav.cache.Caches.*;

public interface OrganizationService {
    List<OrganizationRepresentationObject> listOrganizations();

    OrganizationRepresentationObject getOrganizationByName(String organizationName) throws BusinessException;

    OrganizationRepresentationObject getOrganizationById(Long organizationId);

    List<ExtraAttributesRepresentationObject> getOrganizationExtraAttributesById(Long organizationId);

    OrganizationResponse createOrganization(OrganizationDTO.OrganizationCreationDTO json) throws BusinessException;

    OrganizationResponse updateOrganizationData(OrganizationDTO.OrganizationModificationDTO json, MultipartFile file) throws BusinessException;

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

    List<ShopRepresentationObject> getOrganizationShops();

    String getOrgLogo(Long orgId);

    ByteArrayOutputStream getOrgSiteMap(String url, boolean includeProducts, boolean includeCollections,
                                        boolean includeBrands, boolean includeTags, boolean includeTagsTree) throws IOException;

    List<String> getSubscribedUsers();

    void removeSubscribedUser(String email);

    LinkedHashMap getOrganizationPaymentGateways(Long orgId, String deliveryService);
}
