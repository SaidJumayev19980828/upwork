package com.nasnav.test;

import com.nasnav.dao.UserOTPRepository;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.UserEntity;
import com.nasnav.persistence.UserOtpEntity;
import com.nasnav.service.OTP.OTPService;
import com.nasnav.service.OTP.OTPType;
import com.nasnav.util.RandomGenerator;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Date;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class OTPServiceTest {

    private UserOTPRepository userOTPRepository;

    private OTPService otpService;

    @Before
    public void init() {
        userOTPRepository = Mockito.mock(UserOTPRepository.class);
        otpService = Mockito.mock(OTPService.class);
    }

    @Test
    public void createUserOTP() {
        UserEntity userEntity = buildUserEntity();
        UserOtpEntity userOtpEntity = buildUserOTPEntity();
        Mockito.when(userOTPRepository.findByUser(userEntity)).thenReturn(Optional.of(userOtpEntity));
        Mockito.when(userOTPRepository.save(userOtpEntity)).thenReturn(userOtpEntity);
        Mockito.doNothing().when(userOTPRepository).deleteByOtpAndUser(userOtpEntity.getOtp(), userEntity);
        Mockito.when(otpService.createUserOTP(userEntity, userOtpEntity.getType())).thenReturn(userOtpEntity);
        UserOtpEntity userOTP = otpService.createUserOTP(userEntity, userOtpEntity.getType());
        assertThat(userOTP).isNotNull();
        assertThat(userOTP.getOtp()).isEqualTo(userOtpEntity.getOtp());
    }

    @Test
    public void validateOTP_withValidOTP() {
        UserEntity userEntity = buildUserEntity();
        UserOtpEntity userOtpEntity = buildUserOTPEntity();
        Mockito.when(userOTPRepository.findByUserAndOtp(userEntity, userOtpEntity.getOtp())).thenReturn(Optional.of(userOtpEntity));
        otpService.validateOtp(userOtpEntity.getOtp(), userEntity, userOtpEntity.getType());
        Mockito.verify(otpService, Mockito.times(1)).validateOtp(userOtpEntity.getOtp(), userEntity, userOtpEntity.getType());
    }

    @Test
    public void validateOTP_withInvalidOTP() {
        UserEntity userEntity = buildUserEntity();
        UserOtpEntity userOtpEntity = buildUserOTPEntity();
        Mockito.when(otpService.getUserOTP(userEntity, userOtpEntity.getOtp())).thenReturn(null);
        Mockito.doAnswer(invocationOnMock -> {
            throw new RuntimeBusinessException();
        }).when(otpService).validateOtp(userOtpEntity.getOtp(), userEntity, userOtpEntity.getType());
        Assertions.assertThrows(RuntimeBusinessException.class, () ->
                otpService.validateOtp(userOtpEntity.getOtp(), userEntity, userOtpEntity.getType()));
    }

    private UserOtpEntity buildUserOTPEntity() {
        UserOtpEntity userOtpEntity = new UserOtpEntity();
        userOtpEntity.setUser(buildUserEntity());
        userOtpEntity.setOtp(RandomGenerator.randomNumber(6));
        userOtpEntity.setType(OTPType.REGISTER);
        userOtpEntity.setCreatedAt(new Date());
        userOtpEntity.setId(1L);
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
