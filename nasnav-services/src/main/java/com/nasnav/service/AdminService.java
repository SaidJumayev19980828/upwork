package com.nasnav.service;

import com.nasnav.dto.request.ServiceRegisteredByUserDTO;
import com.nasnav.persistence.ServiceRegisteredByUser;

public interface AdminService {

  void invalidateCaches();

  ServiceRegisteredByUser completeProfile(ServiceRegisteredByUserDTO serviceRegisteredByUserDTO);



}