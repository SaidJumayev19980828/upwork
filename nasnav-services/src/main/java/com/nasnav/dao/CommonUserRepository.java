package com.nasnav.dao;

import com.nasnav.persistence.BaseUserEntity;

import java.util.Optional;

public interface CommonUserRepository {
    BaseUserEntity saveAndFlush(BaseUserEntity userEntity);

    BaseUserEntity getByEmailAndOrganizationId(String email, Long orgId);

    BaseUserEntity getByEmailIgnoreCaseAndOrganizationId(String email, Long orgId, Boolean isEmployee);

    Optional<BaseUserEntity> findByIdAndOrganizationId(Long id, Long organizationId, Boolean isEmp);

    Optional<BaseUserEntity> findById(Long id, Boolean isEmp);
}
