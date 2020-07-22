package com.nasnav.service;

import com.nasnav.dto.AddressDTO;
import org.springframework.web.servlet.view.RedirectView;

import com.nasnav.dto.UserDTOs;
import com.nasnav.dto.UserRepresentationObject;
import com.nasnav.dto.request.user.ActivationEmailResendDTO;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.response.UserApiResponse;

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

    RedirectView activateUserAccount(String token, String redirect) throws BusinessException;

    public UserApiResponse updateUser(String userToken, UserDTOs.EmployeeUserUpdatingObject userJson) throws BusinessException;

    public UserRepresentationObject getUserData(Long id, Boolean isEmployee) throws BusinessException;

	public void resendActivationEmail(ActivationEmailResendDTO accountInfo) throws BusinessException;

	UserApiResponse activateUserAccount(String token) throws BusinessException;

    AddressDTO updateUserAddress(AddressDTO addressDTO);

    void removeUserAddress(Long id);
}
