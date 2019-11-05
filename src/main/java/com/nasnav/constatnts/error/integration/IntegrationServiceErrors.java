package com.nasnav.constatnts.error.integration;

public class IntegrationServiceErrors {
	public static final String ERR_NO_INTEGRATION_PARAMS = "Integrated Organization with ID[%s] has no Integration Parameters!";
	public static final String ERR_MISSING_MANDATORY_PARAMS = "Integrated Organization with ID[%s] has missing required Integration Parameters!";
	public static final String ERR_NO_INTEGRATION_MODULE = "Integrated Organization with ID[%s] has no defined integration module!";
	public static final String ERR_LOADING_INTEGRATION_MODULE_CLASS = "Failed to load IntegrationModule class [%s] for Integrated Organization with ID[%s]!";
	public static final String ERR_INTEGRATION_MODULE_LOAD_FAILED = "Failed to load Integration Module for Integrated Organization with ID[%s]! ";
}
