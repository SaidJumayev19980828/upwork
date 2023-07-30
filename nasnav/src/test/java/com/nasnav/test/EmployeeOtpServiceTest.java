package com.nasnav.test;

import java.util.Date;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.AdditionalAnswers;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.nasnav.AppConfig;
import com.nasnav.dao.EmployeeUserOtpRepository;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.EmployeeUserEntity;
import com.nasnav.persistence.EmployeeUserOtpEntity;
import com.nasnav.persistence.Role;
import com.nasnav.persistence.UserEntity;
import com.nasnav.persistence.UserOtpEntity;
import com.nasnav.service.otp.EmployeeOtpService;
import com.nasnav.service.otp.OtpType;
import com.nasnav.test.commons.test_templates.AbstractTestWithTempBaseDir;
import com.nasnav.util.RandomGenerator;

import static java.util.Set.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EmployeeOtpServiceTest extends AbstractTestWithTempBaseDir {
    @MockBean
    private EmployeeUserOtpRepository employeeUserOtpRepository;

    @Autowired
    private EmployeeOtpService employeeOtpService;

    @Autowired
    private AppConfig appConfig;

    EmployeeUserEntity userEntity = buildEmployeeUserEntity();

    EmployeeUserOtpEntity userOtpEntity;

    @BeforeEach
    void reInit() {
        userOtpEntity = buildEmployeeUserOtpEntity();
        Mockito.when(employeeUserOtpRepository.save(any(EmployeeUserOtpEntity.class))).then(AdditionalAnswers.returnsFirstArg());
        Mockito.when(employeeUserOtpRepository.findByUserAndType(any(EmployeeUserEntity.class), any(OtpType.class)))
                .thenReturn(Optional.of(userOtpEntity));
    }

    @Test
    void createUserOtp() {
        EmployeeUserOtpEntity userOtp = employeeOtpService.createUserOtp(userEntity, userOtpEntity.getType());
        assertNotNull(userOtp);
        assertEquals(userOtp.getOtp().length(), appConfig.otpLength);
    }

    @Test
    void validateOtpWithValidOtp() {
        employeeOtpService.validateOtp(userOtpEntity.getOtp(), userEntity, userOtpEntity.getType());
        Mockito.verify(employeeUserOtpRepository, Mockito.times(1)).delete(userOtpEntity);
    }

    @Test
    void validateOtpWithInvalidOtp() {
        EmployeeUserEntity userEntity = buildEmployeeUserEntity();
        EmployeeUserOtpEntity userOtpEntity = buildEmployeeUserOtpEntity();
        Mockito.when(employeeUserOtpRepository.findByUserAndType(userEntity, userOtpEntity.getType())).thenReturn(Optional.of(userOtpEntity));
        String invalidOtp = "invalid otp";
        OtpType type = userOtpEntity.getType();
        for (int i = 0; i < appConfig.otpMaxRetries - 1; i++) {
            Assertions.assertThrows(RuntimeBusinessException.class, () ->
                employeeOtpService.validateOtp(invalidOtp, userEntity, type));
            Mockito.verify(employeeUserOtpRepository, Mockito.never()).delete(userOtpEntity);
        }
        Assertions.assertThrows(RuntimeBusinessException.class,
                () -> employeeOtpService.validateOtp(invalidOtp, userEntity, type));
        Mockito.verify(employeeUserOtpRepository, Mockito.times(1)).delete(userOtpEntity);
    }

    private EmployeeUserOtpEntity buildEmployeeUserOtpEntity() {
        EmployeeUserOtpEntity userOtpEntity = new EmployeeUserOtpEntity();
        userOtpEntity.setUser(buildEmployeeUserEntity());
        userOtpEntity.setOtp(RandomGenerator.randomNumber(6));
        userOtpEntity.setType(OtpType.REGISTER);
        userOtpEntity.setCreatedAt(new Date());
        userOtpEntity.setId(1L);
        userOtpEntity.setAttempts(0L);
        return userOtpEntity;
    }

    private EmployeeUserEntity buildEmployeeUserEntity() {
        EmployeeUserEntity userEntity = new EmployeeUserEntity();

        userEntity.setId(1L);
        userEntity.setName("Ahmed Eid");
        userEntity.setEmail("test@Mail.com");
        userEntity.setOrganizationId(23L);
        userEntity.setUserStatus(201);
        Role role = new Role();
        role.setId(1);
        role.setName("NAVNAS_ADMIN");
        role.setEmployees(of(userEntity));
        userEntity.setRoles(of(role));
        return userEntity;
    }

}
