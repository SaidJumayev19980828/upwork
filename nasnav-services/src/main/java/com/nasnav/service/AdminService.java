package com.nasnav.service;

import com.nasnav.dto.request.ServiceRegisteredByUserDTO;
import com.nasnav.persistence.ServiceRegisteredEntity;

public interface AdminService {

  void invalidateCaches();

  ServiceRegisteredEntity completeProfile(ServiceRegisteredByUserDTO serviceRegisteredByUserDTO);



}