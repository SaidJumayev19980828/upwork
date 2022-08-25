package com.nasnav.service;

import com.nasnav.dao.ApiCallsRepository;
import com.nasnav.persistence.*;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.ContentCachingRequestWrapper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.stream.Collectors;

@Component
public class ApiLogInterceptor implements HandlerInterceptor {

	@Autowired
	private SecurityService securityService;

	@Autowired
	private ApiCallsRepository apiCallsRepo;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
		HttpServletRequest requestCacheWrapperObject = new ContentCachingRequestWrapper(request);
		ApiLogsEntity apiLogsEntity = prepareLogEntity(requestCacheWrapperObject, response);
		saveApiLogs(apiLogsEntity);
		return true;
	}

	private void saveApiLogs(ApiLogsEntity apiLogsEntity){
		try {
			setLogEntityContextAttributes(apiLogsEntity);
			apiCallsRepo.save(apiLogsEntity);
		} catch (IllegalStateException exception) {

		}
	}

	private ApiLogsEntity prepareLogEntity(HttpServletRequest request, HttpServletResponse response) {
		ApiLogsEntity apiLogsEntity = new ApiLogsEntity();
		String apiUrl = getApiURL(request);
		String requestContent = getRequestContent(request);
		Integer responseCode = response.getStatus();

		apiLogsEntity.setUrl(apiUrl);
		apiLogsEntity.setRequestContent(requestContent);
		apiLogsEntity.setResponseCode(responseCode);

		return apiLogsEntity;
	}

	private String getApiURL(HttpServletRequest request) {
		return request.getMethod() + " " + request.getRequestURI();
	}

	private String getRequestContent(HttpServletRequest request) {
		JSONObject requestBodyJson = readRequestBodyAsJson(request);

		return new JSONObject()
						.put("request_body", requestBodyJson)
						.put("request_parameters", request.getQueryString())
						.toString();
	}

	private JSONObject readRequestBodyAsJson(HttpServletRequest request){
		try {
			var requestBody = request.getParameterMap();

			return requestBody.isEmpty() ? new JSONObject("{}") : new JSONObject(requestBody);
		} catch (Exception e) {
			return new JSONObject("{}");
		}
	}

	private void setLogEntityContextAttributes(ApiLogsEntity apiLogsEntity) throws IllegalStateException {
		setLoggedOrganization(apiLogsEntity);
		setLoggedUser(apiLogsEntity);
	}

	private void setLoggedOrganization(ApiLogsEntity apiLogsEntity) {
		OrganizationEntity organization = securityService.getCurrentUserOrganization();
		apiLogsEntity.setOrganization(organization);
	}

	private void setLoggedUser(ApiLogsEntity apiLogsEntity) throws IllegalStateException {
		BaseUserEntity currentUser = securityService.getCurrentUser();
		if (securityService.currentUserIsCustomer()) {
			apiLogsEntity.setLoggedCustomer((UserEntity) currentUser);
		} else {
			apiLogsEntity.setLoggedEmployee((EmployeeUserEntity) currentUser);
		}
	}
}