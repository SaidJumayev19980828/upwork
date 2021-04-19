package com.nasnav.service;

import com.nasnav.dto.AddressDTO;
import com.nasnav.dto.UserDTOs;
import com.nasnav.dto.UserRepresentationObject;
import com.nasnav.dto.request.user.ActivationEmailResendDTO;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.response.UserApiResponse;
import org.springframework.web.servlet.view.RedirectView;

import java.util.List;

public interface UserService extends CommonUserServiceInterface{

    UserApiResponse registerUserV2(UserDTOs.UserRegistrationObjectV2 userJson) throws BusinessException;

    RedirectView activateUserAccount(String token, String redirect) throws BusinessException;

    UserApiResponse updateUser(UserDTOs.EmployeeUserUpdatingObject userJson) ;

    UserRepresentationObject getUserData(Long id, Boolean isEmployee) throws BusinessException;

	void resendActivationEmail(ActivationEmailResendDTO accountInfo) throws BusinessException;

	UserApiResponse activateUserAccount(String token) throws BusinessException;

    AddressDTO updateUserAddress(AddressDTO addressDTO);

    void removeUserAddress(Long id);

    void suspendUserAccount(Long id, Boolean suspend);

    void subscribeEmail(String email, Long orgId);

    RedirectView activateSubscribedEmail(String token, Long orgId);

    List<UserRepresentationObject> getUserList();
}
