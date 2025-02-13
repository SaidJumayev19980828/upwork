package com.nasnav.exceptions;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

public enum ErrorCodes {
	GLOBAL("unexpected behavior occurred while processing your request because [%s].")
	,U$0001("No user found with ID [%d]!")
	, UXACTVX0001("User with email[%s] doesn't exists for organization[%d]!")
	, UXACTVX0002("Cannot send activation email to[%s], email is already activated!")
	, UXACTVX0003("Cannot send activation email to[%s], need to wait[%s]!")
	, UXACTVX0004("Invalid redirection url[%s]!")
	, UXACTVX0005("Missing recovery token!")
	, UXACTVX0006("Invalid token! %s")
	, UXACTVX0007("Employee with email[%s] doesn't exists")

	, U$LOG$0001("NO USER FOUND FOR A TOKEN!")
	, U$LOG$0002("Invalid credentials!")
	, U$LOG$0003("Need activation!")
	, U$LOG$0004("Account is suspended!")
	, U$LOG$0005("Invalid password!")
	, U$LOG$0006("Expired token!")
	, U$LOG$0007("User with email[%s] already exists for organization[%d]!")
	, U$LOG$0008("Account is already activated!")
	, U$LOG$0009("Email is already subscribed!")
	, U$LOG$0010("No user found with email [%s]!")
	, U$LOG$0011("Need activation method !")

	,U$IMG$0001("File with the same content already exists!")
	,U$IMG$0002("Failed to delete image due to errors!\nerrors:[%s]")
	,U$IMG$0003("Unable to read uploaded file")

	, U$STATUS$0001("Account is already suspended!")
	, U$STATUS$0002("User can't suspend/activates himself!")
	, U$STATUS$0003("Couldn't suspend/activate user account, invalid user status!")
	, U$STATUS$0004("Couldn't get user status!")

	,U$AUTH$0001("User is not an authorized to modify %s!")
	,UAUTH$0002("The user is not authorized to access this resource!")

	,U$EMP$0001("No Roles provided for user creation!")
	,U$EMP$0002("No Employee exists with id[%d]!")
	,U$EMP$0003("Invalid Name [%s]!")
	,U$EMP$0004("Invalid e-mail [%s]!")
	,U$EMP$0005("Invalid org_id [%d]!")
	,U$EMP$0006("An Employee with email[%s] already exists!")
	,U$EMP$0007("Invalid role [%s]!")
	,U$EMP$0008("Employee can't create employees")
	,U$EMP$0009("Employee can't create employees with requested roles!")
	,U$EMP$0010("Created employee must be in the same organization as the manager!")
	,U$EMP$0011("Created employee must be in the same store as the manager!")
	,U$EMP$0012("Invalid store_id [%d]!")
	,U$EMP$0013("Employee can't view other employees with requested roles!")
	,U$EMP$0014("Employee can't view customers!")
	,U$EMP$0015("Registration not confirmed by user!")

	,P$EXP$0001("No column found in csv for writing additional field with name[%s]!")
	,P$VAR$0001("No Variant found with id[%s]!")
	,P$VAR$0002("No Extra Attribute exists with name[%s] for organization[%id]")
	,P$VAR$003("No variant id provided!")
	,P$VAR$004("Missing variant_id or rate value or order_id!")
	,P$VAR$005("No Variant rate found with id[%d]!")
	,P$VAR$006("Rate must be between 0 and 5")
	,P$VAR$007("User can rate only bought products!")
	,P$VAR$008("Missing Extra Attribute name!")
	,P$VAR$009("Failed to delete variants [%s]!")
	,P$VAR$010("User can't delete variants from another org!")
	,P$VAR$011("No variant with feature[%d] exists with id[%d]!")


