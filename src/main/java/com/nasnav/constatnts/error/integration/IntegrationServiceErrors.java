package com.nasnav.constatnts.error.integration;

public class IntegrationServiceErrors {
	public static final String ERR_NO_INTEGRATION_PARAMS = "Integrated Organization with ID[%s] has no Integration Parameters!";
	public static final String ERR_MISSING_MANDATORY_PARAMS = "Integrated Organization with ID[%s] has missing required Integration Parameters!";
	public static final String ERR_NO_INTEGRATION_MODULE = "Integrated Organization with ID[%d] has no defined integration module!";
	public static final String ERR_LOADING_INTEGRATION_MODULE_CLASS = "Failed to load IntegrationModule class [%s] for Integrated Organization with ID[%s]!";
	public static final String ERR_INTEGRATION_MODULE_LOAD_FAILED = "Failed to load Integration Module for Integrated Organization with ID[%s]! ";
	public static final String ERR_EVENT_HANDLE_FAILED = "Failed to handle event[%s]!";
	public static final String ERR_EVENT_HANDLE_FALLBACK_RUN_FAILED = "Failed to handle event[%s], and the Error fallback caused exception of type [%s]!";
	public static final String ERR_EVENT_HANDLE_GENERAL_ERROR = "Failed to run both the handling logic and the error Fallback logic of event [%s]!"
																		+ "\nEvent handling caused exception [%s]!"
																		+ "\nFallback logic caused exception [%s]!";
	public static final String ERR_INVALID_PARAM_NAME = "Integration Parameter name[%s] is not a valid parameter name! Parameter names are expected to only have"
															+ " uppercase characters and '_'.";
	public static final String ERR_ORG_NOT_EXISTS = "Organization of id[%d] doesn't exists!";
}
