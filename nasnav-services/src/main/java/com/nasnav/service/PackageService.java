package com.nasnav.service;

import com.nasnav.dto.request.PackageDto;
import com.nasnav.dto.request.PackageRegisteredByUserDTO;
import com.nasnav.persistence.PackageEntity;
import com.nasnav.persistence.PackageRegisteredEntity;

import java.util.List;

public interface PackageService {

    List<PackageEntity> getPackage();

    void createPackage(PackageDto json) throws Exception;

    void updatePackage(PackageDto packageDto, Long packageId);

    void removePackage(Long packageId);

    PackageRegisteredEntity completeProfile(PackageRegisteredByUserDTO packageRegisteredByUserDTO);

}
