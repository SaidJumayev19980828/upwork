package com.nasnav.service;

import java.util.List;
import java.util.Set;

import org.springframework.data.domain.PageImpl;

import com.nasnav.dto.Organization_BrandRepresentationObject;
import com.nasnav.dto.request.BrandIdAndPriority;
import com.nasnav.exceptions.BusinessException;

public interface BrandService {

  Organization_BrandRepresentationObject getBrandById(Long brandId, boolean yeshteryState);

  void deleteBrand(Long brandId) throws BusinessException;

  PageImpl<Organization_BrandRepresentationObject> getYeshteryBrands(Integer start, Integer count, Long orgId,
      Set<Long> brandIds);

  PageImpl<Organization_BrandRepresentationObject> getOrganizationBrands(List<Long> orgIds, Integer minPriority,Integer start,Integer count);

  void changeBrandsPriority(List<BrandIdAndPriority> dto);

}