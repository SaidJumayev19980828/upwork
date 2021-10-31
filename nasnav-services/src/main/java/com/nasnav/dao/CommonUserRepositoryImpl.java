package com.nasnav.dao;

import com.nasnav.enumerations.Roles;
import com.nasnav.enumerations.YeshteryState;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.BaseUserEntity;
import com.nasnav.persistence.EmployeeUserEntity;
import com.nasnav.persistence.Role;
import com.nasnav.persistence.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.nasnav.commons.utils.StringUtils.isBlankOrNull;
import static com.nasnav.enumerations.YeshteryState.ACTIVE;
import static com.nasnav.exceptions.ErrorCodes.GEN$0004;
import static com.nasnav.exceptions.ErrorCodes.U$LOG$0002;
import static java.util.stream.Collectors.toList;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;


@Repository
public class CommonUserRepositoryImpl implements CommonUserRepository {

	@Autowired
	private EmployeeUserRepository empRepo;
	
	@Autowired
	private UserRepository userRepo;
	/*
	TODO should fix dependency circulation issue and fetch YeshteryUserEntity using yeshteryUserRepo
	@Autowired
	private YeshteryUserRepository yeshteryUserRepo;
	 */
	
	@Autowired
	private RoleRepository roleRepo; 

	
	@Override
	public List<String> getUserRoles(BaseUserEntity user) {
		
		if(user == null)
			return new ArrayList<>();
		
		
		if(user instanceof UserEntity)
			return getCustomerUserRoles();
		
		List<Role> rolesOfEmployeeUser = this.roleRepo.getRolesOfEmployeeUser(user.getId());
		return rolesOfEmployeeUser.stream()
						.filter(role -> role != null)
						.map(Role::getName)
						.collect(toList());
	}
	
	
	
	private List<String> getCustomerUserRoles() {
		// for now, return default role which is Customer
		return Collections.singletonList(Roles.CUSTOMER.name());
	}




	@Override
	public BaseUserEntity saveAndFlush(BaseUserEntity userEntity)  {
		if(userEntity instanceof EmployeeUserEntity)
			return empRepo.saveAndFlush((EmployeeUserEntity)userEntity);
		else if(userEntity instanceof UserEntity)
			return userRepo.saveAndFlush((UserEntity)userEntity);
		else
			throw new RuntimeBusinessException(INTERNAL_SERVER_ERROR, GEN$0004);
	}


	/**
	 * @return user from either users table or employee_users table, a user with this email
	 * and organization id will be returned from users table if it was found there first.
	 * Why ? because we are using 2 table for holding users for legacy reasons :/
	 * 
	 * 	 */
	@Override
	public BaseUserEntity getByEmailAndOrganizationId(String email, Long org_id) {
		BaseUserEntity user = userRepo.getByEmailAndOrganizationId(email, org_id);
		
		if(user == null)
			user = empRepo.findByEmailIgnoreCaseAndOrganizationId(email, org_id).orElse(null);
		
		return user;
	}




	public BaseUserEntity getByEmailIgnoreCaseAndOrganizationId(String email, Long orgId, Boolean isEmployee, YeshteryState state) {
		if (state.equals(ACTIVE)) {
			return userRepo.getYeshteryUserByEmail(email, orgId);
		}
		if(isEmployee != null && isEmployee) {
			return empRepo.getByEmailIgnoreCase(email);
		}else {
			if (isBlankOrNull(orgId)) {
				throw new RuntimeBusinessException(UNAUTHORIZED, U$LOG$0002);
			}
			return userRepo.getByEmailIgnoreCaseAndOrganizationId(email, orgId);
		}
	}




	@Override
	public Optional<BaseUserEntity> findById(Long id, Boolean isEmp) {
		if(isEmp) {
			return empRepo.findById(id)
						.map(BaseUserEntity.class::cast);
		}else {
			return userRepo.findById(id)
						.map(BaseUserEntity.class::cast);
		}
	}

}
