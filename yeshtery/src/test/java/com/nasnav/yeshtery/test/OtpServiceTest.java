package com.nasnav.yeshtery.test;

import com.nasnav.AppConfig;
import com.nasnav.dao.yeshtery.YeshteryUserOtpRepository;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.yeshtery.YeshteryUserEntity;
import com.nasnav.persistence.yeshtery.YeshteryUserOtpEntity;
import com.nasnav.service.otp.OtpType;
import com.nasnav.service.yeshtery.YeshteryOtpService;
import com.nasnav.util.RandomGenerator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.AdditionalAnswers;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.beans.factory.annotation.Autowired;
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
class OtpServiceTest {
    @MockBean
    private YeshteryUserOtpRepository yeshteryUserOtpRepository;

    @Autowired
    private YeshteryOtpService yeshteryOtpService;

    @Autowired
    private AppConfig appConfig;

    YeshteryUserEntity userEntity = buildUserEntity();

    YeshteryUserOtpEntity yeshteryUserOtpEntity;

    @BeforeEach
    void reInit() {
        yeshteryUserOtpEntity = buildUserOtpEntity();
        Mockito.when(yeshteryUserOtpRepository.save(any(YeshteryUserOtpEntity.class))).then(AdditionalAnswers.returnsFirstArg());
        Mockito.when(yeshteryUserOtpRepository.findByUserAndType(any(YeshteryUserEntity.class), any(OtpType.class)))
                .thenReturn(Optional.of(yeshteryUserOtpEntity));
    }

    @Test
    void createUserOtp() {
        YeshteryUserOtpEntity userOtp = yeshteryOtpService.createUserOtp(userEntity, yeshteryUserOtpEntity.getType());
        assertNotNull(userOtp);
        assertEquals(userOtp.getOtp().length(), appConfig.otpLength);
    }

    @Test
    void validateOtpWithValidOtp() {
        yeshteryOtpService.validateOtp(yeshteryUserOtpEntity.getOtp(), userEntity, yeshteryUserOtpEntity.getType());
        Mockito.verify(yeshteryUserOtpRepository, Mockito.times(1)).delete(yeshteryUserOtpEntity);
    }

    @Test
    void validateOtpWithInvalidOtp() {
        YeshteryUserEntity userEntity = buildUserEntity();
        YeshteryUserOtpEntity userOtpEntity = buildUserOtpEntity();
        Mockito.when(yeshteryUserOtpRepository.findByUserAndType(userEntity, userOtpEntity.getType())).thenReturn(Optional.of(userOtpEntity));
        String invalidOtp = "invalid otp";
        OtpType type = userOtpEntity.getType();
        for (int i = 0; i < appConfig.otpMaxRetries - 1; i++) {
            Assertions.assertThrows(RuntimeBusinessException.class, () ->
                yeshteryOtpService.validateOtp(invalidOtp, userEntity, type));
            Mockito.verify(yeshteryUserOtpRepository, Mockito.never()).delete(userOtpEntity);
        }
        Assertions.assertThrows(RuntimeBusinessException.class,
                () -> yeshteryOtpService.validateOtp(invalidOtp, userEntity, type));
        Mockito.verify(yeshteryUserOtpRepository, Mockito.times(1)).delete(userOtpEntity);
    }

    private YeshteryUserOtpEntity buildUserOtpEntity() {
        YeshteryUserOtpEntity userOtpEntity = new YeshteryUserOtpEntity();
        userOtpEntity.setUser(buildUserEntity());
        userOtpEntity.setOtp(RandomGenerator.randomNumber(6));
        userOtpEntity.setType(OtpType.REGISTER);
        userOtpEntity.setCreatedAt(new Date());
        userOtpEntity.setId(1L);
        userOtpEntity.setAttempts(0L);
        return userOtpEntity;
    }

    private YeshteryUserEntity buildUserEntity() {
        YeshteryUserEntity userEntity = new YeshteryUserEntity();

        userEntity.setId(1L);
        userEntity.setFirstName("John");
        userEntity.setLastName("Deo");
        userEntity.setEmail("test@Mail.com");
        userEntity.setOrganizationId(23L);
        userEntity.setUserStatus(201);
        return userEntity;
    }

}
