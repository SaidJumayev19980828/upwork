package com.nasnav.yeshtery.dao;

import com.nasnav.persistence.BaseYeshteryUserEntity;
import com.nasnav.persistence.YeshteryUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface YeshteryUserRepository extends JpaRepository<YeshteryUserEntity, Long> {

    Optional<YeshteryUserEntity> findById(Long id);

    /**
     * Check if the passed resetPasswordToken already exist before or not.
     *
     * @param resetPasswordToken to be checked
     * @return true if esetPasswordToken already exists
     */
    boolean existsByResetPasswordToken(String resetPasswordToken);

    YeshteryUserEntity findByResetPasswordToken(String token);

    boolean existsByEmailIgnoreCaseAndOrganizationId(String email, Long orgId);

    BaseYeshteryUserEntity getByEmail(String email);

    BaseYeshteryUserEntity getByEmailIgnoreCase(String email);

    YeshteryUserEntity getByEmailIgnoreCaseAndOrganizationId(String email, Long orgId);
}

