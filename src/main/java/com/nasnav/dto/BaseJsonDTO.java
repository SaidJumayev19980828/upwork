package com.nasnav.dto;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.beanutils.PropertyUtils;
import org.jboss.logging.Logger;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * 
 * Checks if the setter of the property was ever called. 
 * This is useful when mapping JSON to a BaseJsonDTO subclass, if the setter was never called
 * then JSON field that should be mapped to this property didn't exist in the JSON.
 * This an attempt to differentiate between JSON fields that didn't exist in the JSON , and 
 * fields that exists but had null values, as jackson will convert both to null on in the DTO.
 *
 * Note that the subclasses setters must explicitly add the property getter to the isPresentSet
 * when called.
 * 
 * ex: 
 * public void setMyProperty(String value){
 * 		setPropertyAsPresent("myProperty");
 * 		this.myProperty = value;
 * }
 */

public abstract class BaseJsonDTO {
	
	protected Logger logger= Logger.getLogger(getClass());
	
	//wanted to use property getter as keys , to be more refactoring friendly, but failed.
	//Getters can be passed as method references of type Supplier<> , but i found that java create a new lambda instance for every
	//single method reference, which means i can't use them as Map keys.
	protected Set<PropertyDescriptor> updatedProperties;
	protected Map<PropertyDescriptor, Required> requiredMap;
	
	
	
	public BaseJsonDTO() {
		updatedProperties = new HashSet<>();
		requiredMap = new HashMap<>();
		
		initRequiredProperties();
	}
	
	/**
	 * @param prop : name of the property
	 * 
	 * @return true if the property setter was ever called. 
	 * Given that setter already calls setPropertyAsPresent() for the property name.  	 
	 * 
	 * ex: 
	 * MyDTO myDTO = new MyDTO();
	 * boolean exists = myDTO.isUpdated("myProperty"));
	 * */
	@JsonIgnore
	public boolean isUpdated(String propName) {
		PropertyDescriptor prop = getPropertyDescriptor(propName);
		return isUpdated(prop);
	}
	
	
	
	
	@JsonIgnore
	private boolean isUpdated(PropertyDescriptor prop) {
		return updatedProperties.contains(prop);
	}
	
	
	@JsonIgnore
	protected void setPropertyAsUpdated(String propName) {
		PropertyDescriptor prop = getPropertyDescriptor(propName);
		this.updatedProperties.add(prop);
	}
	
	
	
	
	public Required whenPropertyIsRequired(PropertyDescriptor getter) {
		return requiredMap.getOrDefault(getter, Required.NEVER);
	}
	
	
	
	@JsonIgnore
	protected Set<PropertyDescriptor> getPropertiesRequiredFor(Required requiredFor) {
		return requiredMap
				.entrySet()
				.stream()
				.filter(e -> e.getValue() == requiredFor)
				.map(Entry::getKey)
				.collect(Collectors.toSet());
	}
	
	

	@JsonIgnore
	public Set<PropertyDescriptor> getRequiredPropertiesForDataCreate() {
		return	Stream.concat(
					getRequiredPropertiesAlways().stream()
					, getPropertiesRequiredFor(Required.FOR_CREATE).stream())
				.collect(Collectors.toSet());
	}
	

	
	@JsonIgnore
	public Set<PropertyDescriptor> getRequiredPropertiesForDataUpdate() {
		return	Stream.concat(
					getRequiredPropertiesAlways().stream()
					, getPropertiesRequiredFor(Required.FOR_UPDATE).stream())
				.collect(Collectors.toSet());
	}

	@JsonIgnore
	public Set<String> getRequiredPropertyNamesForDataUpdate() {
		Set propertyNames = getRequiredPropertiesAlways().stream()
				.map(value -> value.getName()).collect(Collectors.toSet());
		propertyNames.addAll(getPropertiesRequiredFor(Required.FOR_UPDATE).stream()
				.map(value -> value.getName()).collect(Collectors.toSet()));
		return propertyNames;
	}

	@JsonIgnore
	public Set<PropertyDescriptor> getRequiredPropertiesAlways() {
		return getPropertiesRequiredFor(Required.ALWAYS);
	}
	
	
	
	
	protected Boolean areRequiredPropertiesPresent(Required required) {
		return Arrays.asList(Required.ALWAYS, required)
				.stream()
				.filter(req -> req != null)
				.flatMap(req -> getPropertiesRequiredFor(req).stream())
				.allMatch(this::isUpdated);			
	}
	
	
	/**
	 * @return if properties that are required for creating new data are:
	 * - were all updated at least once
	 * */
	@JsonIgnore
	public Boolean areRequiredForDataCreatePropertiesPresent() {
		return areRequiredPropertiesPresent(Required.FOR_CREATE);
	}
	
	
	/**
	 * @return if properties that are required for updating existing data are:
	 * - were all updated at least once
	 * */
	@JsonIgnore
	public Boolean areRequiredForDataUpdatePropertiesPresent() {
		return areRequiredPropertiesPresent(Required.FOR_UPDATE);
	}


