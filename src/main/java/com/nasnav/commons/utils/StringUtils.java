package com.nasnav.commons.utils;

import com.nasnav.constatnts.EntityConstants;
import com.nasnav.exceptions.EntityValidationException;
import com.nasnav.response.UserApiResponse;
import com.nasnav.response.ApiResponseBuilder;
import com.nasnav.response.ResponseStatus;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

import org.springframework.http.HttpStatus;


public class StringUtils extends org.springframework.util.StringUtils{
	private static Random random;

	public static boolean patternMatcher(String input, String regex) {
		if (input == null) {
			return false;
		}
		return input.matches(regex);
	}

	/**
	 * validate the name param against Name_PATTERN
	 *
	 * @param name to be matched against Name_PATTERN
	 * @return true if name match Name_PATTERN
	 */
	public static boolean validateName(String name) {
		return patternMatcher(name, EntityConstants.Name_PATTERN);
	}

	
	
	public static boolean validateEmail(String email) {
		return patternMatcher(email, EntityConstants.EMAIL_PATTERN);
	}

	
	
	public static boolean isNotBlankOrNull(Object object) {
		return !isBlankOrNull(object);
	}

	
	
	public static boolean isBlankOrNull(Object object) {
		if (object == null) {
			return true;
		}
		if (object instanceof String) {
			return ((String) object).isEmpty();
		}
		if (object instanceof Collection) {
			return ((Collection) object).isEmpty();
		}
		return false;
	}

	
	
	public static String generateUUIDToken() {
		return UUID.randomUUID().toString();
	}

	
	
	

	
	
	
	public static void validateNameAndEmail(String name, String email, Long orgId) {
		List<ResponseStatus> responseStatusList = new ArrayList<>();
		if (!StringUtils.validateName(name)) {
			responseStatusList.add(ResponseStatus.INVALID_NAME);
		}
		if (!StringUtils.validateEmail(email)) {
			responseStatusList.add(ResponseStatus.INVALID_EMAIL);
		}
		if (StringUtils.isBlankOrNull(orgId) || orgId <= 0){
			responseStatusList.add(ResponseStatus.INVALID_ORGANIZATION);
		}
		if (!responseStatusList.isEmpty()) {
			throw new EntityValidationException("Invalid User Entity: " + responseStatusList,
					UserApiResponse.createStatusApiResponse(responseStatusList), HttpStatus.NOT_ACCEPTABLE);
		}
	}
	
	

	public static String encodeUrl(String url){
		String result = url.replaceAll("[^a-zA-Z0-9]+", "-").replaceAll("^-|-$","");
		return result.toLowerCase();
	}
	
	public static boolean validateUrl(String url, String regex){
		return url.matches(regex);
	}
	
	
	public static String getFileNameSanitized(String name) {
		return name.replaceAll("[^a-zA-Z0-9\\.]+", "-")
						.replaceAll("^-|-$","")
						.toLowerCase();
	}
	
	
	
	
	public static String nullSafe(String string) {
		return Optional.ofNullable(string)
				       .orElse("");
	}
}
