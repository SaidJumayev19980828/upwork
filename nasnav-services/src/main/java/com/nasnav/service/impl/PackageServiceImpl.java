package com.nasnav.service.impl;

import com.nasnav.dao.CountryRepository;
import com.nasnav.dao.PackageRepository;
import com.nasnav.dao.PackageRegisteredRepository;
import com.nasnav.dto.request.PackageDto;
import com.nasnav.dto.request.PackageRegisteredByUserDTO;
import com.nasnav.dto.response.PackageResponse;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.mappers.PackageMapper;
import com.nasnav.persistence.*;
import com.nasnav.service.PackageService;
import com.nasnav.service.SecurityService;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static com.nasnav.exceptions.ErrorCodes.*;
import static org.springframework.http.HttpStatus.NOT_FOUND;


@Service
@RequiredArgsConstructor
public class PackageServiceImpl implements PackageService {
    private final SecurityService securityService;
    private final PackageRepository packageRepository;
    private final PackageRegisteredRepository packageRegisteredRepository;
    private final PackageMapper packageMapper;
    private final CountryRepository countryRepo;
    @Override
    public List<PackageResponse> getPackages() {
        return packageRepository.findAll().stream().map(packageMapper::toPackageResponse).collect(Collectors.toList());
    }

    @Override
    public void createPackage(PackageDto json) throws Exception {
        PackageEntity newPackage = new PackageEntity();
        newPackage.setName(json.getName());
        newPackage.setDescription(json.getDescription());
        newPackage.setPrice(json.getPrice());
        newPackage.setPeriodInDays(json.getPeriodInDays());
        CountriesEntity country = countryRepo.findByIsoCode(json.getCurrencyIso());
        if(country == null){
           throw new RuntimeBusinessException(NOT_FOUND,PA$CUR$0002,json.getCurrencyIso());
        }
        newPackage.setCountry(country);
        packageRepository.save(newPackage);
    }

    @Override
    public void updatePackage(PackageDto dto, Long packageId) {
        PackageEntity entity = packageRepository.findById(packageId).orElseThrow(
                () -> new RuntimeBusinessException(NOT_FOUND,PA$USR$0002,packageId)
        );
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setPrice(dto.getPrice());
        entity.setPeriodInDays(dto.getPeriodInDays());
        CountriesEntity country = countryRepo.findByIsoCode(dto.getCurrencyIso());
        if(country == null){
            throw new RuntimeBusinessException(NOT_FOUND,PA$CUR$0002,dto.getCurrencyIso());
        }
        entity.setCountry(country);
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

    @Transactional
    @Override
    public Long registerPackageProfile(PackageRegisteredByUserDTO packageRegisteredByUserDTO) {

        OrganizationEntity org = securityService.getCurrentUserOrganization();

        EmployeeUserEntity employee = securityService.getCurrentUserOptional().map(EmployeeUserEntity.class::cast).orElse(null);
        

        PackageEntity packageEntity = packageRepository.findById(packageRegisteredByUserDTO.getPackageId()).orElseThrow(
                () -> new RuntimeBusinessException(NOT_FOUND, PA$USR$0002, packageRegisteredByUserDTO.getPackageId()));

        PackageRegisteredEntity packageRegisteredEntity = packageRegisteredRepository.findByOrganization(org).orElseGet(() -> new PackageRegisteredEntity(org));
        packageRegisteredEntity.setPackageEntity(packageEntity);

        packageRegisteredEntity.setCreatorEmployee(employee);

        return packageRegisteredRepository.save(packageRegisteredEntity).getId();
    }
}
