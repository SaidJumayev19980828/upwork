package com.nasnav.service;

import com.nasnav.dto.request.PackageDTO;
import com.nasnav.dto.request.PackageRegisteredByUserDTO;
import com.nasnav.dto.response.PackageResponse;

import java.util.List;

public interface PackageService {

    List<PackageResponse> getPackages();

    Long createPackage(PackageDTO json) throws Exception;

    void updatePackage(PackageDTO packageDto, Long packageId);

    void removePackage(Long packageId);

    Long registerPackageProfile(PackageRegisteredByUserDTO packageRegisteredByUserDTO);

}
