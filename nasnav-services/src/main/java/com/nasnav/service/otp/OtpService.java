package com.nasnav.service.otp;

import org.springframework.stereotype.Service;

import com.nasnav.AppConfig;
import com.nasnav.dao.UserOtpRepository;
import com.nasnav.persistence.UserEntity;
import com.nasnav.persistence.UserOtpEntity;

@Service
public class OtpService extends BaseOtpService<UserEntity, UserOtpEntity, UserOtpRepository> {

  public OtpService(UserOtpRepository otpRepository, AppConfig config) {
    super(otpRepository, config);
  }

  @Override
  protected UserOtpEntity createEntity() {
    return new UserOtpEntity();
  }
}
