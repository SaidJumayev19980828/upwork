package com.nasnav.service.OTP;

import com.nasnav.AppConfig;
import com.nasnav.dao.UserOTPRepository;
import com.nasnav.exceptions.ErrorCodes;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.UserEntity;
import com.nasnav.persistence.UserOtpEntity;
import com.nasnav.util.RandomGenerator;

import lombok.RequiredArgsConstructor;

import org.apache.commons.lang3.time.DateUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@RequiredArgsConstructor
@Service
public class OTPService {

    private final UserOTPRepository userOTPRepository;

    private final AppConfig appConfig;

    @Transactional
    public UserOtpEntity createUserOTP(UserEntity user, OTPType otpType) {
        userOTPRepository.deleteByUser(user);
        UserOtpEntity userOtpEntity = new UserOtpEntity();
        userOtpEntity.setUser(user);
        userOtpEntity.setOtp(RandomGenerator.randomNumber(appConfig.otpLength));
        userOtpEntity.setType(otpType);
        userOtpEntity.setCreatedAt(new Date());
        return userOTPRepository.save(userOtpEntity);
    }

    // new transaction is needed to keep retries consistent in case of transaction or no transaction
    @Transactional(propagation = Propagation.REQUIRES_NEW, noRollbackFor = RuntimeBusinessException.class)
    public void validateOtp(String otp, UserEntity user, OTPType otpType) {

        UserOtpEntity userOTP = getUserOTP(user, otpType);

        Date currentDate = new Date();

        Date expiration = DateUtils.addSeconds(userOTP.getCreatedAt(), appConfig.otpValidDurationInSeconds);

        Long attempts = userOTP.incrementAttempts();
        userOTP = userOTPRepository.save(userOTP);

        if (userOTP.getOtp().equals(otp)
                && attempts <= appConfig.otpMaxRetries
                && currentDate.after(userOTP.getCreatedAt())
                && currentDate.before(expiration)) {
            userOTPRepository.delete(userOTP);
        } else {
            if (attempts >= appConfig.otpMaxRetries) userOTPRepository.delete(userOTP);
            throw new RuntimeBusinessException(ErrorCodes.OTP$INVALID.getValue(), HttpStatus.BAD_REQUEST.name(), HttpStatus.BAD_REQUEST);
        }
    }

    public UserOtpEntity getUserOTP(UserEntity user, OTPType type) {
        return userOTPRepository.findByUserAndType(user, type).
                orElseThrow(() -> new RuntimeBusinessException(ErrorCodes.OTP$NOTFOUND.getValue(), HttpStatus.BAD_REQUEST.name(), HttpStatus.BAD_REQUEST));
    }
}
