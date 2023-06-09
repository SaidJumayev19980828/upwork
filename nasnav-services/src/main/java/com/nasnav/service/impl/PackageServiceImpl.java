package com.nasnav.service.impl;

import com.nasnav.dao.PackageRepository;
import com.nasnav.dao.PackageRegisteredRepository;
import com.nasnav.dto.request.PackageDto;
import com.nasnav.dto.request.PackageRegisteredByUserDTO;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.PackageEntity;
import com.nasnav.persistence.PackageRegisteredEntity;
import com.nasnav.service.PackageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

import static com.nasnav.exceptions.ErrorCodes.*;
import static org.springframework.http.HttpStatus.NOT_FOUND;


@Service
public class PackageServiceImpl implements PackageService {
    @Autowired
    private PackageRepository packageRepository;

    @Autowired
    private PackageRegisteredRepository packageRegisteredRepository;

    public List<PackageEntity> getPackage() {
        List<PackageEntity> packages = packageRepository.findAll();
        return packages;
    }

    @Override
    public void createPackage(PackageDto json) throws Exception {
        PackageEntity newPackage = new PackageEntity();
        newPackage.setName(json.getName());
        newPackage.setDescription(json.getDescription());
        newPackage.setPrice(json.getPrice());
        packageRepository.save(newPackage);
    }

    @Override
    public void updatePackage(PackageDto dto, Long packageId) {
        PackageEntity entity = packageRepository.findById(packageId).orElseThrow(
                () -> new RuntimeBusinessException(NOT_FOUND,G$EVENT$0001,packageId)
        );
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setPrice(dto.getPrice());
        packageRepository.save(entity);
    }
    @Override
    @Transactional(rollbackFor = Throwable.class)
    public void removePackage(Long packageId) {
        PackageEntity entity = packageRepository.findById(packageId).orElseThrow(
                () -> new RuntimeBusinessException(NOT_FOUND,G$EVENT$0001,packageId)
        );
        List<PackageRegisteredEntity> packagesRegistered= packageRegisteredRepository.findByPackageId(packageId);
        if(!packagesRegistered.isEmpty()){
            throw new RuntimeBusinessException(HttpStatus.NOT_ACCEPTABLE, PA$USR$0001, packageId);
        }
        packageRepository.delete(entity);
    }

    @Override
    public PackageRegisteredEntity completeProfile(PackageRegisteredByUserDTO packageRegisteredByUserDTO) {

        PackageEntity packageEntity = packageRepository.findById(packageRegisteredByUserDTO.getPackageId()).get();

        PackageRegisteredEntity packageRegisteredEntity = PackageRegisteredEntity.builder().packageEntity(packageEntity).userId(packageRegisteredByUserDTO.getUserId()).registeredDate(new Date()).build();

        return packageRegisteredRepository.save(packageRegisteredEntity);
    }
}
