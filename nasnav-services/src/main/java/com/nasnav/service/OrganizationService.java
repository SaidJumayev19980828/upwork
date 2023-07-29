package com.nasnav.service;

import com.nasnav.dto.*;
import com.nasnav.dto.request.RegisterDto;
import com.nasnav.dto.request.organization.OrganizationCreationDTO;
import com.nasnav.dto.request.organization.OrganizationModificationDTO;
import com.nasnav.dto.request.organization.SettingDTO;
import com.nasnav.dto.response.YeshteryOrganizationDTO;
import com.nasnav.enumerations.Settings;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.persistence.OrganizationEntity;
import com.nasnav.request.SitemapParams;
import com.nasnav.response.DomainOrgIdResponse;
import com.nasnav.response.OrganizationResponse;
import com.nasnav.response.ProductImageUpdateResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

public interface OrganizationService {
    List<OrganizationRepresentationObject> listOrganizations();

    OrganizationRepresentationObject getOrganizationByNameOrUrlOrId(String name, String url, Long id, Integer yeshteryState) throws BusinessException;

    OrganizationRepresentationObject getOrganizationByName(String organizationName, Integer yeshteryState) throws BusinessException;

    OrganizationRepresentationObject getOrganizationById(Long organizationId, Integer yeshteryState);

    OrganizationResponse createOrganization(OrganizationCreationDTO json) throws BusinessException;

    OrganizationResponse registerOrganization(RegisterDto json) throws Exception;

    OrganizationResponse updateOrganizationData(OrganizationModificationDTO json, MultipartFile file) throws BusinessException;

    OrganizationResponse validateAndUpdateBrand(BrandDTO json, MultipartFile logo, MultipartFile banner, MultipartFile cover);

    OrganizationResponse createOrganizationBrand(BrandDTO json, MultipartFile logo, MultipartFile banner, MultipartFile cover) throws BusinessException;

    ProductImageUpdateResponse updateOrganizationImage(MultipartFile file, OrganizationImageUpdateDTO imgMetaData) throws BusinessException;

    void deleteImage(Long imgId, String url);

    DomainOrgIdResponse getOrganizationAndSubdirsByUrl(String urlString, Integer yeshteryState);

    void deleteSetting(String settingName);

    void updateSetting(SettingDTO settingDto);

    Map<String,String> getOrganizationSettings(Long orgId);

    Optional<String> getOrganizationSettingValue(Long orgId, Settings setting);

    List<ShopRepresentationObject> getOrganizationShops();

    String getOrgLogo(Long orgId);

    ResponseEntity<String> getOrgSiteMap(String userToken, SitemapParams params) throws IOException;

    List<String> getSubscribedUsers();

    void removeSubscribedUser(String email);

    LinkedHashMap<String, Map<String, Object>> getOrganizationPaymentGateways(Long orgId, String deliveryService);

    List<YeshteryOrganizationDTO> getYeshteryOrganizations(List<Long> categoryIds);

    List<OrganizationEntity> getYeshteryOrgs();
}
