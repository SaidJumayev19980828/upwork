package com.nasnav.test;

import com.nasnav.NavBox;
import com.nasnav.dao.UserOTPRepository;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.UserEntity;
import com.nasnav.persistence.UserOtpEntity;
import com.nasnav.service.OTP.OTPService;
import com.nasnav.service.OTP.OTPType;
import com.nasnav.util.RandomGenerator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.AdditionalAnswers;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.PropertySource;

import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@SpringBootTest
@AutoConfigureWebTestClient
@PropertySource("classpath:test.database.properties")
public class OTPServiceTest {
    @MockBean
    private UserOTPRepository userOTPRepository;

    @Autowired
    private OTPService otpService;

    @Value("${otp.max-retries:3}")
    private long maxRetries;

    @Value("${otp.length:6}")
    private int otpLength;

    UserEntity userEntity = buildUserEntity();

    UserOtpEntity userOtpEntity;

    @BeforeEach
    void reInit() {
        userOtpEntity = buildUserOTPEntity();
        Mockito.when(userOTPRepository.save(any(UserOtpEntity.class))).then(AdditionalAnswers.returnsFirstArg());
        Mockito.when(userOTPRepository.findByUserAndType(any(UserEntity.class), any(OTPType.class)))
                .thenReturn(Optional.of(userOtpEntity));
    }

    @Test
    void createUserOTP() {
        UserOtpEntity userOTP = otpService.createUserOTP(userEntity, userOtpEntity.getType());
        assertNotNull(userOTP);
        assertEquals(userOTP.getOtp().length(), otpLength);
    }

    @Test
    void validateOTPWithValidOTP() {
        otpService.validateOtp(userOtpEntity.getOtp(), userEntity, userOtpEntity.getType());
        Mockito.verify(userOTPRepository, Mockito.times(1)).delete(userOtpEntity);
    }

    @Test
    void validateOTPWithInvalidOTP() {
        UserEntity userEntity = buildUserEntity();
        UserOtpEntity userOtpEntity = buildUserOTPEntity();
        Mockito.when(userOTPRepository.findByUserAndType(userEntity, userOtpEntity.getType())).thenReturn(Optional.of(userOtpEntity));
        String invalidOTP = "invalid otp";
        OTPType type = userOtpEntity.getType();
        for (int i = 0; i < maxRetries - 1; i++) {
            Assertions.assertThrows(RuntimeBusinessException.class, () ->
                otpService.validateOtp(invalidOTP, userEntity, type));
            Mockito.verify(userOTPRepository, Mockito.never()).delete(userOtpEntity);
        }
        Assertions.assertThrows(RuntimeBusinessException.class,
                () -> otpService.validateOtp(invalidOTP, userEntity, type));
        Mockito.verify(userOTPRepository, Mockito.times(1)).delete(userOtpEntity);
    }

    private UserOtpEntity buildUserOTPEntity() {
        UserOtpEntity userOtpEntity = new UserOtpEntity();
        userOtpEntity.setUser(buildUserEntity());
        userOtpEntity.setOtp(RandomGenerator.randomNumber(6));
        userOtpEntity.setType(OTPType.REGISTER);
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
