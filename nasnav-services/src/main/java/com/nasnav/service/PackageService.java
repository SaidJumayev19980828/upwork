package com.nasnav.service;

import com.nasnav.dto.request.PackageDTO;
import com.nasnav.dto.request.PackageRegisterDTO;
import com.nasnav.dto.response.PackageResponse;
import com.nasnav.dto.response.SimpleOrganizationDto;
import com.nasnav.persistence.OrganizationEntity;
import com.nasnav.persistence.PackageEntity;
import com.nasnav.persistence.UserEntity;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface PackageService {

    List<PackageResponse> getPackages();

    PackageResponse createPackage(PackageDTO json) throws Exception;

    PackageResponse updatePackage(PackageDTO packageDto, Long packageId);

    void deletePackage(Long packageId);

    String registerPackage(PackageRegisterDTO packageRegisterDTO);

    String deregisterPackage(PackageRegisterDTO packageRegisterDTO);

    Long getPackageIdRegisteredInOrg(UserEntity user);

    @Transactional
    PackageEntity getPackageRegisteredInOrg(OrganizationEntity organization);

    List<SimpleOrganizationDto> getOrganizationsByPackageId(Long packageId);
}
