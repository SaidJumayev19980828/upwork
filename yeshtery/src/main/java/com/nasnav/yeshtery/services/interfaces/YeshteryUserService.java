package com.nasnav.yeshtery.services.interfaces;

import com.nasnav.dto.AddressDTO;
import com.nasnav.dto.UserDTOs;
import com.nasnav.dto.UserRepresentationObject;
import com.nasnav.dto.request.user.ActivationEmailResendDTO;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.response.UserApiResponse;
import com.nasnav.yeshtery.response.YeshteryUserApiResponse;
import org.springframework.web.servlet.view.RedirectView;

import java.util.List;

public interface YeshteryUserService  extends CommonYeshteryUserServiceInterface {
    YeshteryUserApiResponse registerYeshteryUserV2(String referral, UserDTOs.UserRegistrationObjectV2 userJson) throws BusinessException;
    RedirectView activateYeshteryUserAccount(String token, String redirect) throws BusinessException;
    UserRepresentationObject getYeshteryUserData(Long id, Boolean isEmployee) throws BusinessException;
    void resendActivationYeshteryEmail(ActivationEmailResendDTO accountInfo) throws BusinessException;
    YeshteryUserApiResponse activateYeshteryUserAccount(String token) throws BusinessException;
    void subscribeYeshteryEmail(String email, Long orgId);
    RedirectView activateYeshterySubscribedEmail(String token, Long orgId);
    AddressDTO updateUserAddress(AddressDTO addressDTO);
    void removeUserAddress(Long id);
    UserApiResponse updateUser(UserDTOs.EmployeeUserUpdatingObject userJson);
    List<UserRepresentationObject> getUserList();
}
