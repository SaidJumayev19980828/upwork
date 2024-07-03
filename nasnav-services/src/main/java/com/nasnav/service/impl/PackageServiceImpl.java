package com.nasnav.service.impl;

import com.nasnav.commons.utils.StringUtils;
import com.nasnav.dao.*;
import com.nasnav.dto.request.PackageDTO;
import com.nasnav.dto.request.PackageRegisterDTO;
import com.nasnav.dto.response.PackageResponse;
import com.nasnav.dto.response.SimpleOrganizationDto;
import com.nasnav.enumerations.Roles;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.*;
import com.nasnav.service.PackageService;
import com.nasnav.service.SecurityService;
import io.jsonwebtoken.lang.Collections;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.nasnav.exceptions.ErrorCodes.*;
import static org.springframework.http.HttpStatus.*;

@Service
@RequiredArgsConstructor
public class PackageServiceImpl implements PackageService {
    private final SecurityService securityService;
    private final PackageRepository packageRepository;
    private final PackageRegisteredRepository packageRegisteredRepository;
    private final CountryRepository countryRepo;
    private final ServiceRepository serviceRepository;
    private final OrganizationRepository orgRepo;

    @Override
    public List<PackageResponse> getPackages() {
        return packageRepository.findAll()
                .stream().map(PackageResponse::fromPackageEntity).toList();
    }

    @Override
    public List<SimpleOrganizationDto> getOrganizationsByPackageId(Long packageId) {
        return packageRegisteredRepository.findByPackageId(packageId).stream()
                .map(PackageRegisteredEntity::getOrganization)
                .filter(Objects::nonNull)
                .map(org -> new SimpleOrganizationDto(org.getId(), org.getName(), org.getDescription(), org.getPname()))
                .toList();
    }