	,P$PRO$0000("No product exists with ID[%d]! for that Organization")
	,P$PRO$0001("No product id provided!")
	,P$PRO$0002("No product exists with ID[%d]!")
	,P$PRO$0003("Product name Must be provided!")
	,P$PRO$0004("Product name cannot be Null!")
	,P$PRO$0005("Brand Id Must be provided!")
	,P$PRO$0006("Failed to parse product update json [%s]!")
	,P$PRO$0007("No Operation provided! parameter operation should have values in[\"create\",\"update\"]!")
	,P$PRO$0008("No Operation provided! parameter operation should have values in[%s]!")
	,P$PRO$0009("Failed to delete products [%s]!")
	,P$PRO$0010("Product of ID[%d] cannot be deleted by a user from organization of id [%d]")
	,P$PRO$0011("Cannot delete Products! the products are inside bundles [%s] and must be removed first!")
	,P$PRO$0012("No collection exists with ID[%d]!")
	,P$PRO$0013("No collections exists with ID %s!")
	,P$PRO$0014("Some products are still used in collections! are you sure you want to delete them?")
	,P$PRO$0015("Product with id [%d] has no stocks!")
	,P$PRO$0016("Product with id [%d] Not Item or Bundle or not found")

	,P$IMPORT$0001("Store Manager is allowed only to update stocks!")
	,P$IMPORT$0002("Store Manager cannot access stocks of another shop!")
	,P$IMPORT$0003("Imported file isn't supported")
	,P$IMPORT$0004("Imported file is ambiguous and can be read as multiple types")

	,P$STO$0001("No stock exists with ID:[%d]!")
	,P$STO$0002("Stock total value can't be negative!")
	,P$STO$0003("Stock is linked to %s!")

	,P$BRA$0001("No Brand exists with ID:[%d]!")
	,P$BRA$0002("Brand with id [%d] doesn't belong to organization with id [%d]")
	,P$BRA$0003("Brand with id [%d] linked to products [%s]!")
	,P$BRA$0004("No Brand id is provided!")

	,P$FTR$0001("No Product Feature type exists with code[%d]!")
	,P$FTR$0002("Feature is still used by some products!")

	,P$IMG$0001("Provided Zip file has no data!")
	,P$IMG$0002("Provided images archive is not ZIP file!")
	,P$IMG$0003("No feature provided for swatch images!")
	,P$IMG$0004("No feature exists with id[%d]!")
	,P$IMG$0005("Failed to read zip file!")
	,P$IMG$0006("Failed To parse CSV file!")
	,P$IMG$0007("Failed to prepare images for import due to errors!\nerrors:[%s]")
	,P$IMG$0008("Feature is not of the type %s!")
	,P$IMG$0009("Failed to find data extra-attribute for feature [%d]!")
	,P$IMG$0010("Unsupported file MIME [%s]. Select image or video!")
	,P$IMG$0011("No Image exists with id [%s] within orgId [%s]!")
	,P$IMG$0012("Image delete require only one parameter (Image_id, product_id or brand_id)!")

	,GEN$0001("NO %s exists with ID:[%d]!")
	,GEN$0002("Missing or invalid %s, available values are [true, false]!")
	,GEN$0003("Couldn't send mail, reason[%s]")
	,GEN$0004("Unknown User Entity Type")
	,GEN$0005("the provided url is malformed!")
	,GEN$0006("Couldn't resize image [%s]")
	,GEN$0007("Must provide width or height!")
	,GEN$0008("No file name provided!")
	,GEN$0009("Failed to save file Organization directory at location : %s")
	,GEN$3dM$0001("Failed to save file model directory at location : %s")
	,GEN$3dM$0002("no 3d model exists with model id [%d]")
	,GEN$0010("Failed to create directory at location : %s")
	,GEN$0011("No file exists with url: %s")
	,GEN$0012("Invalid URL : %s")
	,GEN$0013("Failed to parse MIME type for the file:  %s")
	,GEN$0014("Failed to parse type for the file:  %s")
	,GEN$0015("Couldn't read image [%s]")
	,GEN$0016("Provided category_id[%d] doesn't match any existing category!")
	,GEN$0017("There are still %s [%s] assigned to this category!")
	,GEN$0018("Invalid file type[%s]! only MIME 'image' types are accepted!")
	,GEN$0019("Failed to read resource[%s]!")
	,GEN$0021("Domain and subdir already exist[%s]!")
	,GEN$0022("Missing required parameters!")
	,GEN$0023("Failed to delete file with url[%s] at location [%s]")

	,S$0001("Shop is linked to %s !")
	,S$0002( "No Shop exists with ID: [%d]!")
	,S$0003("No shops found!")
	,S$0004("Shop with id[%d] has invalid address id[%d]")
	,S$0005( "No Shop exists with ID: [%d] for organization[%d]!")
	,S$0006("No shop id provided!")
	,S$0007("No shop found with provided code[%s]!")

