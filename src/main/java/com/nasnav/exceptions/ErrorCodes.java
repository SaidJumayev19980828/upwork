package com.nasnav.exceptions;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.Getter;

public enum ErrorCodes {
	
	UXACTVX0001("User with email[%s] doesn't exists for organization[%d]!")
	, UXACTVX0002("Cannot send activation email to[%s], email is already activated!")
	, UXACTVX0003("Cannot send activation email to[%s], need to wait[%s]!")
	, UXACTVX0004("Invalid redirection url[%s]!")
	
	, U$LOG$0001("NO USER FOUND FOR A TOKEN!")
	
	,P$EXP$0001("No column found in csv for writing product feature[%s]!")
	,P$VAR$0001("No Variant found with id[%d]!") 
	,P$VAR$0002("No Extra Attribute exists with name[%s] for organization[%id]")
	
	,P$PRO$0001("No product id provided!")
	,P$PRO$0002("No product exists with ID[%d]!")
	,P$PRO$0003("Product name Must be provided!")
	,P$PRO$0004("Product name cannot be Null!")
	,P$PRO$0005("Brand Id Must be provided!")
	,P$PRO$0006("Failed to parse product update json [%s]!")
	,P$PRO$0007("No Operation provided! parameter operation should have values in[\"create\",\"update\"]!")
	
	,P$BRA$0001("No Brand exists with ID:[%d]!")
	,P$BRA$0002("Brand with id [%d] doesnot belong to organization with id [%d]")
	
	;
	
	@Getter
	@JsonValue
    private final String value;
	
	@JsonCreator
	ErrorCodes(String value) {
        this.value = value;
    }
}
