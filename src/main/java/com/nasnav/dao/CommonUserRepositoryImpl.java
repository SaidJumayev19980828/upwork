package com.nasnav.dao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;

import com.nasnav.enumerations.Roles;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.persistence.BaseUserEntity;
import com.nasnav.persistence.EmployeeUserEntity;
import com.nasnav.persistence.Role;
import com.nasnav.persistence.UserEntity;


@Repository
public class CommonUserRepositoryImpl implements CommonUserRepository {

	@Autowired
	private EmployeeUserRepository empRepo;
	
	@Autowired
	private UserRepository userRepo;
	
	
	@Autowired
	private RoleRepository roleRepo; 
	
	
	
	@Override
	public Optional<? extends BaseUserEntity> findByAuthenticationToken(String authToken) {
		Optional<? extends BaseUserEntity>  user = empRepo.findByAuthenticationToken(authToken);
		if(user.isPresent())
			return user;
		else
            return userRepo.findByAuthenticationToken(authToken);
	}
	
	
	
	
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
						.collect(Collectors.toList());	
	}
	
	
	
	private List<String> getCustomerUserRoles() {
		// for now, return default role which is Customer
		return Collections.singletonList(Roles.CUSTOMER.name());
	}




	@Override
	public BaseUserEntity saveAndFlush(BaseUserEntity userEntity) throws BusinessException {
		BaseUserEntity savedEntity = null;
		if(userEntity instanceof EmployeeUserEntity)
			savedEntity =  empRepo.saveAndFlush((EmployeeUserEntity)userEntity);
		else if(userEntity instanceof UserEntity)
			savedEntity =  userRepo.saveAndFlush((UserEntity)userEntity);
		else
			//TODO : need error code for that  
			throw new BusinessException("Unknown User Entity Type", "?", HttpStatus.INTERNAL_SERVER_ERROR);
		
		return savedEntity;
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
			user = empRepo.getByEmailAndOrganizationId(email, org_id);
		
		return user;
	}




	@Override
	public BaseUserEntity getByEmailIgnoreCaseAndOrganizationId(String email, Long orgId) {		
		BaseUserEntity user = userRepo.getByEmailIgnoreCaseAndOrganizationId(email, orgId);
		
		if(user == null)
			user = empRepo.getByEmailIgnoreCaseAndOrganizationId(email, orgId);
		
		return user;		
	}

}