	,S$360$0001("No 360 shop found!")
	,S$360$0002("No scene found with ID[%d]!")
	,S$360$0003("Must provide shop_id to attach shop360s to it")
	,S$360$0004("There exists shop360 attached to this shop already!")
	,S$360$0005("Must provide type for JsonData (web or mobile)")
	,S$360$F$0001("floor [%d] number can't be null!")
	,S$360$F$0002("No floor found")
	,S$360$F$0003("Provided floor No. [%d] doesn't exist!")
	,S$360$S$0002("No section found")
	,S$360$S$0003("Provided section No. [%d] doesn't exist!")
	,S$360$S$0004("Provided scene No. [%d] doesn't exist!")
	,S$360$PRO$POS$001("%s is linked to products positions!, please confirm if you want to delete them too")

	,TYP$0001("Missing or invalid type, available types are [%s]!")

	,ORG$EXTRATTR$0001("No extra attribute exists with ID:[%d]!")

	,ORG$SHIP$0001("Organization is not registered with shipping service[%s]!")
	,ORG$SHIP$0002("Couldn't get variant org_id!")

	,ORG$SITEMAP("User has no access to this sitemap!")

	,THEME$0001("Provided theme_class_id [%d] doesn't match any existing theme class!")
	,THEME$0002("There are %s linked to class [%d]")
	,THEME$0003("Provided theme_id [%s] doesn't match any existing theme!")
	,THEME$0004("uid is already used by another theme!")

	,ORG$THEME$0001("Removed classes has a theme[%d] assigned to org[%d]!")
	,ORG$THEME$0002("Removed theme is used by organizations %s!")
	,ORG$CREATE$001("Provided p_name is already used by another organization")
	,ORG$CREATE$002("Failed To Set Owner User To Organization")

	,ORG$FTR$0001("Invalid feature name! The feature name can't be null or Empty!")
	,ORG$FTR$0002("Invalid feature name! The feature name is already used!")
	,ORG$FTR$0003("Invalid parameters [feature_id], no feature exists with id [%d]!")

	,ORG$IMG$0001("No Image exists with id [%d]!")
	,ORG$IMG$0002("No Image exists with url [%s]!")
	,ORG$IMG$0003("Must provide either image_id or url!")
    ,ORG$IMG$0004("No Image Type exists with id [%d]!")
	,CAT$0001("Provided parent category[%d] doesn't exit!")
	,CAT$0002("Missing or invalid category ID!")

	,ORG$LOY$0001("Missing loyalty point type name!")
	,ORG$LOY$0002("Missing loyalty point properties!")
	,ORG$LOY$0003("Invalid loyalty point type id!")
	,ORG$LOY$0004("No loyalty point type exists with id[%d]!")
	,ORG$LOY$0005("Loyalty point type linked to loyalty points!")
	,ORG$LOY$0006("No loyalty point exists with id[%d]!")
	,ORG$LOY$0007("Loyalty point linked to user transactions!")
	,ORG$LOY$0008("Missing loyalty point config properties!")
	,ORG$LOY$0009("Start date can't be after end date!")
	,ORG$LOY$0010("Amount from can't be bigger than amount to!")
	,ORG$LOY$0011("No loyalty point config exists with id[%d]!")
	,ORG$LOY$0012("Can't redeem loyalty point with id[%d]!")
	,ORG$LOY$0013("Use either amounts or ratio!")
	,ORG$LOY$0014("User is not linked to org with id[%d] !")
	,ORG$LOY$0015("Invalid loyalty config for org with id[%d] !")
	,ORG$LOY$0016("no Loyalty event with id[%d] !")
	,ORG$LOY$0017("Invalid Pin code [%s] !")
	,ORG$LOY$0018("No config with id [%d] for org with id [%d] !")
	,ORG$LOY$0019("No tier found with id [%d]!")
	,ORG$LOY$0021("No tier found for user with id [%d]!")
	,ORG$LOY$0022("Tier is linked to config with id [%d]!")
	,ORG$LOY$0023("Tier is linked to [%d] users!")
	,ORG$LOY$0024("No active config for org with id [%d] !")
	,ORG$LOY$0025("the available points are not enough !")
	,ORG$LOY$0026("the Tier id [%d] have Active config with id [%d]!")

