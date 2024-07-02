package com.nasnav.service.impl;

import com.nasnav.commons.utils.StringUtils;
import com.nasnav.dao.OrganizationRepository;
import com.nasnav.dao.OrganizationServicesRepository;
import com.nasnav.dao.ServiceRepository;
import com.nasnav.dto.OrganizationServicesDto;
import com.nasnav.dto.request.ServiceDTO;
import com.nasnav.dto.response.ServiceResponse;
import com.nasnav.exceptions.CustomException;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.OrganizationEntity;
import com.nasnav.persistence.OrganizationServicesEntity;
import com.nasnav.persistence.ServiceEntity;
import com.nasnav.service.SecurityService;
import com.nasnav.service.ServiceInterface;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.nasnav.exceptions.ErrorCodes.*;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@Log4j2
@RequiredArgsConstructor
public class ServiceImpl implements ServiceInterface {

    private final ServiceRepository serviceRepository;
    private final OrganizationServicesRepository organizationServicesRepository;
    private final SecurityService securityService;
    private final OrganizationRepository organizationRepository;

    @Override
    public ServiceResponse createService(ServiceDTO service) {
        ServiceEntity serviceEntity = new ServiceEntity();
        ServiceDTO.toEntity(service, serviceEntity);
        serviceEntity = serviceRepository.save(serviceEntity);

        return ServiceResponse.from(serviceEntity);
    }

    @Override
    public ServiceResponse updateService(Long serviceId, ServiceDTO service) {
        ServiceEntity serviceEntity = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new CustomException(PA$SRV$0002, serviceId, NOT_FOUND));
        ServiceDTO.toEntity(service, serviceEntity);
        serviceRepository.save(serviceEntity);

        return ServiceResponse.from(serviceEntity);
    }

    @Override
    public void deleteService(Long serviceId) {
        ServiceEntity serviceEntity = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new CustomException(PA$SRV$0002, serviceId, NOT_FOUND));
        serviceRepository.deletePackageServiceByServiceId(serviceId);
        serviceRepository.deleteServicePermissionsByServiceId(serviceId);
        serviceRepository.delete(serviceEntity);
    }

    @Override
    public ServiceResponse getService(Long serviceId) {
        ServiceEntity serviceEntity = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new CustomException(PA$SRV$0002, serviceId, NOT_FOUND));
        return ServiceResponse.from(serviceEntity);
    }

    @Override
    public List<ServiceResponse> getAllServices() {
        List<ServiceEntity> serviceEntities = serviceRepository.findAll();
        return serviceEntities.stream().map(ServiceResponse::from).toList();
    }

    @Override
    public List<ServiceResponse> getOrgServiceResponses(Long orgId) {
        if (StringUtils.isBlankOrNull(orgId)) {
            orgId = securityService.getCurrentUserOrganization().getId();
        }
        List<Long> serviceIds = organizationServicesRepository.findAllByOrgId(orgId).stream()
                .filter(OrganizationServicesEntity::getEnabled)
                .map(OrganizationServicesEntity::getServiceId)
                .toList();
        if (serviceIds.isEmpty()) {
            return new ArrayList<>();
        }
        return serviceRepository.findByIdIn(serviceIds).stream()
                .map(ServiceResponse::from).toList();
    }

    @Override
    public List<OrganizationServicesDto> getOrgServices(Long orgId, Long serviceId) {
        List<OrganizationServicesEntity> entities;

        if (StringUtils.isBlankOrNull(orgId) && StringUtils.isBlankOrNull(serviceId)) {
            throw new RuntimeBusinessException(NOT_FOUND, PA$SRV$0002, "");
        } else if (StringUtils.isBlankOrNull(orgId) && StringUtils.isNotBlankOrNull(serviceId)) {
            entities = organizationServicesRepository.findAllByServiceId(serviceId);
        } else if (StringUtils.isNotBlankOrNull(orgId) && StringUtils.isBlankOrNull(serviceId)) {
            entities = organizationServicesRepository.findAllByOrgId(orgId);
        } else {
            entities = organizationServicesRepository.findAllByOrgIdAndServiceId(orgId, serviceId);
        }

        Set<Long> orgIds = entities.stream()
                .map(OrganizationServicesEntity::getOrgId)
                .collect(Collectors.toSet());

        Map<Long, String> orgIdToNameMap = organizationRepository.findAllById(orgIds).stream()
                .collect(Collectors.toMap(OrganizationEntity::getId, OrganizationEntity::getName));

        return entities.stream()
                .filter(entity -> entity.getEnabled() != null)
                .map(entity -> new OrganizationServicesDto(entity.getOrgId(), entity.getServiceId(), entity.getEnabled(),
                        orgIdToNameMap.getOrDefault(entity.getOrgId(), "")))
                .toList();
    }

    @Override
    public void updateOrgService(OrganizationServicesDto requestDto) {
        OrganizationServicesEntity entity = organizationServicesRepository.getByOrgIdAndServiceId(requestDto.getOrgId(), requestDto.getServiceId());
        if (entity == null) {
            entity = new OrganizationServicesEntity(requestDto.getOrgId(), requestDto.getServiceId(), requestDto.getEnabled());
        }
        entity.setEnabled(requestDto.getEnabled());
        organizationServicesRepository.save(entity);
    }

    @Override
    public void enablePackageServicesForOrganization(Long packageId, Long orgId) {
        Set<ServiceEntity> services = serviceRepository.findAllByPackageEntity_Id(packageId);
        if (services.isEmpty()) {
            return;
        }
        List<OrganizationServicesEntity> entities = new ArrayList<>();
        services.stream().map(service -> new OrganizationServicesEntity(orgId, service.getId(), service.getEnabled()))
                .forEach(entities::add);
        organizationServicesRepository.saveAll(entities);
    }
}
