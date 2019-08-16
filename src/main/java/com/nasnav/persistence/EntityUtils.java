package com.nasnav.persistence;

import java.util.List;

import com.nasnav.response.ApiResponseBuilder;
import com.nasnav.response.ResponseStatus;
import com.nasnav.response.UserApiResponse;

public class EntityUtils {
	public static UserApiResponse createFailedLoginResponse(List<ResponseStatus> responseStatuses) {
		return new ApiResponseBuilder().setSuccess(false).setResponseStatuses(responseStatuses).build();
	}
}
