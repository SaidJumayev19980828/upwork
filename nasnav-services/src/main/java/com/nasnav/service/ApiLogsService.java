package com.nasnav.service;

import com.nasnav.dto.response.ApiLogsResponse;
import com.nasnav.request.ApiLogsSearchParam;

public interface ApiLogsService {
	public ApiLogsResponse getAPIsCalls(ApiLogsSearchParam searchParam);
}