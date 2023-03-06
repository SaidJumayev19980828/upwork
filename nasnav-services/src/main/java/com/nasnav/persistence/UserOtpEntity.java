package com.nasnav.persistence;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "user_otp")
public class UserOtpEntity extends BaseUserOtpEntity<UserEntity> {

}
