package com.nasnav.service.OTP;

import com.nasnav.dao.UserOTPRepository;
import com.nasnav.exceptions.ErrorCodes;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.UserEntity;
import com.nasnav.persistence.UserOtpEntity;
import com.nasnav.util.RandomGenerator;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Optional;

@Service
public class OTPService {

    private final UserOTPRepository userOTPRepository;

    @Value("${otp.valid-duration-in-seconds}")
    private int validDurationInSeconds;

    @Value("${otp.length}")
    private int otpLength;

    public OTPService(UserOTPRepository userOTPRepository) {
        this.userOTPRepository = userOTPRepository;
    }

    @Transactional
    public UserOtpEntity createUserOTP(UserEntity user, OTPType otpType) {
        Optional<UserOtpEntity> userOtp = userOTPRepository.findByUser(user);
        userOtp.ifPresent(userOtpEntity -> userOTPRepository.deleteByOtpAndUser(userOtpEntity.getOtp(), user));
        UserOtpEntity userOtpEntity = new UserOtpEntity();
        userOtpEntity.setUser(user);
        userOtpEntity.setOtp(RandomGenerator.randomNumber(otpLength));
        userOtpEntity.setType(otpType);
        userOtpEntity.setCreatedAt(new Date());
        return userOTPRepository.save(userOtpEntity);
    }

    public void validateOtp(String otp, UserEntity user, OTPType otpType) {

        UserOtpEntity userOTP = getUserOTP(user, otp);

        if (otpType != userOTP.getType() || new Date().after(DateUtils.addSeconds(userOTP.getCreatedAt(), validDurationInSeconds)))
            throw new RuntimeBusinessException(ErrorCodes.OTP$INVALID.getValue(), HttpStatus.BAD_REQUEST.name(), HttpStatus.BAD_REQUEST);

    }

    public void deleteUserOTP(UserOtpEntity userOtpEntity) {
        getUserOTP(userOtpEntity.getUser(), userOtpEntity.getOtp());
        userOTPRepository.deleteById(userOtpEntity.getId());
    }

    public UserOtpEntity getUserOTP(UserEntity user, String otp) {
        return userOTPRepository.findByUserAndOtp(user, otp).
                orElseThrow(() -> new RuntimeBusinessException(ErrorCodes.OTP$NOTFOUND.getValue(), HttpStatus.BAD_REQUEST.name(), HttpStatus.BAD_REQUEST));
    }
}
