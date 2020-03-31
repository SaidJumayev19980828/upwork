package com.nasnav.service;

import com.nasnav.dto.UserDTOs;
import com.nasnav.dto.UserRepresentationObject;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.response.UserApiResponse;
import org.springframework.web.servlet.view.RedirectView;

public interface UserService extends CommonUserServiceInterface{


    /**
     * Register the passed User entity
     * by saving it to DB
     *
     * @param userJson UserJson string
     * @return User entity after saving to DB
     */
    UserApiResponse registerUser(UserDTOs.UserRegistrationObject userJson);

    UserApiResponse registerUserV2(UserDTOs.UserRegistrationObjectV2 userJson) throws BusinessException;

    RedirectView activateUserAccount(String token) throws BusinessException;

    public UserApiResponse updateUser(String userToken, UserDTOs.EmployeeUserUpdatingObject userJson) throws BusinessException;

    public UserRepresentationObject getUserData(Long id, Boolean isEmployee) throws BusinessException;
}
