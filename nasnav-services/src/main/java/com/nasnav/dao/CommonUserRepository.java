package com.nasnav.dao;

import com.nasnav.persistence.BaseUserEntity;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface CommonUserRepository{
	List<String> getUserRoles(BaseUserEntity user);
	BaseUserEntity saveAndFlush(BaseUserEntity userEntity) ;
	BaseUserEntity getByEmailAndOrganizationId(String email, Long org_id);
	BaseUserEntity getByEmailIgnoreCaseAndOrganizationId(String email, Long orgId, Boolean isEmployee);
	Optional<BaseUserEntity> findById(Long id, Boolean isEmp);
	Optional<BaseUserEntity> getByIdAndOrganizationIdAndRoles(Long id, Long orgId, Boolean isEmployee, Set<String> roles);
}
