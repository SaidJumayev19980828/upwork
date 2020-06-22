package com.nasnav.commons.utils;

import static java.util.Arrays.asList;
import static java.util.Optional.ofNullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.http.HttpStatus;

import com.nasnav.constatnts.EntityConstants;
import com.nasnav.exceptions.EntityValidationException;
import com.nasnav.response.ResponseStatus;
import com.nasnav.response.UserApiResponse;


public class StringUtils extends org.springframework.util.StringUtils{
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
		if (object instanceof Collection<?>) {
			return ((Collection<?>) object).isEmpty();
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
		String result = ofNullable(url)
							.orElse("")
							.replaceAll("[^a-zA-Z0-9]+", "-")
							.replaceAll("^-|-$","");
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

	
	/**
	 * @return object.toString() , or null if the object is null
	 * */
	public static String nullableToString(Object object) {
		return ofNullable(object)
				.map(Object::toString)
				.orElse(null);		
	}
	
	
	
	public static String nullSafe(String string) {
		return ofNullable(string)
				       .orElse("");
	}
	
	
	public static boolean anyBlankOrNull(String... strings) {
		return asList(strings)
				.stream()
				.anyMatch(StringUtils::isBlankOrNull);			
	}
	
	
	
	
	public static boolean anyNotBlankOrNull(String... strings) {
		return asList(strings)
				.stream()
				.anyMatch(StringUtils::isNotBlankOrNull);			
	}
	
	
	
	public static boolean endsWithAnyOf(String string, String...suffixes) {
		String str = ofNullable(string).orElse("");
		return asList(suffixes)
				.stream()
				.filter(Objects::nonNull)
				.anyMatch(suffix -> str.endsWith(suffix));
	}
	
	
	
	public static boolean endsWithAnyOfAndIgnoreCase(String string, String...suffixes) {
		String str = ofNullable(string).orElse("");
		return asList(suffixes)
				.stream()
				.filter(Objects::nonNull)
				.anyMatch(suffix -> str.toLowerCase().endsWith(suffix.toLowerCase()));
	}
	
	
	
	public static boolean startsWithAnyOf(String string, String...preffixes) {
		String str = ofNullable(string).orElse("");
		return asList(preffixes)
				.stream()
				.filter(Objects::nonNull)
				.anyMatch(prefix -> str.startsWith(prefix));
	}
	
	
	
	
	public static boolean startsWithAnyOfAndIgnoreCase(String string, String...preffixes) {
		String str = ofNullable(string).orElse("");
		return asList(preffixes)
				.stream()
				.filter(Objects::nonNull)
				.anyMatch(prefix -> str.toLowerCase().startsWith(prefix.toLowerCase()));
	}
	
	
	
	public static Long parseLongWithDefault(String string, Long defaultVal) {
		return ofNullable(string)
				.map(str -> {
					try{ 
						return Long.parseLong(str);
						}catch(Throwable t) {
							return defaultVal;}
					})
				.orElse(defaultVal);
	}
}