	,AREA$001("No Area exists with ID:[%d]!")
	,SUBAREA$001("No Sub-Area exists with ID:[%d] for organization[%d]!")
	,SUBAREA$002("Sub-Area with ID:[%d] doesn't match area with id[%d]!")
	,SUBAREA$003("Provided Sub-Areas %s doesn't match any existing sub-areas for organization[%d]!")
	,ADDR$ADDR$0001("%s with name [%s] already exists!")
	,ADDR$ADDR$0002("Address with id[%d] doesn't exists!")
	,ADDR$ADDR$0003("Must provide id of parent %s")
	,ADDR$ADDR$0004("Must provide customer address!")
	,ADDR$ADDR$0005("Address has no city!")
	,ADDR$ADDR$0006("No %s exists with ID:[%d]!")
	,ADDR$ADDR$0007("Can't delete %s linked to address")
	,ADDR$ADDR$0008("No Country name was provided!")

	,PA$USR$0001("Package not deleted because related user!")
	,PA$USR$0002("No Package found with ID: [%d]")
	,PA$USR$0003("Stripe Price Id Is Missing")
	,PA$CUR$0002("Currency Not found with iso: [%d]")
	,PA$SRV$0001("No Service Exist with Code : [%s]")
	,PA$SRV$0002("No Service Exist with ID : [%s]")
	,PA$SRV$0003("Service Enabled Status Can`t Be Null")
	,PA$SRV$0004("No Service Found For This Org ID: [%s]")
	,PA$SRV$0005("No Service Found For Package: [%s]")


	,ORG$SUB$0001("No Package Registered In Organization")
	,ORG$SUB$0002("Unable to get Currency From Package")
	,ORG$SUB$0004("Organization Owner User Is Not Found")
	,ORG$SUB$0005("Organization Already Have Subscription")
	,ORG$SUB$0006("No Stripe Subscription In Organization")
	,ORG$SUB$0007("Package Id is Missing")
	,ORG$SUB$0008("Organization is Already subscribed in Package : [%d]")
    ,ORG$SUB$0009("No Package Registered In Organization or Subscription is expired or canceled")


	,STR$CAL$0001("Stripe Failed To Cancel Subscription")
	,STR$CAL$0002("Stripe Failed To Create Setup Intent")
	,STR$CAL$0003("Stripe Failed To Create Customer Using Email : [%s]")
	,STR$CAL$0004("Stripe Failed To Create Subscription")
	,STR$CAL$0005("Stripe Failed To Change Plan")
	,STR$CAL$0006("Stripe Failed To Change Plan : PriceId [%s] is Not Exist In Stripe")
	,STR$WH$0001("Stripe Webhook is not valid")
	,STR$WH$0002("Stripe Webhook : Failed To get Organization")
	,STR$WH$0003("Stripe Webhook : Failed To get subscribed Package")
	,STR$WH$0004("Stripe Webhook : Subscription Not Found")
	,STR$WH$0005("Stripe Webhook : Failed To Update Customer Default Payment Method")
	,STR$WH$0006("Stripe Webhook : Failed To Update Subscription Default Payment Method")
	,STR$WH$0007("Stripe Webhook : Failed To PayRetry Invoice of Subscription")
	,STR$WH$0008("Stripe Webhook : Failed To Deserialize Event Object")



	,BC$PRI$0001("Failed To Fetch Currency Price")

	,G$USR$0001("User is not an employee!")
	,G$JSON$0001("Failed to read json into an object!")
	,G$JSON$0002("Failed to write json from an object!")
	,G$PRAM$0001("Missing required parameters in object[%s]!")
	,G$PRAM$0002("Invalid Parameter value[%s]!")
	,G$ORG$0001("No organization exists with id[%d]!")
	,G$ORG$0002("No yeshtery organization found")
	,G$ORG$0003("No organization exists with name [%s]!")
	,G$STK$0001("Failed to get stock data for stock [%d]!")