    @Override
    public PackageResponse createPackage(PackageDTO packageDto) {
        PackageEntity newPackage = new PackageEntity();
        newPackage.setName(packageDto.getName());
        newPackage.setDescription(packageDto.getDescription());
        newPackage.setPrice(packageDto.getPrice());
        newPackage.setPeriodInDays(packageDto.getPeriodInDays());
        if (StringUtils.isBlankOrNull(packageDto.getStripePriceId())) {
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, PA$USR$0003);
        }
        newPackage.setStripePriceId(packageDto.getStripePriceId());
        CountriesEntity country = countryRepo.findByIsoCode(packageDto.getCurrencyIso());
        if (country == null) {
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, PA$CUR$0002, packageDto.getCurrencyIso());
        }
        newPackage.setCountry(country);
        Set<ServiceEntity> serviceEntitySet = new HashSet<>();
        if (Collections.isEmpty(packageDto.getServiceIds())) {
            List<ServiceEntity> serviceEntities =
                    serviceRepository.findByIdIn(packageDto.getServiceIds());
            newPackage.setServices(new HashSet<>(serviceEntities));
        }
        newPackage.setServices(serviceEntitySet);
        newPackage = packageRepository.save(newPackage);
        return PackageResponse.fromPackageEntity(newPackage);
    }

    @Override
    public PackageResponse updatePackage(PackageDTO packageDto, Long packageId) {
        PackageEntity entity = packageRepository.findById(packageId).orElseThrow(
                () -> new RuntimeBusinessException(NOT_FOUND, PA$USR$0002, packageId)
        );
        if (StringUtils.isNotBlankOrNull(packageDto.getName())) {
            entity.setName(packageDto.getName());
        }
        if (StringUtils.isNotBlankOrNull(packageDto.getDescription())) {
            entity.setDescription(packageDto.getDescription());
        }
        if (StringUtils.isNotBlankOrNull(packageDto.getPrice())) {
            entity.setPrice(packageDto.getPrice());
        }
        if (StringUtils.isNotBlankOrNull(packageDto.getCurrencyIso())) {
            CountriesEntity country = countryRepo.findByIsoCode(packageDto.getCurrencyIso());
            if (country == null) {
                throw new RuntimeBusinessException(NOT_ACCEPTABLE, PA$CUR$0002, packageDto.getCurrencyIso());
            }
            entity.setCountry(country);
        }
        if (StringUtils.isNotBlankOrNull(packageDto.getPeriodInDays())) {
            entity.setPeriodInDays(packageDto.getPeriodInDays());
        }
        if (StringUtils.isNotBlankOrNull(packageDto.getStripePriceId())) {
            entity.setStripePriceId(packageDto.getStripePriceId());
        }
        if (!Collections.isEmpty(packageDto.getServiceIds())) {
            List<Long> serviceIds = packageDto.getServiceIds();
            List<ServiceEntity> serviceEntities = serviceRepository.findByIdIn(serviceIds);
            entity.setServices(new HashSet<>(serviceEntities));
        }
        packageRepository.save(entity);
        return PackageResponse.fromPackageEntity(entity);
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public void deletePackage(Long packageId) {
        PackageEntity entity = packageRepository.findById(packageId).orElseThrow(
                () -> new RuntimeBusinessException(NOT_FOUND, PA$USR$0002, packageId)
        );
        packageRegisteredRepository.deleteByPackageEntity_Id(packageId);
        packageRepository.delete(entity);
    }

    @Transactional
    @Override
    public String registerPackage(PackageRegisterDTO packageRegisterDTO) {
        EmployeeUserEntity employee = securityService.getCurrentUserOptional().map(EmployeeUserEntity.class::cast).orElse(null);

        if (employee == null || !(securityService.currentUserHasRole(Roles.NASNAV_ADMIN) || securityService.currentUserHasRole(Roles.NASNAV_EMPLOYEE)
                || Objects.equals(employee.getOrganizationId(), packageRegisterDTO.getOrganizationId()))) {
            throw new RuntimeBusinessException(FORBIDDEN, UAUTH$0002);
        }

        PackageEntity packageEntity = packageRepository.findById(packageRegisterDTO.getPackageId()).orElseThrow(
                () -> new RuntimeBusinessException(NOT_FOUND, PA$USR$0002, packageRegisterDTO.getPackageId()));

        OrganizationEntity organization = orgRepo.findById(packageRegisterDTO.getOrganizationId()).orElseThrow(
                () -> new RuntimeBusinessException(NOT_FOUND, G$ORG$0001, packageRegisterDTO.getOrganizationId()));

        PackageRegisteredEntity packageRegisteredEntity = packageRegisteredRepository.findByOrganization(organization).orElseGet(() -> new PackageRegisteredEntity(organization));
        packageRegisteredEntity.setPackageEntity(packageEntity);
        packageRegisteredEntity.setCreatorEmployee(employee);
        packageRegisteredEntity.setRegisteredDate(new Date());
        packageRegisteredRepository.save(packageRegisteredEntity);
        return "Package Registered Successfully";
    }

    @Transactional
    @Override
    public String deregisterPackage(PackageRegisterDTO packageRegisterDTO) {
        EmployeeUserEntity employee = securityService.getCurrentUserOptional().map(EmployeeUserEntity.class::cast).orElse(null);

        if (employee == null || !(securityService.currentUserHasRole(Roles.NASNAV_ADMIN) || securityService.currentUserHasRole(Roles.NASNAV_EMPLOYEE)
                || Objects.equals(employee.getOrganizationId(), packageRegisterDTO.getOrganizationId()))) {
            throw new RuntimeBusinessException(FORBIDDEN, UAUTH$0002);
        }

        packageRegisteredRepository.deleteByOrganization_Id(packageRegisterDTO.getOrganizationId());
        return "Package Deregistered Successfully";
    }

    @Transactional
    @Override
    public Long getPackageIdRegisteredInOrg(UserEntity user) {
        if (user == null || user.getOrganizationId() == null) {
            return null;
        }
        Optional<OrganizationEntity> organizationEntity = orgRepo.findById(user.getOrganizationId());
        if (organizationEntity.isEmpty()) {
            return null;
        }

        PackageRegisteredEntity packageRegisteredEntity = packageRegisteredRepository.findByOrganization(organizationEntity.get()).orElse(null);
        if (packageRegisteredEntity != null && packageRegisteredEntity.getPackageEntity() != null) {
            return packageRegisteredEntity.getPackageEntity().getId();
        }
        return null;
    }

    @Transactional
    @Override
    public PackageEntity getPackageRegisteredInOrg(OrganizationEntity organization) {
        if (organization == null) {
            return null;
        }

        PackageRegisteredEntity packageRegisteredEntity = packageRegisteredRepository.findByOrganization(organization).orElse(null);
        if (packageRegisteredEntity != null && packageRegisteredEntity.getPackageEntity() != null) {
            return packageRegisteredEntity.getPackageEntity();
        }
        return null;
    }
}
