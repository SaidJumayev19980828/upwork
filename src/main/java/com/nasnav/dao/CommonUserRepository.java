package com.nasnav.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nasnav.exceptions.BusinessException;
import com.nasnav.persistence.BaseUserEntity;


@Repository
public interface CommonUserRepository{
	Optional<? extends BaseUserEntity> findByAuthenticationToken(String authToken);
	List<String> getUserRoles(BaseUserEntity user);
	BaseUserEntity saveAndFlush(BaseUserEntity userEntity) throws BusinessException;
	BaseUserEntity getByEmailAndOrganizationId(String email, Long org_id);
	BaseUserEntity getByEmailIgnoreCaseAndOrganizationId(String email, Long orgId, Boolean isEmployee);
}
