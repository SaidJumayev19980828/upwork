package com.nasnav.persistence;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "employee_user_otp")
public class EmployeeUserOtpEntity extends BaseUserOtpEntity<EmployeeUserEntity> {

}
