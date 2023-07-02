package com.nasnav.dao;

import com.nasnav.dto.UserDTOs;
import com.nasnav.enumerations.YeshteryState;
import com.nasnav.persistence.BaseUserEntity;
import com.nasnav.response.UserApiResponse;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface CommonUserRepository{
	List<String> getUserRoles(BaseUserEntity user);
	BaseUserEntity saveAndFlush(BaseUserEntity userEntity) ;
	BaseUserEntity getByEmailAndOrganizationId(String email, Long org_id);
	BaseUserEntity getByEmailIgnoreCaseAndOrganizationId(String email, Long orgId, Boolean isEmployee);
	Optional<BaseUserEntity> findById(Long id, Boolean isEmp);
	UserApiResponse changePasswordUser(UserDTOs.ChangePasswordUserObject userJson);
	Optional<BaseUserEntity> getByIdAndOrganizationIdAndRoles(Long id, Long orgId, Boolean isEmployee, Set<String> roles);
}
