package com.nasnav.service.yeshtery;

import org.springframework.stereotype.Service;

import com.nasnav.AppConfig;
import com.nasnav.dao.yeshtery.YeshteryUserOtpRepository;
import com.nasnav.persistence.yeshtery.YeshteryUserEntity;
import com.nasnav.persistence.yeshtery.YeshteryUserOtpEntity;
import com.nasnav.service.otp.BaseOtpService;

@Service
public class YeshteryOtpService extends BaseOtpService<YeshteryUserEntity, YeshteryUserOtpEntity, YeshteryUserOtpRepository> {

  public YeshteryOtpService(YeshteryUserOtpRepository otpRepository, AppConfig config) {
    super(otpRepository, config);
  }

  @Override
  protected YeshteryUserOtpEntity createEntity() {
    return new YeshteryUserOtpEntity();
  }
  
}
