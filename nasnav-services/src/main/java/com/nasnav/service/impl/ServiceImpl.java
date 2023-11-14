package com.nasnav.service.impl;

import com.nasnav.dao.OrganizationServicesRepository;
import com.nasnav.dao.ServiceRepository;
import com.nasnav.dao.ThemeClassRepository;
import com.nasnav.dto.OrganizationServicesDto;
import com.nasnav.dto.request.ServiceDTO;
import com.nasnav.dto.response.ServiceResponse;
import com.nasnav.exceptions.CustomException;
import com.nasnav.mappers.ServiceMapper;
import com.nasnav.persistence.OrganizationEntity;
import com.nasnav.persistence.OrganizationServicesEntity;
import com.nasnav.persistence.ServiceEntity;
import com.nasnav.persistence.ServiceInstanceEntity;
import com.nasnav.service.OrganizationService;
import com.nasnav.service.SecurityService;
import com.nasnav.service.ServiceInterface;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.nasnav.exceptions.ErrorCodes.*;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@Log4j2
@RequiredArgsConstructor
public class ServiceImpl implements ServiceInterface {

    private final ServiceRepository serviceRepository;
    private final OrganizationServicesRepository organizationServicesRepository;
    private final SecurityService securityService;

    @Override
    public ServiceResponse createService(ServiceDTO service) {
        ServiceEntity serviceEntity = new ServiceEntity();
        serviceEntity.setCode(service.getCode());
        serviceEntity.setName(service.getName());
        serviceEntity.setDescription(service.getDescription());
        serviceEntity.setLightLogo(service.getLightLogo());
        serviceEntity.setDarkLogo(serviceEntity.getDarkLogo());
        if (service.getEnabled() != null){
            serviceEntity.setEnabled(service.getEnabled());
        }
        serviceRepository.save(serviceEntity);

        return ServiceMapper.INSTANCE.entityToDto(serviceEntity);
    }

    @Override
    public ServiceResponse updateService(Long serviceId, ServiceDTO service) {
        ServiceEntity serviceEntity = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new CustomException(PA$SRV$0002, serviceId, NOT_FOUND));
        ServiceEntity updatedEntity;
        if (service.getEnabled() == null){
            throw new CustomException(PA$SRV$0003.getValue(), BAD_REQUEST);
        }
        updatedEntity =ServiceMapper.INSTANCE.dtoToEntity(service);
        updatedEntity.setId(serviceEntity.getId());
        updatedEntity.setPackageEntity(serviceEntity.getPackageEntity());

        serviceRepository.save(updatedEntity);

        return ServiceMapper.INSTANCE.entityToDto(updatedEntity);
    }

    @Override
    public void deleteService(Long serviceId) {
        ServiceEntity serviceEntity = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new CustomException(PA$SRV$0002, serviceId, NOT_FOUND));
        serviceRepository.delete(serviceEntity);
    }

    @Override
    public ServiceResponse getService(Long serviceId) {
        ServiceEntity serviceEntity = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new CustomException(PA$SRV$0002, serviceId, NOT_FOUND));
        return ServiceMapper.INSTANCE.entityToDto(serviceEntity);
    }

    @Override
    public List<ServiceResponse> getALlServices() {
        List<ServiceEntity> serviceEntities = serviceRepository.findAll();
        return ServiceMapper.INSTANCE.entitiesToBeansWithoutList(serviceEntities);
    }

    @Override
    public List<OrganizationServicesDto> getOrgServices() {
        OrganizationEntity org = securityService.getCurrentUserOrganization();
        if (Objects.isNull(org.getPackageRegistration())){
            throw new CustomException(ORG$SUB$0001, NOT_FOUND);
        }
        List<OrganizationServicesEntity> entities =
                organizationServicesRepository.findAllByOrgId(org.getId());

        if (entities.isEmpty()){
            Set<ServiceEntity> services = org.getPackageRegistration().getPackageEntity().getServices();
            if (services.isEmpty()){
                throw new CustomException(PA$SRV$0004, org.getId(), NOT_FOUND);
            }
            for (ServiceEntity service: services){
                entities.add(
                        OrganizationServicesEntity
                                .builder()
                                .orgId(org.getPackageRegistration().getPackageEntity().getId())
                                .serviceId(service.getId())
                                .enabled(true)
                                .build()
                );
            }
            organizationServicesRepository.saveAll(entities);

        }

        return ServiceMapper.INSTANCE.orgToBeansWithoutList(entities);
    }

    @Override
    public List<OrganizationServicesDto> updateOrgService(List<OrganizationServicesDto> request) {

        OrganizationEntity org = securityService.getCurrentUserOrganization();
        if (Objects.isNull(org.getPackageRegistration())){
            throw new CustomException(ORG$SUB$0001, NOT_FOUND);
        }
        organizationServicesRepository.deleteAllByOrgId(org.getId());

        List<OrganizationServicesEntity> entities = new ArrayList<>();
        Set<ServiceEntity> services = org.getPackageRegistration().getPackageEntity().getServices();

        if (services.isEmpty()){
            throw new CustomException(PA$SRV$0004, org.getId(), NOT_FOUND);
        }

        for (OrganizationServicesDto orgService: request){
            entities.add(
                    OrganizationServicesEntity
                            .builder()
                            .orgId(org.getPackageRegistration().getPackageEntity().getId())
                            .serviceId(orgService.getServiceId())
                            .enabled(orgService.getEnabled())
                            .build()
            );
        }

        organizationServicesRepository.saveAll(entities);

        return ServiceMapper.INSTANCE.orgToBeansWithoutList(entities);
    }

}