	,E$USR$0001("User is an employee!")
	,E$USR$0002("User not found!")
	,E$USR$0003("Invalid current password!")
	,E$USR$0004("Password and Confirm Password do not match!")
    ,E$USR$0005("this user [%d] not assigned to Meetus AR Organization")
	,E$USR$0006("this user [%d] has a un expected role")
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
	,O$CRT$0012("Invalid Cart optimization configuration parameters [%s] !")
	,O$CRT$0013("Missing Cart optimization configuration parameters for organization [%d] and optimizer [%s] !")
	,O$CRT$0014("Failed to parse optimization configuration json for organization [%d] and optimizer [%s] !")
	,O$CRT$0015("Failed to optimize cart! Customer address has no Sub-Area, or the sub-area is not supported!")
	,O$CRT$0016("Requested quantity exceeded maximum allowed quantity for this item")
	,O$CRT$0017("Requested price exceeded maximum allowed price")
	,O$CRT$0018("Cart optimization resulted in empty cart!")
	,O$CRT$0019("Cart items has no stocks available!")
	,O$CRT$0020("Cart Must only has the items of same store you pickup from!")
	,O$SHP$0001("Failed to create shipment for order[%d]!")
	,O$SHP$0002("Sub-Order with id[%d] has no shop!")
	,O$SHP$0003("Failed to create a shipment for the order with the given parameters!")
	,O$SHP$0004("Failed to create a shipment for return request[%d]!")
	,O$SHP$0005("Meta-order with id [%d], has no sub-orders!")
	,O$SHP$0006("Provided area_id [%d] doesn't map to any existing external area_id")

	,O$CHK$0001("Must have at least one item in cart!")
	,O$CHK$0002("Must provide shipping service provider")
	,O$CHK$0003("Must provide shipping service additional data")
	,O$CHK$0004("Failed to finish checkout! Cart optimization for shipping resulted in changes in item%s!")

	,O$NEW$0001("Failed to create order! Please try again!")

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
	,O$RET$0023("No store found with this id!")

	,O$0001("No order exists with ID[%d]!")

	,O$GNRL$0001("Cannot update order status from [%s] to [%s]!")
	,O$GNRL$0002("No Meta order exists with id[%d]!")
	,O$GNRL$0003("Cannot access Meta order with id[%d] by the current user!")

	,O$WISH$0001("Employee users cannot manage wishlists!")
	,O$WISH$0002("No wishlist item exists with id[%d]")

	,SHP$OFFR$0001("Invalid Stock id's! no stocks were given or stocks doesn't exists!")

	,ENUM$0001("Invalid shipping status")
	,ENUM$0002("Provided status [%s] doesn't match any existing status!")

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
	,SHP$SRV$0013("Missing shipping address for shipping service[%s]!")
	,SHP$SRV$0014("Delivery is only supported in the same city!")
	,SHP$SRV$0015("Delivery is only available for items value starting from %s!")

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
	,PROMO$PARAM$0011("Promo code[%s] was already terminated!")
	,PROMO$PARAM$0012("Invalid promo type_id[%d]!")
	,PROMO$PARAM$0013("Must provide promo code for promo with type_id[%d]!")
	,PROMO$PARAM$0014("Usage limit exceeded max value!")
	,PROMO$PARAM$0015("Missing parameter[%s] in given promotion [%s]!")
	,PROMO$PARAM$0016("Promo code [%s] is not applicable to the current user!")
	,PROMO$PARAM$0017("Invalid start date")
	,PROMO$PARAM$0018("Invalid end date")
	,PROMO$PARAM$0019("organization(s) [%s] not found in yeshtery")

	,DATE$TIME$0001("Invalid date time field [%s]")
	,DATE$TIME$0002("Invalid date time format")

	,ORG$SETTING$0001("No setting exists with name[%s]!")
	,ORG$SETTING$0002("No setting type exists with [%d]!")
	,ORG$NOTFOUND("No Organization found")

	,TAG$TREE$0001("No tag exists with id[%s]!")
	,TAG$TREE$0002("No tag exists with name[%s]!")
	,
	ORG$ADDON$0001("No addon type exists with [%d]!")
	,ORG$ADDON$0002("No addon stock exists with [%d]!"),
	ORG$ADDON$0003("addon and cart item not in the same shop!")
	,ORG$ADDON$0004("Cart item  addons has no stocks available!")
	,NAVBOX$SRCH$0001("Missing required parameters, required parameters are org_id and keyword!")
	,NAVBOX$SRCH$0002("Failed to run search query on elasticsaerch!\nError details: %s ")

	,SRCH$SYNC$0001("Failed to synchronize products data for organization[%d]!")
	,SRCH$SYNC$0002("Failed to synchronize products data for organization[%d]!\nError details: %s")


