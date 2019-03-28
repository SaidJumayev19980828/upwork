package com.nasnav.enumerations;

/**
 * Hold Response Status constants
 */
public enum OrderFailedStatus {
	UNAUTHENTICATED,	//401	User credentials not provided in headers or invalid
	INSUFFICIENT_RIGHTS,	//	401	the user does not have right to modify this order
	MULTIPLE_STORES,	//	406	requested items belong to several shops
	SYNTAX_ERROR,	//	406	unable to parse the JSON basket
	INVALID_ORDER,	//	406	order does not exist
	INVALID_STATUS;	//406	order status cannot be changed to the requested
}