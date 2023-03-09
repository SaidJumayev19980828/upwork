package com.nasnav.persistence.yeshtery;

import javax.persistence.Entity;
import javax.persistence.Table;

import com.nasnav.persistence.BaseUserOtpEntity;

@Entity
@Table(name = "yeshtery_user_otp")
public class YeshteryUserOtpEntity extends BaseUserOtpEntity<YeshteryUserEntity> {

}
