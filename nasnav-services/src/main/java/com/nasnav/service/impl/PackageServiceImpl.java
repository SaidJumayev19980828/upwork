package com.nasnav.service.impl;

import com.nasnav.commons.utils.StringUtils;
import com.nasnav.dao.*;
import com.nasnav.dto.request.PackageDTO;
import com.nasnav.dto.request.PackageRegisteredByUserDTO;
import com.nasnav.dto.request.ServiceDTO;
import com.nasnav.dto.response.PackageResponse;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.mappers.PackageMapper;
import com.nasnav.persistence.*;
import com.nasnav.service.PackageService;
import com.nasnav.service.SecurityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static com.nasnav.exceptions.ErrorCodes.*;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class PackageServiceImpl implements PackageService {
    private final SecurityService securityService;
    private final PackageRepository packageRepository;
    private final PackageRegisteredRepository packageRegisteredRepository;
    private final PackageMapper packageMapper;
    private final CountryRepository countryRepo;
    private final ServiceRepository serviceRepository;
    private final OrganizationRepository orgRepo;

    @Override
    public List<PackageResponse> getPackages() {
        List<PackageEntity> packageEntities = packageRepository.findAll();
        return packageMapper.entitiesToBeansWithoutList(packageEntities);
    }


    @Override
    public PackageResponse createPackage(PackageDTO json) {
        PackageEntity newPackage = new PackageEntity();
        newPackage.setName(json.getName());
        newPackage.setDescription(json.getDescription());
        newPackage.setPrice(json.getPrice());
        newPackage.setPeriodInDays(json.getPeriodInDays());
        if(StringUtils.isBlankOrNull(json.getStripePriceId())){
            throw new RuntimeBusinessException(NOT_ACCEPTABLE,PA$USR$0003);
        }
        newPackage.setStripePriceId(json.getStripePriceId());
        CountriesEntity country = countryRepo.findByIsoCode(json.getCurrencyIso());
        if(country == null){
           throw new RuntimeBusinessException(NOT_ACCEPTABLE,PA$CUR$0002,json.getCurrencyIso());
        }
        newPackage.setCountry(country);
        Set<ServiceEntity> serviceEntitySet = new HashSet<>();
        List<ServiceInstanceEntity> serviceInstanceEntities = new ArrayList<>();
        if(json.getServices() != null){

            List<String> serviceCodes = json.getServices()
                    .stream()
                    .map(ServiceDTO::getCode).collect(Collectors.toList());

            List<ServiceEntity> serviceEntities =
                    serviceRepository.findAllByCodeIn(serviceCodes).orElseThrow();

            for(ServiceDTO serviceDTO : json.getServices()){
                Optional<ServiceEntity> serviceEntity = serviceEntities.stream()
                        .filter(s -> s.getCode().equals(serviceDTO.getCode())).findFirst();
                        if (serviceEntity.isEmpty()){
                            throw new RuntimeBusinessException(NOT_ACCEPTABLE,PA$SRV$0001,
                                    serviceDTO.getCode());
                        }else {
                            serviceEntitySet.add(serviceEntity.get());
                            serviceInstanceEntities.add(
                              ServiceInstanceEntity
                                      .builder()
                                      .serviceId(serviceEntity.get().getId())
                                      .name(serviceDTO.getName())
                                      .description(serviceDTO.getDescription())
                                      .build()
                            );
                        }
            }

        }
        newPackage.setServices(serviceEntitySet);
        newPackage.setServiceInstances(serviceInstanceEntities);
        packageRepository.save(newPackage);
        return packageMapper.toPackageResponse(newPackage);
    }

    @Override
    public PackageResponse updatePackage(PackageDTO dto, Long packageId) {
        PackageEntity entity = packageRepository.findById(packageId).orElseThrow(
                () -> new RuntimeBusinessException(NOT_FOUND,PA$USR$0002,packageId)
        );
        entity.getServiceInstances().clear();
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setPrice(dto.getPrice());
        entity.setPeriodInDays(dto.getPeriodInDays());
        if(StringUtils.isBlankOrNull(dto.getStripePriceId())){
            throw new RuntimeBusinessException(NOT_ACCEPTABLE,PA$USR$0003);
        }
        entity.setStripePriceId(dto.getStripePriceId());
        CountriesEntity country = countryRepo.findByIsoCode(dto.getCurrencyIso());
        if(country == null){
            throw new RuntimeBusinessException(NOT_ACCEPTABLE,PA$CUR$0002,dto.getCurrencyIso());
        }
        entity.setCountry(country);

        Set<ServiceEntity> serviceEntitySet = new HashSet<>();
        List<ServiceInstanceEntity> serviceInstanceEntities = new ArrayList<>();
        if(dto.getServices() != null){
            List<String> serviceCodes = dto.getServices()
                    .stream()
                    .map(ServiceDTO::getCode).collect(Collectors.toList());

            List<ServiceEntity> serviceEntities =
                    serviceRepository.findAllByCodeIn(serviceCodes).orElseThrow();
            for(ServiceDTO serviceDTO : dto.getServices()){
                Optional<ServiceEntity> serviceEntity = serviceEntities.stream()
                        .filter(s -> s.getCode().equals(serviceDTO.getCode())).findFirst();
                if (serviceEntity.isEmpty()){
                    throw new RuntimeBusinessException(NOT_ACCEPTABLE,PA$SRV$0001,
                            serviceDTO.getCode());
                }else {
                    serviceEntitySet.add(serviceEntity.get());
                    serviceInstanceEntities.add(
                            ServiceInstanceEntity
                                    .builder()
                                    .serviceId(serviceEntity.get().getId())
                                    .name(serviceDTO.getName())
                                    .description(serviceDTO.getDescription())
                                    .build()
                    );
                }
            }
        }

        entity.setServices(serviceEntitySet);
        entity.getServiceInstances().addAll(serviceInstanceEntities);
        packageRepository.save(entity);
        return packageMapper.toPackageResponse(entity);

    }
    @Override
    @Transactional(rollbackFor = Throwable.class)
    public void removePackage(Long packageId) {
        PackageEntity entity = packageRepository.findById(packageId).orElseThrow(
                    () -> new RuntimeBusinessException(NOT_FOUND,PA$USR$0002,packageId)
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

    @Transactional
    @Override
    public Long getPackageIdRegisteredInOrg(UserEntity user) {
        Long packageId = null;
        //Get User Organization
        if(user == null || user.getOrganizationId() == null){
            return null;
        }
        Optional<OrganizationEntity> organizationEntity = orgRepo.findById(user.getOrganizationId());
        if(organizationEntity.isEmpty()){
            return null;
        }
        //Get Package Registered For the organization
        PackageRegisteredEntity packageRegisteredEntity = packageRegisteredRepository.findByOrganization(organizationEntity.get()).orElse(null);
        if(packageRegisteredEntity != null && packageRegisteredEntity.getPackageEntity() != null){
            packageId = packageRegisteredEntity.getPackageEntity().getId();
        }
        return packageId;
    }


    @Transactional
    @Override
    public Long getPackageIdRegisteredInOrg(OrganizationEntity organization) {
        Long packageId = null;
        //Get User Organization
        if(organization == null){
            return null;
        }
        //Get Package Registered For the organization
        PackageRegisteredEntity packageRegisteredEntity = packageRegisteredRepository.findByOrganization(organization).orElse(null);
        if(packageRegisteredEntity != null && packageRegisteredEntity.getPackageEntity() != null){
            packageId = packageRegisteredEntity.getPackageEntity().getId();
        }
        return packageId;
    }
}
