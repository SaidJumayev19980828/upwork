package com.nasnav.dao;

import com.nasnav.persistence.UserEntity;
import com.nasnav.persistence.UserOtpEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserOTPRepository extends JpaRepository<UserOtpEntity, Long> {

    void deleteByOtpAndUser(String otp, UserEntity user);
    Optional<UserOtpEntity> findByUser(UserEntity user);
    Optional<UserOtpEntity> findByUserAndOtp(UserEntity user, String otp);
}