	,SEO$ADD$0001("SEO keywords for entity of type[%s] and id[%d] cannot be modified by user from organization[%d]!")

	,INTG$EVENT$0001("Failed to create instance of event of type[%s]!")

	,LOY$PARAM$0004("Invalid loyalty points type_id[%d]!")
	,LOY$PARAM$0005("Loyalty points is not applicable to the current user!")
	,LOY$PARAM$0006("Point amount should be greater than zero!")
	,TIERS$PARAM$0002("Cannot update tier with id[%d], only inactive tiers can be update!")
	,TIERS$PARAM$0003("Missing parameters in given tiers [%s]!")
	,TIERS$PARAM$0004("Tier [%d] does not belong to the organization [%d]!")
	,TIERS$PARAM$0005("Can not remove the provided Tier With id [%d] because it is the default tier!")


	,REF$PARAM$0001("Invalid referral code type_id[%d]!")
	,REF$PARAM$0002("Couldn't find referral with id [%d]!")
	,REF$PARAM$0003("Couldn't find referral code [%s] for user!")
	,REF$PARAM$0004("Invalid referral code Status value[%d]!")
	,REF$PARAM$0005("referral accept token not valid!")
	,REF$PARAM$0006("the OTP not sent successfully!")
	,REF$PARAM$0007("There is no referral code for user to validate!")
	,REF$PARAM$0008("there is no referral code for this user")
	,REF$PARAM$0009("There is already Token sent for this user, plz check the SMS or resend token!")
	,REF$PARAM$0010("There is no settings for this organization!")
	,REF$PARAM$0011("Parent Registration ended!")
	,REF$PARAM$0012("Referral Registration ended!")
	,REF$PARAM$0013("Can't Register With phone number!")
	,REF$PARAM$0014("User Already has Referral Code!")
	,REF$PARAM$0015("Insufficient Referral Wallet Balance! Wallet Balance must cover the total price.")
	,REF$PARAM$0016("There is no existing referral code with code [%s]")

	,VIDEO$PARAM$0001("Video chat is not enabled for organization with id[%d]!")
	,VIDEO$PARAM$0002("Invalid user type!")
	,VIDEO$PARAM$0003("Session not found!")
	,VIDEO$PARAM$0004("Invalid Session!")
	,VIDEO$PARAM$0005("Couldn't establish new connection, %s")
	,VIDEO$PARAM$0006("Must provide either org_id or shop_id!")
	,VIDEO$PARAM$0007("User has active session!")
	,VIDEO$PARAM$0008("Organization reached its maximum connections limit!")

	,PAYMENT$CALLBACK$001("No payment entity found with transaction reference [%s]!")
	,PAYMENT$CALLBACK$002("Untrusted source!")

	,PROCESS$CANCEL$0001("Process not cancelable"),

	OTP$NOTFOUND("OTP not found"),
	OTP$INVALID("Invalid OTP")

	,AVA$NOT$EXIST("No Availability found")
	,AVA$OVER$LAPPED("Availabilities is overlapped with others")

	,G$INFLU$0001("Influencer Not found with id[%d]!")
	,G$INFLU$0002("Influencer with id[%d] request already exist!")
	,G$INFLU$0003("Influencer with id[%d] is suspended")

	,G$EVENT$0001("Event Not fount with id[%d]!")
	,EVENT$NOT$EDITABLE$0002("Event with id[%d] can't be updated due to it passed!")
	,EVENT$MODIFICATION$0003("Event request can't be modified")
	,EVENT$REQUEST$0004("Event request with id[%d] already exist!!")
	,EVENT$REQUEST$0005("Event request with id[%d] not found!!")
	,EVENT$HAS$HOST$0005("Event with id[%d] already has host!!")
	,EVENT$HAS$INTEREST$0006("You already interested in Event with id[%d]!!")

	,G$POST$0001("Post Not found with id[%id]!")
	,REVIEW_001("No Review found with that provided ID!")
	,POST$LIKE$0002("no record found that the user liked this post before!")
	,POST$LIKE$0003("user already liked this post before!")
	,POST$REVIEW$ATTACHMENT("No attachments found fot review you should upload images")

	,G$QUEUE$0001("no queue entity found")

	,CHAT$EXTERNAL("Chat Server responded with [%d]")

	,CHAT$NOT_CONFIGURED("Chat Server parameters not configured")

