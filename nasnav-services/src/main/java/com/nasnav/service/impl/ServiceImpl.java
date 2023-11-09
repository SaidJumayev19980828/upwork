package com.nasnav.service.impl;

import com.nasnav.dao.ServiceRepository;
import com.nasnav.dto.request.ServiceDTO;
import com.nasnav.dto.response.ServiceResponse;
import com.nasnav.exceptions.CustomException;
import com.nasnav.mappers.ServiceMapper;
import com.nasnav.persistence.ServiceEntity;
import com.nasnav.service.ServiceInterface;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.nasnav.exceptions.ErrorCodes.PA$SRV$0002;
import static com.nasnav.exceptions.ErrorCodes.PA$SRV$0003;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@Log4j2
@RequiredArgsConstructor
public class ServiceImpl implements ServiceInterface {

    private final ServiceRepository serviceRepository;
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
        ServiceEntity updatedEntity = new ServiceEntity();
        updatedEntity.setId(serviceEntity.getId());
        if (service.getEnabled() == null){
            throw new CustomException(PA$SRV$0003.getValue(), BAD_REQUEST);
        }
        updatedEntity =ServiceMapper.INSTANCE.dtoToEntity(service);
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
}
