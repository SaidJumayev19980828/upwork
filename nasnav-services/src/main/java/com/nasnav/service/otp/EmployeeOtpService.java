package com.nasnav.service.otp;

import org.springframework.stereotype.Service;

import com.nasnav.AppConfig;
import com.nasnav.dao.EmployeeUserOtpRepository;
import com.nasnav.persistence.EmployeeUserEntity;
import com.nasnav.persistence.EmployeeUserOtpEntity;

@Service
public class EmployeeOtpService extends BaseOtpService<EmployeeUserEntity,
        EmployeeUserOtpEntity,
        EmployeeUserOtpRepository> {

  public EmployeeOtpService(EmployeeUserOtpRepository otpRepository, AppConfig config) {
    super(otpRepository, config);
  }

  @Override
  protected EmployeeUserOtpEntity createEntity() {
    return new EmployeeUserOtpEntity();
  }
}
