package com.nasnav.service;

import com.nasnav.dao.ApiCallsRepository;
import com.nasnav.persistence.*;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.ContentCachingRequestWrapper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@Component
public class ApiLogInterceptor implements HandlerInterceptor {

	@Autowired
	private SecurityService securityService;

	@Autowired
	private ApiCallsRepository apiCallsRepo;


	@Override
	public void afterCompletion(HttpServletRequest request,
								HttpServletResponse response,
								Object handler,
								@Nullable Exception ex) throws Exception {
		ApiLogsEntity apiLogsEntity = prepareLogEntity(request, response);

		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				saveApiLogs(apiLogsEntity);
			}
		});
		thread.start();
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
		String requestContent = getRequestContent((ContentCachingRequestWrapper) request);
		Integer responseCode = response.getStatus();

		apiLogsEntity.setUrl(apiUrl);
		apiLogsEntity.setRequestContent(requestContent);
		apiLogsEntity.setResponseCode(responseCode);

		return apiLogsEntity;
	}

	private String getApiURL(HttpServletRequest request) {
		return request.getMethod() + " " + request.getRequestURI();
	}

	private String getRequestContent(ContentCachingRequestWrapper request) {
		JSONObject requestBodyJson = readRequestBodyAsJson(request);

		String requestContent = new JSONObject()
									.put("request_body", requestBodyJson)
									.put("request_parameters", request.getQueryString())
									.toString();
		return requestContent;
	}

	private JSONObject readRequestBodyAsJson(ContentCachingRequestWrapper request){
		String requestBody = new String(request.getContentAsByteArray(), StandardCharsets.UTF_8);
		requestBody = requestBody.lines().collect(Collectors.joining(""));

		return requestBody.isEmpty() ? new JSONObject("{}") : new JSONObject(requestBody);
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