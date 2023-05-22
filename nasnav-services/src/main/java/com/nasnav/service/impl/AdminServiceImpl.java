package com.nasnav.service.impl;

import com.nasnav.dao.ServiceRegisteredByUserRepository;
import com.nasnav.dao.ServiceRepository;
import com.nasnav.dto.request.ServiceRegisteredByUserDTO;
import com.nasnav.persistence.ServiceRegisteredEntity;
import com.nasnav.persistence.Services;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import com.nasnav.service.AdminService;

import java.util.Date;


@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    ServiceRegisteredByUserRepository serviceRegisteredByUserRepository ;

    @Autowired
    ServiceRepository serviceRepository ;
    @Override
    public void invalidateCaches() {
        cacheManager.getCacheNames()
                    .stream()
                    .forEach(cacheName -> cacheManager.getCache(cacheName).clear());
    }

    @Override
    public ServiceRegisteredEntity completeProfile(ServiceRegisteredByUserDTO serviceRegisteredByUserDTO) {

         Services services = serviceRepository.findById(serviceRegisteredByUserDTO.getServiceId()).get();

         ServiceRegisteredEntity serviceRegisteredEntity = ServiceRegisteredEntity.builder().services(services).userId(serviceRegisteredByUserDTO.getUserId()).registeredDate(new Date()).build();

        return serviceRegisteredByUserRepository.save(serviceRegisteredEntity);
    }


}
