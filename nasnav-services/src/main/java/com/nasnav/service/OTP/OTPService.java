package com.nasnav.service.OTP;

import com.nasnav.dao.UserOTPRepository;
import com.nasnav.exceptions.ErrorCodes;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.UserEntity;
import com.nasnav.persistence.UserOtpEntity;
import com.nasnav.util.RandomGenerator;

import lombok.RequiredArgsConstructor;

import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@RequiredArgsConstructor
@Service
public class OTPService {

    private final UserOTPRepository userOTPRepository;

    @Value("${otp.valid-duration-in-seconds:600}")
    private int validDurationInSeconds;

    @Value("${otp.length:6}")
    private int otpLength;

    @Value("${otp.max-retries:3}")
    private long maxRetries;

    @Transactional
    public UserOtpEntity createUserOTP(UserEntity user, OTPType otpType) {
        userOTPRepository.deleteByUser(user);
        UserOtpEntity userOtpEntity = new UserOtpEntity();
        userOtpEntity.setUser(user);
        userOtpEntity.setOtp(RandomGenerator.randomNumber(otpLength));
        userOtpEntity.setType(otpType);
        userOtpEntity.setCreatedAt(new Date());
        return userOTPRepository.save(userOtpEntity);
    }

    public void validateOtp(String otp, UserEntity user, OTPType otpType) {

        UserOtpEntity userOTP = getUserOTP(user, otpType);

        Date currentDate = new Date();

        Date expiration = DateUtils.addSeconds(userOTP.getCreatedAt(), validDurationInSeconds);

        Long attempts = userOTP.incrementAttempts();
        userOTP = userOTPRepository.save(userOTP);

        if (userOTP.getOtp().equals(otp)
                && attempts <= maxRetries
                && currentDate.after(userOTP.getCreatedAt())
                && currentDate.before(expiration)) {
            userOTPRepository.delete(userOTP);
        } else {
            if (attempts >= maxRetries) userOTPRepository.delete(userOTP);
            throw new RuntimeBusinessException(ErrorCodes.OTP$INVALID.getValue(), HttpStatus.BAD_REQUEST.name(), HttpStatus.BAD_REQUEST);
        }
    }

    public UserOtpEntity getUserOTP(UserEntity user, OTPType type) {
        return userOTPRepository.findByUserAndType(user, type).
                orElseThrow(() -> new RuntimeBusinessException(ErrorCodes.OTP$NOTFOUND.getValue(), HttpStatus.BAD_REQUEST.name(), HttpStatus.BAD_REQUEST));
    }
}
