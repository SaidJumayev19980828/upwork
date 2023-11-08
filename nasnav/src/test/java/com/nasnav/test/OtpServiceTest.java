package com.nasnav.test;

import com.nasnav.AppConfig;
import com.nasnav.dao.UserOtpRepository;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.UserEntity;
import com.nasnav.persistence.UserOtpEntity;
import com.nasnav.service.otp.OtpService;
import com.nasnav.service.otp.OtpType;
import com.nasnav.test.commons.test_templates.AbstractTestWithTempBaseDir;
import com.nasnav.util.RandomGenerator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.AdditionalAnswers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OtpServiceTest {
    @Mock
    private UserOtpRepository userOtpRepository;

    private OtpService otpService;

    private static AppConfig appConfig = new AppConfig(false);

    UserEntity userEntity = buildUserEntity();

    UserOtpEntity userOtpEntity;

    @BeforeAll
    static void init() {
        appConfig.otpLength = 6;
        appConfig.otpMaxRetries = 3;
        appConfig.otpValidDurationInSeconds = 600;
    }

    @BeforeEach
    void reInit() {
        otpService = new OtpService(userOtpRepository, appConfig);
        userOtpEntity = buildUserOtpEntity();
        Mockito.when(userOtpRepository.save(any(UserOtpEntity.class))).then(AdditionalAnswers.returnsFirstArg());
        Mockito.when(userOtpRepository.findByUserAndType(any(UserEntity.class), any(OtpType.class)))
                .thenReturn(Optional.of(userOtpEntity));
    }

    @Test
    void createUserOtp() {
        UserOtpEntity userOtp = otpService.createUserOtp(userEntity, userOtpEntity.getType());
        assertNotNull(userOtp);
        assertEquals(userOtp.getOtp().length(), appConfig.otpLength);
    }

    @Test
    void validateOtpWithValidOtp() {
        otpService.validateOtp(userOtpEntity.getOtp(), userEntity, userOtpEntity.getType());
        Mockito.verify(userOtpRepository, Mockito.times(1)).delete(userOtpEntity);
    }

    @Test
    void validateOtpWithInvalidOtp() {
        UserEntity userEntity = buildUserEntity();
        UserOtpEntity userOtpEntity = buildUserOtpEntity();
        Mockito.when(userOtpRepository.findByUserAndType(userEntity, userOtpEntity.getType())).thenReturn(Optional.of(userOtpEntity));
        String invalidOtp = "invalid otp";
        OtpType type = userOtpEntity.getType();
        for (int i = 0; i < appConfig.otpMaxRetries - 1; i++) {
            Assertions.assertThrows(RuntimeBusinessException.class, () ->
                otpService.validateOtp(invalidOtp, userEntity, type));
            Mockito.verify(userOtpRepository, Mockito.never()).delete(userOtpEntity);
        }
        Assertions.assertThrows(RuntimeBusinessException.class,
                () -> otpService.validateOtp(invalidOtp, userEntity, type));
        Mockito.verify(userOtpRepository, Mockito.times(1)).delete(userOtpEntity);
    }

    private UserOtpEntity buildUserOtpEntity() {
        UserOtpEntity userOtpEntity = new UserOtpEntity();
        userOtpEntity.setUser(buildUserEntity());
        userOtpEntity.setOtp(RandomGenerator.randomNumber(6));
        userOtpEntity.setType(OtpType.REGISTER);
        userOtpEntity.setCreatedAt(new Date());
        userOtpEntity.setId(1L);
        userOtpEntity.setAttempts(0L);
        return userOtpEntity;
    }

    private UserEntity buildUserEntity() {
        UserEntity userEntity = new UserEntity();

        userEntity.setId(1L);
        userEntity.setFirstName("John");
        userEntity.setLastName("Deo");
        userEntity.setEmail("test@Mail.com");
        userEntity.setOrganizationId(23L);
        userEntity.setUserStatus(201);
        return userEntity;
    }

}
