package com.nasnav.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.nasnav.persistence.BaseUserEntity;


@Repository
public interface CommonUserRepository{
	List<String> getUserRoles(BaseUserEntity user);
	BaseUserEntity saveAndFlush(BaseUserEntity userEntity) ;
	BaseUserEntity getByEmailAndOrganizationId(String email, Long org_id);
	BaseUserEntity getByEmailIgnoreCaseAndOrganizationId(String email, Long orgId, Boolean isEmployee);
	Optional<BaseUserEntity> findById(Long id, Boolean isEmp);
}
