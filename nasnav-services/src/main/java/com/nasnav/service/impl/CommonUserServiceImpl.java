package com.nasnav.service.impl;

import static com.nasnav.exceptions.ErrorCodes.E$USR$0003;
import static com.nasnav.exceptions.ErrorCodes.E$USR$0004;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nasnav.PasswordEncoderConfig;
import com.nasnav.dao.CommonUserRepository;
import com.nasnav.dto.UserDTOs.ChangePasswordUserObject;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.BaseUserEntity;
import com.nasnav.response.UserApiResponse;
import com.nasnav.service.CommonUserService;
import com.nasnav.service.SecurityService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommonUserServiceImpl implements CommonUserService {
	private final SecurityService securityService;
	private final PasswordEncoderConfig passwordEncoderConfig;
	private final CommonUserRepository commonUserRepository;

	@Override
	@Transactional
	public UserApiResponse changePasswordUser(ChangePasswordUserObject userJson) {
		BaseUserEntity userAuthed = securityService.getCurrentUser();
		if (!userJson.getNewPassword().equals(userJson.getConfirmPassword())) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, E$USR$0004, "ConfirmPassword");
		}
		if (!passwordEncoderConfig.passwordEncoder().matches(userJson.currentPassword,
				userAuthed.getEncryptedPassword())) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, E$USR$0003, "oldPassword");
		}
		userAuthed.setEncryptedPassword(passwordEncoderConfig.passwordEncoder().encode(userJson.newPassword));
		commonUserRepository.saveAndFlush(userAuthed);

		return new UserApiResponse(userAuthed.getId());
	}

}
