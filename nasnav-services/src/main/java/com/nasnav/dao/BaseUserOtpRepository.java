package com.nasnav.dao;

import com.nasnav.persistence.BaseUserOtpEntity;
import com.nasnav.persistence.DefaultBusinessEntity;
import com.nasnav.service.otp.OtpType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.Optional;

@NoRepositoryBean
public interface BaseUserOtpRepository<U extends DefaultBusinessEntity<Long>, O extends BaseUserOtpEntity<U>>
        extends JpaRepository<O, Long> {

    void deleteByOtpAndUser(String otp, U user);
    void deleteByUser(U user);
    Optional<O> findByUser(U user);
    Optional<O> findByUserAndType(U user, OtpType type);
    Optional<O> findByUserAndOtp(U user, String otp);
}
