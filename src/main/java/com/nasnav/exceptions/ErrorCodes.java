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

	,U$AUTH$0001("User is not an authorized to modify %s!")
	
	,P$EXP$0001("No column found in csv for writing additional field with name[%s]!")
	,P$VAR$0001("No Variant found with id[%s]!")
	,P$VAR$0002("No Extra Attribute exists with name[%s] for organization[%id]")
	,P$VAR$003("No variant id provided!")
	
	,P$PRO$0001("No product id provided!")
	,P$PRO$0002("No product exists with ID[%d]!")
	,P$PRO$0003("Product name Must be provided!")
	,P$PRO$0004("Product name cannot be Null!")
	,P$PRO$0005("Brand Id Must be provided!")
	,P$PRO$0006("Failed to parse product update json [%s]!")
	,P$PRO$0007("No Operation provided! parameter operation should have values in[\"create\",\"update\"]!")
	,P$PRO$0008("No Operation provided! parameter operation should have values in[%s]!")

	,P$STO$0001("No stock exists with ID:[%d]!")
	
	,P$BRA$0001("No Brand exists with ID:[%d]!")
	,P$BRA$0002("Brand with id [%d] doesnot belong to organization with id [%d]")

	,GEN$0001("NO %s exists with ID:[%d]!")

	,S$0001("Shop is linked to %s !")
	,S$0002( "No Shop exists with ID: [%d]!")
	,S$0003("No shops found!")
	,S$0004("Shop with id[%d] has invalid address id[%d]")
	,S$0005( "No Shop exists with ID: [%d] for organization[%d]!")

	,S$360$0001("No 360 shop found!")
	,S$360$F$0001("floor [%d] number can't be null!")

	,TYP$0001("Missing or invalid type, available types are [%s]!")

	,ORG$EXTRATTR$0001("No extra attribute exists with ID:[%d]!")

	,ORG$SHIP$0001("Organization is not registered with shipping service[%s]!")

	,AREA$001("No Area exists with ID:[%d]!")
	,ADDR$ADDR$0001("%s with name [%s] already exists!")
	,ADDR$ADDR$0002("Address with id[%d] doesn't exists!")
	,ADDR$ADDR$0003("Must provide id of parent $s")
	,ADDR$ADDR$0004("Must provide customer address!")
	,ADDR$ADDR$0005("Address has no city!")
	,ADDR$ADDR$0006("No %s exists with ID:[%d]!")
	,ADDR$ADDR$0007("Can't delete %s linked to address")

	,G$USR$0001("User is not an employee!")
	,G$JSON$0001("Failed to read json into an object!")
	,G$PRAM$0001("Missing required parameters in object[%s]!")
	,G$ORG$0001("No organization exists with id[%d]!")
	,G$STK$0001("Failed to get stock data for stock [%d]!")

	,E$USR$0001("User is an employee!")

	,O$CRT$0001("Cannot create a cart for an employee user!")
	,O$CRT$0002("Quantity must be greater than equal zero!")
	,O$CRT$0003("Quantity is greater than available stock!")
	,O$CRT$0004("Currencies of items are different!")
	,O$CRT$0005("Cart items belong to different organizations!")
	,O$CRT$0006("Missing Cart optimization strategy!")
	,O$CRT$0007("Invalid Cart optimization strategy [%s] !")
	,O$CRT$0008("Failed to parse Cart optimization parameters [%s] !")
	,O$CRT$0009("Failed to get a cart optimizer with name [%s] !")
	,O$CRT$0010("Invalid or missing cart optimization parameters!")
	,O$CRT$0011("Failed to optimize cart item for shipping! No stock can fulfill the cart for cart item with id[%d] and stock Id[%d]!")

	,O$SHP$0001("Failed to create shipment for order[%d]!")
	,O$SHP$0002("Sub-Order with id[%d] has no shop!")
	,O$SHP$0003("Failed to create a shipment for the order with the given parameters!")

	,O$CHK$0001("Must have at least one item in cart!")
	,O$CHK$0002("Must provide shipping service provider")
	,O$CHK$0003("Must provide shipping service additional data")
	,O$CHK$0004("Failed to finish checkout! Cart optimization for shipping resulted in changes in item prices!")
	
	,O$CFRM$0001("No order exists for shop[%d] with id[%d]!")
	,O$CFRM$0002("Cannot Confirm order with id[%d]! Invalid order Status [%s]!")
	,O$CFRM$0003("Cannot Confirm order with id[%d]! User didn't provide a phone number!")
	,O$CFRM$0004("No order exists for Organizations[%d] with id[%d]!")
	
	,O$RJCT$0001("No sub order Id was provided!")
	,O$RJCT$0002("Cannot reject order with id[%d]! Invalid order Status [%s]!")
	
	,O$CNCL$0002("Cannot cancel Meta order with id[%d]! Invalid order Status [%s]!")

	,O$MAIL$0001("Failed to send notification email about order[%d] to email[%s]!")
	
	,O$ORG$0001("Sub-Order with id[%d] has no Organization!")

	,O$0001("No order exists with ID[%d]!")
	
	,O$GNRL$0001("Cannot update order staus from [%s] to [%s]!")
	,O$GNRL$0002("No Meta order exists with id[%d]!")
	,O$GNRL$0003("Cannot access Meta order with id[%d] by the current user!")
	
	,SHP$OFFR$0001("Invalid Stock id's! no stocks were given or stocks doesn't exists!")

	,ENUM$0001("Invalid shipping status")
	
	,SHP$SRV$0001("Invalid service parameter [%s]!")
	,SHP$SRV$0002("Missing Service parameters for shipping service[%s]!")
	,SHP$SRV$0003("Missing Service parameter with name[%s] for shipping service[%s]!")
	,SHP$SRV$0004("Failed to get valid response from external shipping service [%s]! returned response is[%s]")
	,SHP$SRV$0005("Shipping service [%s] doesnot support city with id[%d]!")
	,SHP$SRV$0006("No Shipping service exits with id[%s]!")
	,SHP$SRV$0007("Invalid Shipping service parameters structure [%s]!")
	,SHP$SRV$0008("The given value [%s] for Service parameter [%s] of shipping service [%s] has invlaid type!")
	,SHP$SRV$0009("Shipment not found!")
	,SHP$SRV$0010("Cannot create shipment with the given parameters!")
	,SHP$SRV$0011("Cannot create shipment with the given parameters due to : %s!")

	,SHP$PARS$0001("Error while parsing status update request!")

	,SHP$SVC$0001("Shipping service is not available!")


	,SHP$USR$0001("Cannot request shipment! User with id[%d] doesn't exists!")
	
	,PROMO$ENUM$0001("Saved Promo has invalid status!")
	,PROMO$JSON$0001("Saved Promo has invalid json string value[%s]!")
	,PROMO$PARAM$0001("Invalid promo status [%s]!")
	,PROMO$PARAM$0002("Missing parameters in given promotion [%s]!")
	,PROMO$PARAM$0003("Promotion start date cannot be after its end date!")
	,PROMO$PARAM$0004("Promotion dates cannot be in the past!")
	,PROMO$PARAM$0005("Cannot update promotion with id[%d], only inactive promotions can be update!")
	,PROMO$PARAM$0006("Promo code [%s] was already used before!")
	,PROMO$PARAM$0007("No Promo exisits with id[%d]!")
	;
	
	@Getter
	@JsonValue
    private final String value;
	
	@JsonCreator
	ErrorCodes(String value) {
        this.value = value;
    }
}
