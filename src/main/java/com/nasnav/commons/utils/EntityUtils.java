package com.nasnav.commons.utils;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
	
	
	
	public static boolean areEqual(Object... elements){
		List elementList =  Arrays.asList(elements);
		Set elementSet = new HashSet<>(elementList);
		return  elementSet.size() == 1;
	}
	
	
	
	public static boolean anyIsNull(Object... elements){
		List elementList =  Arrays.asList(elements);
		return elementList.stream().anyMatch(e -> e == null);
	}
	
	
	
	public static boolean allIsNull(Object... elements){
		List elementList =  Arrays.asList(elements);
		return elementList.stream().allMatch(e -> e == null);
	}
	
	
	
	@SafeVarargs
	public static <T> Set<T> setOf(T...elements){
		return new HashSet<>( Arrays.asList(elements));
	}
	
	
	
	
	public static boolean isNullOrEmpty(Collection collection) {
		return collection == null || collection.isEmpty();
	}
	
	
	
	
	public static boolean isNullOrZero(Long n) {
		return n == null || n == 0L;
	}
	
	
	
	public static <T> List<T> concateLists(List<T> list1, List<T> list2){
		if(list1 == null)
			return list2;
		
		if(list2 == null)
			return list1;
		
		List<T> concate = new ArrayList<>(list1);
		concate.addAll(list2);
		return concate;
	}
	
	
}



