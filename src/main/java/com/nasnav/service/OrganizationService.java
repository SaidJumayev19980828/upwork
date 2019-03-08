package com.nasnav.service;

import com.nasnav.dto.CategoryRepresentationObject;
import com.nasnav.dto.OrganizationRepresentationObject;
import com.nasnav.dto.ShopRepresentationObject;
import com.nasnav.exceptions.BusinessException;

public interface OrganizationService {

    OrganizationRepresentationObject getOrganizationByName(String organizationName) throws BusinessException;

    CategoryRepresentationObject getOrganizationCategories(Long organizationId) throws BusinessException;

    ShopRepresentationObject getOrganizationShops(Long organizationId) throws BusinessException;
}
