package com.nasnav.service;

import com.nasnav.persistence.BaseUserEntity;
import com.nasnav.persistence.EmployeeUserEntity;

import org.springframework.security.core.userdetails.UserDetails;

import com.nasnav.dto.UserDTOs;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.response.UserApiResponse;

import java.util.Optional;

public interface SecurityService {
	
	Optional<UserDetails> findUserByAuthToken(String token);
	
	/**
     * login user to system
     *
     * @param body json object containing email and password
     * @return UserApiResponse object holding the status
	 * @throws BusinessException 
     */
    UserApiResponse login(UserDTOs.UserLoginObject body) throws BusinessException;
    
    
    EmployeeUserEntity getCurrentUser();
    
    Long getCurrentUserOrganization();
}
