package com.nasnav.dao;

import com.nasnav.enumerations.YeshteryState;
import com.nasnav.persistence.BaseUserEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

public interface CommonUserRepository{
	List<String> getUserRoles(BaseUserEntity user);
	BaseUserEntity saveAndFlush(BaseUserEntity userEntity) ;
	BaseUserEntity getByEmailAndOrganizationId(String email, Long org_id);
	BaseUserEntity getByEmailIgnoreCaseAndOrganizationId(String email, Long orgId, Boolean isEmployee, YeshteryState state);
	Optional<BaseUserEntity> findById(Long id, Boolean isEmp);
}
