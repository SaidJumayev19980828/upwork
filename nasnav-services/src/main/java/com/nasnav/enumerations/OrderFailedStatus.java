package com.nasnav.enumerations;

/**
 * Hold Response Status constants
 */
public enum OrderFailedStatus {
	//@JsonProperty("UNAUTHENTICATED")
	UNAUTHENTICATED,	//401	User credentials not provided in headers or invalid
	//@JsonProperty("INSUFFICIENT_RIGHTS")
	INSUFFICIENT_RIGHTS,	//	401	the user does not have right to modify this order
	//@JsonProperty("MULTIPLE_STORES")
	MULTIPLE_STORES,	//	406	requested items belong to several shops
	//@JsonProperty("SYNTAX_ERROR")
	SYNTAX_ERROR,	//	406	unable to parse the JSON basket
	//@JsonProperty("INVALID_ORDER")
	INVALID_ORDER,	//	406	order does not exist
	//@JsonProperty("INVALID_STATUS")
	INVALID_STATUS	//406	order status cannot be changed to the requested
}