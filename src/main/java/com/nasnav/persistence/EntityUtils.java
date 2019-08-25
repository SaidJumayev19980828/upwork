package com.nasnav.persistence;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;

import com.nasnav.response.ApiResponseBuilder;
import com.nasnav.response.ResponseStatus;
import com.nasnav.response.UserApiResponse;

public class EntityUtils {
	public static UserApiResponse createFailedLoginResponse(List<ResponseStatus> responseStatuses) {
		return new ApiResponseBuilder().setSuccess(false).setResponseStatuses(responseStatuses).build();

	}
	
	
	public static void copyNonNullProperties( Object source, Object destination) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		Map<String,Object> sourceProperties = PropertyUtils.describe(source);
		Map<String,Object> destProperties = PropertyUtils.describe(destination);
		sourceProperties.entrySet().stream()
				.filter(e -> ! e.getKey().equals("class"))
				.filter(e -> destProperties.keySet().contains(e.getKey()))
				.filter(e -> PropertyUtils.isWriteable(destination, e.getKey()))
		        .filter(e -> e.getValue() != null)		        
		        .forEach(e -> {
			        try {
			            PropertyUtils.setProperty(destination, e.getKey(), e.getValue());
			        } catch (Exception ex) {
			        	String msg = String.format("Failed to copy bean property [%s] from object of type [%s] to object of type [%s]" , e.getKey(), source.getClass(),destination.getClass());
			            throw new RuntimeException(msg);
			        }
	    });
	}
	
}
