package com.nasnav.commons.utils;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

public class EntityUtils {

	private static final Logger logger = LogManager.getLogger();
	public static final String DEFAULT_TIMESTAMP_PATTERN = "yyyy-MM-dd'T'HH:mm:ss'Z'";
	public static final String YESHTERY_PNAME = "yeshtery";

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
		List<Object> elementList =  asList(elements);
		Set<Object> elementSet = new HashSet<>(elementList);
		return  elementSet.size() == 1;
	}
	
	
	
	public static boolean anyIsNull(Object... elements){
		List<Object> elementList =  asList(elements);
		return elementList.stream().anyMatch(e -> e == null);
	}
	
	
	public static boolean anyIsNonNull(Object... elements){
		List<Object> elementList =  asList(elements);
		return elementList.stream().anyMatch(Objects::nonNull);
	}
	
	
	
	public static boolean allIsNull(Object... elements){
		List<Object> elementList =  asList(elements);
		return elementList.stream().allMatch(e -> e == null);
	}


	public static boolean anyIsEmpty(Optional<?>... elements){
		List<Optional<?>> elementList =  asList(elements);
		return elementList.stream().anyMatch(opt -> !opt.isPresent());
	}

	
	
	public static boolean isNullOrEmpty(Collection<?> collection) {
		return collection == null || collection.isEmpty();
	}



	public static boolean notNullNorEmpty(Collection<?> collection) {
		return !isNullOrEmpty(collection);
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
	
	
	
	@SafeVarargs
	public static <T> Boolean collectionContainsAnyOf(Collection<T> collection, T... objects) {
		List<T> objectList = asList(objects);
		return objectList
				.stream()
				.anyMatch(collection::contains);
	}
	
	
	public static <T,R>  R calcValueOrElseReturn(Function<T,R> function, T arg , R elseValue) {
		try {
			return function.apply(arg);
		}catch(Throwable t) {
			return elseValue;
		}
	} 
	
	
	/**
	 * @return the given Function after being wrapped in a try-catch block.
	 * The returned Function will return null, if the original function has thrown an exception. 
	 * */
	public static <T,R>  Function<T,R> failSafeFunction(Function<T,R> function){
		return 
			arg -> {
				try {
					return function.apply(arg);
				}catch(Throwable t) {
					return null;
				}
			};
		
	}


	public static boolean noneIsNull(Object... elements) {
		List<?> elementList =  asList(elements);
		return elementList
				.stream()
				.noneMatch(Objects::isNull);
	}


	public static boolean noneIsEmpty(Collection<?> ... collections) {
		List<Collection<?>> elementList =  asList(collections);
		return elementList
				.stream()
				.noneMatch(c -> c == null || c.isEmpty());
	}
	
	
	
	@SafeVarargs
	public static <T> Optional<T> firstExistingValueOf(T... values) {
		return Stream
				.of(values)
				.filter(Objects::nonNull)
				.findFirst();
	}
	
	
	
	
	
	@SafeVarargs
	public static <T> Optional<T> firstExistingValueOf(Optional<T>... values) {
		return Stream
				.of(values)
				.filter(Objects::nonNull)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.findFirst();
	}
	
	
	
	
	public static Optional<Long> parseLongSafely(String str){
		try {
			return Optional.of(Long.parseLong(str));
		}catch(Throwable e) {
			logger.error(e,e);
			return Optional.empty();
		}
	}




	public static Optional<Long> parseLongSafely(Object object) {
		try{
			if(object == null){
				return empty();
			}else {
				return Optional.of(Long.valueOf(object.toString()));
			}
		}catch(Throwable e){
			logger.error(e,e);
			return empty();
		}
	}




	public static Optional<Integer> parseIntegerSafely(Object object) {
		try{
			if(object == null){
				return empty();
			}else {
				return Optional.of(Integer.valueOf(object.toString()));
			}
		}catch(Throwable e){
			logger.error(e,e);
			return empty();
		}
	}



	public static Optional<LocalDateTime> parseTimeString(String timeStr, String pattern) {
		String pattenStr = ofNullable(pattern).orElse(DEFAULT_TIMESTAMP_PATTERN);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattenStr);
		return ofNullable(timeStr)
				.map(t -> LocalDateTime.parse(timeStr, formatter));
	}
	
	
	
	
	public static Optional<LocalDateTime> parseTimeString(String timeStr) {
		return parseTimeString(timeStr, DEFAULT_TIMESTAMP_PATTERN);
	}


	public static Optional<String> toTimeString(LocalDateTime time){
		return ofNullable(time)
				.map(t -> DateTimeFormatter.ofPattern(DEFAULT_TIMESTAMP_PATTERN).format(t));
	}


	public static LocalDateTime toLocalDateTime(ZonedDateTime time){
		return ofNullable(time)
				.map(t -> t.withZoneSameInstant(ZoneId.of("UTC")))
				.map(ZonedDateTime::toLocalDateTime)
				.orElse(null);
	}


	public static boolean anyIsTrue(Boolean ... flags){
		return Arrays.stream(flags)
				.map(booleanVal -> ofNullable(booleanVal).orElse(false))
				.anyMatch(FunctionalUtils::self);
	}
}