	/**
	 * @return if properties that required for updating existing data are:
	 * - were all updated at least once
	 * */
	@JsonIgnore
	public Boolean areRequiredAlwaysPropertiesPresent() {
		return areRequiredPropertiesPresent(Required.ALWAYS);
	}
	
	
	
	
	
	
	
	@JsonIgnore
	public Boolean arePropertiesRequiredForCreateNotNull() {
		return getRequiredPropertiesForDataCreate()
				.stream()
				.map(this::getPropertyValue)
				.allMatch(Objects::nonNull);
	}
	
	
	
	
	@JsonIgnore
	public Boolean arePropertiesRequiredForUpdateNotNull() {
		return getRequiredPropertiesForDataUpdate()
				.stream()
				.map(this::getPropertyValue)
				.allMatch(Objects::nonNull);
	}
	
	
	
	/**
	 * @return if properties that required for updating existing data are:
	 * - were all updated at least once
	 * - all have non-null values 
	 * */
	@JsonIgnore
	public Boolean areRequiredForUpdatePropertiesProvided() {
		return areRequiredForDataUpdatePropertiesPresent() 
					&& arePropertiesRequiredForUpdateNotNull(); 
				
	}
	
	
	
	/**
	 * @return if properties that required for creating new data are:
	 * - were all updated at least once
	 * - all have non-null values 
	 * */
	@JsonIgnore
	public Boolean areRequiredForCreatePropertiesProvided() {
		return areRequiredForDataCreatePropertiesPresent() 
				&& arePropertiesRequiredForCreateNotNull();
				
	}
	
	
	/**
	 * @return if properties that are always required for updating or creating data are:
	 * - were all updated at least once
	 * - all have non-null values 
	 * */
	@JsonIgnore
	public Boolean isRequiredPropertyProvided(String propName) {
		PropertyDescriptor prop = getPropertyDescriptor(propName);
		Object value = getPropertyValue(prop);
		
		return isUpdated(prop) && value != null;
	}

	private Object getPropertyValue(PropertyDescriptor prop) {
		Object value = null;
		try {
			 value = PropertyUtils.getProperty(this, prop.getName());
		} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			logger.error(e,e);
			throwPropertyNotFoundException(prop.getName());
		}
		return value;
	}
	
	
	/**
	 * @return list of properties getters , these properties had been set at least once.
	 * */
	@JsonIgnore
	public Set<PropertyDescriptor> getUpdatedProperties() {
		return updatedProperties;
				
	}
	
	
	@JsonIgnore
	public void setPropertyAsRequired(String propName, Required required) {
		PropertyDescriptor prop = getPropertyDescriptor(propName);
		if(prop == null)
			throwPropertyNotFoundException(propName);
		this.requiredMap.put(prop, required);
	}

	private PropertyDescriptor getPropertyDescriptor(String propName) {
		PropertyDescriptor prop = null;
		try {
			prop = PropertyUtils.getPropertyDescriptor(this, propName);
		} catch (Exception e) {
			logger.error(e,e);
			throwPropertyNotFoundException(propName);
		}		
		return prop;
	}
	
	
	
	@JsonIgnore
	public void setPropertyAsRequired(String propName) {
		setPropertyAsRequired(propName, Required.ALWAYS);
	}
	
	
	
	@JsonIgnore
	public void setPropertyAsRequiredForNewData(String propName) {
		setPropertyAsRequired(propName, Required.FOR_CREATE);
	}
	
	
	
	
	
	@JsonIgnore
	public void setPropertyAsRequiredForUpdatedData(String propName) {
		setPropertyAsRequired(propName, Required.FOR_UPDATE);
	}
	

	

	private void throwPropertyNotFoundException(String propName) {		
		String msg = String.format( "No property found with name [%s] for class of type [%s]", propName, getClass().getName());
		throw new IllegalStateException(msg);
	}
	
	
	/**
	 * assign the names of required properties and when it is required:
	 * - ALWAYS
	 * - FOR_CREATE : required when inserting  new data to the system
	 * - FOR_UPDATE : required when updating existing data in the system.
	 * */
	@JsonIgnore
	protected abstract void initRequiredProperties();
}
