package com.nasnav.service;

import com.nasnav.dto.ActivationMethod;
import com.nasnav.dto.AddressDTO;
import com.nasnav.dto.UserDTOs;
import com.nasnav.dto.UserRepresentationObject;
import com.nasnav.dto.request.ActivateOtpDto;
import com.nasnav.dto.request.user.ActivationEmailResendDTO;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.persistence.UserEntity;
import com.nasnav.request.ImageBase64;
import com.nasnav.response.RecoveryUserResponse;
import com.nasnav.response.UserApiResponse;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.view.RedirectView;

import java.io.IOException;
import java.util.List;

public interface UserService extends CommonUserServiceInterface {

    UserApiResponse registerUserReferral(UserDTOs.UserRegistrationObjectV2 userJson, Long referrer) throws BusinessException;

    RedirectView activateUserAccount(String token, String redirect) throws BusinessException;

    UserApiResponse updateUser(UserDTOs.EmployeeUserUpdatingObject userJson);

    UserRepresentationObject getUserData(Long id, Boolean isEmployee) throws BusinessException;

    void resendActivationEmail(ActivationEmailResendDTO accountInfo) throws BusinessException;

    UserApiResponse activateUserAccount(String token) throws BusinessException;

    AddressDTO updateUserAddress(AddressDTO addressDTO);

    void removeUserAddress(Long id);

    void suspendUserAccount(Long id, Boolean suspend);

    void subscribeEmail(String email, Long orgId);

    RedirectView activateSubscribedEmail(String token, Long orgId);

    List<UserRepresentationObject>  getUserListByStatusPaging(Integer start, Integer count, Integer userStatus);

    List<UserRepresentationObject> getUserList();

    List<UserEntity> getYeshteryUsersByAllowReward(Boolean allowReward);

    void updateUserByTierIdAndOrgId(Long userId, Long orgId);

    void sendEmailRecovery(String email, Long orgId);

    UserApiResponse activateUserAccount(ActivateOtpDto activateOtp);

    void sendEmailRecovery(String email, Long orgId, ActivationMethod activationMethod);

    RecoveryUserResponse activateRecoveryOtp(ActivateOtpDto activateOtp) throws BusinessException;

    UserApiResponse updateUserAvatar(MultipartFile file);

    UserApiResponse processUserAvatar(ImageBase64 image) throws IOException;

    void updateUserPhone(Long userId, Long organizationId ,String phone);

}