	,BANK$ACC$0001("No user or org is provided")
	,BANK$ACC$0002("an account already exist for this user or org")
	,BANK$ACC$0003("no account found for the user")
	,BANK$ACC$0004("the Deposit/Withdrawal not valid from BC point of view")
	,BANK$ACC$0005("the account have not enough balance")
	,BANK$ACC$0006("No reservation with id: [%d]")
	,BANK$ACC$0007("this transaction is done before")
	,BANK$ACC$0008("The provide Api key is Not Valid, check it again")
	,BANK$ACC$0009("no account found for the Organization")

	,BANK$TRANS$0001("Insufficient Fund for that Customer")


	,ROOMS$ROOM$NotFound("No Room Template found with %s id [%d]")
	,ROOMS$ROOM$InvalidStatus("room status can't be changed from %s to %s")
	,ROOMS$ORG$NotFound("Organization with id [%d] not found or inaccessible")

	,ADVER$001("No Advertisement found with id [%d]")
	,ADVER$002(" Advertisement product must not contain multiple rules with the same action!")


	,NOTIF$0001("Firebase not initialized")
	,NOTIF$0002("Firebase error occurred with error code [%s]")
	,NOTIF$0003("No notification tokens found for user with id [%d]")
	,NOTIF$0004("No notification tokens found for organization with id [%d]")
	,NOTIF$0005("No notification tokens found for shop with id [%d]")
	,NOTIF$0006("Couldn't get message body")
	,FRT$VARS001("Error while processing the file")
	,FRT$VARS002("Invalid Key Or Error while processing the file ")
	,CSV$001("Parsing error due to missing headers [%s]")
	,CSV$002("Invalid data in column: [%s], row number: [%d]")
	,XLS$001("The following table header(s) not found: [%s]")
	,XLS$002("Conversion error or invalid data at row number: [%d], column name: [%s]")

	,G$CONTACT$0001("No Contact-Us form exists with id[%d]!")
	,NOTIUSER$0006("We can not find customer Id With Body Request")
	,NOTSELECTEDSTOCKIDS("We can not find selected stock ids With Body Request (provide selectedStockIds property)")
	,NOTIUSERPARAM$0006("We can not find customer Id With Request Param")
	,PROMO$EXCEPTION("Discount is bigger than the min price")
	,$001$PROMO$("Promo not found")
	,$002$PROMO$("This promo can't be used. Usage limit reached.")
	,SCRAPPING$001("You should upload Scrapping file if you want the process to be manual")
	,SCRAPPING$002("No Web Scraping record found for that Organization  with id [%s]")
	,SCRAPPING$003("An error occurred while processing your file because [%s]. Please check the file format and try again")

	,$001$REFERRAL$("No Referral Wallet Found for that User %d.")

	,$003d$MODEL$("missing params you should enter barcode or sku."),
	$004d$MODEL$("missing params you should enter at lest one file."),
	MODEL$005("This barcode already exists in the database."),
	MODEL$006("This SKU already exists in the database."),

	PE_EVENT_$001("No personal event found with the provided ID [%s] and a start date in the future For that User"),
	PE_EVENT_$002("No personal event found with the provided ID [%s]"),

	COMPEN$001("No Compensation Action found with the provided ID [%s]"),

	COMPEN$002("No Compensation Rule found with the provided ID [%s] for that Organization"),

	COMPEN$003("Cannot update or delete a rule that is currently in use with an advertisement!"),

	BC$001("An error occurred while processing that request du to %s"),

	STORE_CHECKOUT$001("There is no user for this employee to do checkout"),
	INFREF$001("User name already exists!"),
	INFREF$002("Passwords Doesn't match!"),
	INFREF$003("User name or password incorrect!"),
	INFREF$004("No influencer username found!"),
	INFREF$005("There is already promotion code with same code!"),
	INFREF$006("Choose only one of cashback strategy!"),

	PERMISSION$001("The permission already exists in the system."),
	PERMISSION$002("The name cannot be empty or null. Please provide name for the permission"),
	PERMISSION$003("The permission is not found in the system."),
	ROLE$001("The role is not found in the system."),
	ROLE$002("The name and organizationId cannot be null"),
	ROLE$003("The role already exists");

	@Getter
	@JsonValue
    private final String value;

	@JsonCreator
	ErrorCodes(String value) {
        this.value = value;
    }
}
