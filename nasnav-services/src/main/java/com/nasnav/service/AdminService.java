package com.nasnav.service;

import com.nasnav.dto.request.PackageRegisteredByUserDTO;
import com.nasnav.persistence.PackageRegisteredEntity;

public interface AdminService {

  void invalidateCaches();

}