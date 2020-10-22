package com.nasnav.exceptions;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.Getter;

public enum ErrorCodes {
	U$0001("No user found with ID [%d]!")
	, UXACTVX0001("User with email[%s] doesn't exists for organization[%d]!")
	, UXACTVX0002("Cannot send activation email to[%s], email is already activated!")
	, UXACTVX0003("Cannot send activation email to[%s], need to wait[%s]!")
	, UXACTVX0004("Invalid redirection url[%s]!")
	, UXACTVX0005("Missing recovery token!")
	, UXACTVX0006("Invalid token! %s")
	
	, U$LOG$0001("NO USER FOUND FOR A TOKEN!")
	, U$LOG$0002("Invalid credentials!")
	, U$LOG$0003("Need activation!")
	, U$LOG$0004("Account is suspended!")
	, U$LOG$0005("Invalid password[%s]!")
	, U$LOG$0006("Expired token!")
	, U$LOG$0007("User with email[%s] already exists for organization[%d]!")
	, U$LOG$0008("Account is already activated!")


	, U$STATUS$0001("Account is already suspended!")

	,U$AUTH$0001("User is not an authorized to modify %s!")

	,U$EMP$0001("No Roles provided for user creation!")
	,U$EMP$0002("No Employee exists with id[%d]!")
	,U$EMP$0003("Invalid Name [%s]!")
	,U$EMP$0004("Invalid e-mail [%s]!")
	,U$EMP$0005("Invalid org_id [%d]!")
	,U$EMP$0006("Employee with email[%s] already exists for organization[%d]!")
	,U$EMP$0007("Invalid role [%s]!")
	,U$EMP$0008("Employee can't create employees")
	,U$EMP$0009("Employee can't create employees with requested roles!")
	,U$EMP$0010("Created employee must be in the same organization as the manager!")
	,U$EMP$0011("Created employee must be in the same store as the manager!")
	,U$EMP$0012("Invalid store_id [%d]!")

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
	,GEN$0002("Missing or invalid %s, available values are [true, false]!")
	,GEN$0003("Couldn't send mail, reason[$s]")
	,GEN$0004("Unknown User Entity Type]")

	,S$0001("Shop is linked to %s !")
	,S$0002( "No Shop exists with ID: [%d]!")
	,S$0003("No shops found!")
	,S$0004("Shop with id[%d] has invalid address id[%d]")
	,S$0005( "No Shop exists with ID: [%d] for organization[%d]!")
	,S$0006("No shop id provided!")

	,S$360$0001("No 360 shop found!")
	,S$360$0002("No scene found with ID[%d]!")
	,S$360$F$0001("floor [%d] number can't be null!")
	,S$360$PRO$POS$001("%s is linked to products positions!, please confirm if you want to delete them too")

	,TYP$0001("Missing or invalid type, available types are [%s]!")

	,ORG$EXTRATTR$0001("No extra attribute exists with ID:[%d]!")

	,ORG$0001("No organization found with ID[%d]!")

	,ORG$SHIP$0001("Organization is not registered with shipping service[%s]!")

	,AREA$001("No Area exists with ID:[%d]!")
	,ADDR$ADDR$0001("%s with name [%s] already exists!")
	,ADDR$ADDR$0002("Address with id[%d] doesn't exists!")
	,ADDR$ADDR$0003("Must provide id of parent $s")
	,ADDR$ADDR$0004("Must provide customer address!")
	,ADDR$ADDR$0005("Address has no city!")
	,ADDR$ADDR$0006("No %s exists with ID:[%d]!")
	,ADDR$ADDR$0007("Can't delete %s linked to address")
	,ADDR$ADDR$0008("No Country name was provided!")

	,G$USR$0001("User is not an employee!")
	,G$JSON$0001("Failed to read json into an object!")
	,G$PRAM$0001("Missing required parameters in object[%s]!")
	,G$PRAM$0002("Invalid Parameter value[%s]!")
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
	,O$CRT$0012("Invalid Cart optimization common parameters [%s] !")
	,O$CRT$0013("Missing Cart optimization common parameters for organization [%d] and optimizer [%s] !")
	,O$CRT$0014("Failed to parse Common optimization parameters for organization [%d] and optimizer [%s] !")
	
	,O$SHP$0001("Failed to create shipment for order[%d]!")
	,O$SHP$0002("Sub-Order with id[%d] has no shop!")
	,O$SHP$0003("Failed to create a shipment for the order with the given parameters!")
	,O$SHP$0004("Failed to create a shipment for return request[%d]!")

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

	,O$RET$0001("No return request item found with ID[%s]!")
	,O$RET$0002("Returned item quantity cannot be greater than requested quantity!")
	,O$RET$0003("Either provide return request item or basket item!")
	,O$RET$0004("Must provide return request item ID!")
	,O$RET$0005("Must provide return request item Quantity!")
	,O$RET$0006("Must provide return basket item ID!")
	,O$RET$0007("Must provide return basket item Quantity!")
	,O$RET$0008("No order items found with ID[%s]!")
	,O$RET$0009("Returned items are in different orders!")
	,O$RET$0010("Return request items are in different return request!")
    ,O$RET$0012("Return request item with basket[%d] already existing!")
    ,O$RET$0013("Returned item quantity cannot be less than or equal 0!")
    ,O$RET$0014("No Order item exists with id[%d]!")
	,O$RET$0015("Cannot Return order items that belongs to another user!")
	,O$RET$0016("Return period has passed for item(s)!")
	,O$RET$0017("No return request found with ID[%d]!")
	,O$RET$0018("Cannot update return request status from [%s] to [%s]!")
	,O$RET$0019("Failed to get customer address!")
	,O$RET$0020("Store managers cannot update received items! Please contact the organization manager to do it!")
	,O$RET$0021("Cannot return order with status[%s]!")
	,O$RET$0022("Items are not returnable or are overdue!")

	,O$0001("No order exists with ID[%d]!")

	,O$GNRL$0001("Cannot update order status from [%s] to [%s]!")
	,O$GNRL$0002("No Meta order exists with id[%d]!")
	,O$GNRL$0003("Cannot access Meta order with id[%d] by the current user!")

	,O$WISH$0001("Employee users cannot manage wishlists!")
	,O$WISH$0002("No wishlist item exists with id[%d]")

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
	,SHP$SRV$0012("Failed to calculate shipping fees for shipping service [%s], with shipment details[%s]!"
			+ " Make sure there is no missing data , and the destination city is supported by the shipping service!")

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
	,PROMO$PARAM$0006("Promo code [%s] was already used before by another promotion!")
	,PROMO$PARAM$0007("No Promo exists with id[%d]!")
	,PROMO$PARAM$0008("No active Promo exists with code[%s]!")
	,PROMO$PARAM$0009("Promo code [%s] is not applicable to the current cart!")
	,PROMO$PARAM$0010("Promo code[%s] was already used!")
	
	,ORG$SETTING$0001("No setting exists with name[%s]!")

	,TAG$TREE$0001("No tag exists with id[%s]!")
	;
	
	@Getter
	@JsonValue
    private final String value;
	
	@JsonCreator
	ErrorCodes(String value) {
        this.value = value;
    }
}
