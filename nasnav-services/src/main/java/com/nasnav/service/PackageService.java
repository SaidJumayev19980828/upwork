package com.nasnav.service;

import com.nasnav.dto.request.PackageDTO;
import com.nasnav.dto.request.PackageRegisteredByUserDTO;
import com.nasnav.dto.response.PackageResponse;

import java.util.List;

public interface PackageService {

    List<PackageResponse> getPackages();

    PackageResponse createPackage(PackageDTO json) throws Exception;

    PackageResponse updatePackage(PackageDTO packageDto, Long packageId);

    void removePackage(Long packageId);

    Long registerPackageProfile(PackageRegisteredByUserDTO packageRegisteredByUserDTO);

}
