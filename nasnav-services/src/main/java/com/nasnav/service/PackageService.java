package com.nasnav.service;

import com.nasnav.dto.request.PackageDto;
import com.nasnav.dto.request.PackageRegisteredByUserDTO;
import com.nasnav.dto.response.PackageResponse;

import java.util.List;

public interface PackageService {

    List<PackageResponse> getPackages();

    void createPackage(PackageDto json) throws Exception;

    void updatePackage(PackageDto packageDto, Long packageId);

    void removePackage(Long packageId);

    Long registerPackageProfile(PackageRegisteredByUserDTO packageRegisteredByUserDTO);

}
