package com.nasnav.service;

import com.nasnav.dto.UserDTOs.ChangePasswordUserObject;
import com.nasnav.response.UserApiResponse;

public interface CommonUserService {
	UserApiResponse changePasswordUser(ChangePasswordUserObject userJson);
}
