package com.nasnav.service.otp;

import com.nasnav.AppConfig;
import com.nasnav.dao.BaseUserOtpRepository;
import com.nasnav.exceptions.ErrorCodes;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.DefaultBusinessEntity;
import com.nasnav.persistence.UserEntity;
import com.nasnav.persistence.BaseUserOtpEntity;
import com.nasnav.util.RandomGenerator;

import lombok.RequiredArgsConstructor;

import org.apache.commons.lang3.time.DateUtils;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@RequiredArgsConstructor
public abstract class BaseOtpService<U extends DefaultBusinessEntity<Long>, O extends BaseUserOtpEntity<U>, R extends BaseUserOtpRepository<U, O>> {

    protected final R userOTPRepository;

    protected final AppConfig appConfig;

    protected abstract O createEntity();

    @Transactional
    public O createUserOtp(U user, OtpType otpType) {
        userOTPRepository.deleteByUser(user);
        O userOtpEntity = createEntity();
        userOtpEntity.setUser(user);
        userOtpEntity.setOtp(RandomGenerator.randomNumber(appConfig.otpLength));
        userOtpEntity.setType(otpType);
        userOtpEntity.setCreatedAt(new Date());
        return userOTPRepository.save(userOtpEntity);
        
    }

    // new transaction is needed to keep retries consistent in case of transaction or no transaction
    @Transactional(propagation = Propagation.REQUIRES_NEW, noRollbackFor = RuntimeBusinessException.class)
    public void validateOtp(String otp, U user, OtpType otpType) {

        O userOtp = getUserOtp(user, otpType);

        Date currentDate = new Date();

        Date expiration = DateUtils.addSeconds(userOtp.getCreatedAt(), appConfig.otpValidDurationInSeconds);

        Long attempts = userOtp.incrementAttempts();
        userOtp = userOTPRepository.save(userOtp);

        if (userOtp.getOtp().equals(otp)
                && attempts <= appConfig.otpMaxRetries
                && currentDate.after(userOtp.getCreatedAt())
                && currentDate.before(expiration)) {
            userOTPRepository.delete(userOtp);
        } else {
            if (attempts >= appConfig.otpMaxRetries) {
                userOTPRepository.delete(userOtp);
            }
            throw new RuntimeBusinessException(ErrorCodes.OTP$INVALID.getValue(), HttpStatus.BAD_REQUEST.name(), HttpStatus.BAD_REQUEST);
        }
    }

    public O getUserOtp(U user, OtpType type) {
        return userOTPRepository.findByUserAndType(user, type).
                orElseThrow(() -> new RuntimeBusinessException(ErrorCodes.OTP$NOTFOUND.getValue(), HttpStatus.BAD_REQUEST.name(), HttpStatus.BAD_REQUEST));
    }
}
