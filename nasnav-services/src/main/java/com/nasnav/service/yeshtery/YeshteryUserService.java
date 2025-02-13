package com.nasnav.service.yeshtery;

import com.nasnav.dto.AddressDTO;
import com.nasnav.dto.PaginatedResponse;
import com.nasnav.dto.UserDTOs;
import com.nasnav.dto.UserRepresentationObject;
import com.nasnav.dto.request.ActivateOtpDto;
import com.nasnav.dto.request.ActivateOtpWithPasswordDto;
import com.nasnav.dto.request.user.ActivationEmailResendDTO;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.persistence.UserEntity;
import com.nasnav.persistence.yeshtery.YeshteryUserEntity;
import com.nasnav.response.RecoveryUserResponse;
import com.nasnav.response.UserApiResponse;
import com.nasnav.dto.response.YeshteryUserApiResponse;
import org.springframework.web.servlet.view.RedirectView;


public interface YeshteryUserService  extends CommonYeshteryUserServiceInterface {
    YeshteryUserApiResponse registerYeshteryUserV2(Long referral, UserDTOs.UserRegistrationObjectV2 userJson) throws BusinessException;
    RedirectView activateYeshteryUserAccount(String token, String redirect) throws BusinessException;
    UserRepresentationObject getYeshteryUserData(Long id, Boolean isEmployee) throws BusinessException;
    void resendActivationYeshteryEmail(ActivationEmailResendDTO accountInfo) throws BusinessException;
    YeshteryUserApiResponse activateYeshteryUserAccount(String token) throws BusinessException;
    void subscribeYeshteryEmail(String email, Long orgId);
    RedirectView activateYeshterySubscribedEmail(String token, Long orgId);
    AddressDTO updateUserAddress(AddressDTO addressDTO);
    void removeUserAddress(Long id);
    UserApiResponse updateUser(UserDTOs.EmployeeUserUpdatingObject userJson);
    PaginatedResponse<UserRepresentationObject> getUserList(Integer userStatus, Integer page, Integer size);

    int linkNonYeshteryUsersToCorrespondingYeshteryUserEntity();
    int linkNonYeshteryUsersToCorrespondingYeshteryUserEntity(YeshteryUserEntity yeshteryUser);

    YeshteryUserEntity createYeshteryEntity(String name, String email, UserEntity nasnavUser, int yeshteryOrgId, Long orgId);

    UserApiResponse activateUserAccount(ActivateOtpWithPasswordDto activateOtp);

    RecoveryUserResponse activateRecoveryOtp(ActivateOtpDto activateOtp) throws BusinessException;

    UserEntity getUserForOrg(UserEntity user, Long orgId);
}
