package com.nasnav.service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import com.nasnav.dao.CommonUserRepository;
import com.nasnav.dao.RoleRepository;
import com.nasnav.persistence.BaseUserEntity;
import com.nasnav.persistence.Role;
import com.nasnav.persistence.UserEntity;

public class SecurityServiceImpl implements SecurityService {
	
	@Autowired
	private CommonUserRepository  userRepo;
	
	@Autowired
	private RoleRepository roleRepo;
	

	
	
	@Override
	public Optional<UserDetails> findUserByAuthToken(String token){
			return userRepo.findByAuthenticationToken(token)
							.flatMap(this::getUser)	;			
			
	}
	
	
	
	
	
	private Optional<UserDetails> getUser(BaseUserEntity userEntity) {		
		List<GrantedAuthority> roles = getUserRoles(userEntity);
		User user= new User(userEntity.getName(), userEntity.getEncryptedPassword(), true, true, true, true,roles);
        return Optional.of(user);		
	}





	private List<GrantedAuthority> getUserRoles(BaseUserEntity userEntity) {
		if(userEntity != null && userEntity instanceof UserEntity)
			return Arrays.asList();
		
		return roleRepo.getRolesOfEmployeeUser(userEntity.getId()).stream()
												.map(Role::getName)
												.map(SimpleGrantedAuthority::new)
												.collect(Collectors.toList());
	}
}

