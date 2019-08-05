package com.nasnav.dao;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.nasnav.persistence.BaseUserEntity;


@Repository
public class CommonUserRepositoryImp implements CommonUserRepository {

	@Autowired
	private EmployeeUserRepository empRepo;
	
	@Autowired
	private UserRepository userRepo;
	
	
	@Override
	public Optional<? extends BaseUserEntity> findByAuthenticationToken(String authToken) {
		Optional<? extends BaseUserEntity>  user = empRepo.findByAuthenticationToken(authToken);
		if(user.isPresent())
			return user;
		else
            return userRepo.findByAuthenticationToken(authToken);
	}

}
