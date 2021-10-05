package com.nasnav.constatnts.error.dataimport;

public class ErrorMessages {
	public static final String ERR_PRODUCT_IMPORT_MISSING_PARAM = "Missing required parameters! required parameters are {shop_id, currency, encoding, headers, csv}";
	public static final String ERR_MISSING_STOCK_UPDATE_PARAMS = "Missing required parameters! required parameters are {shop_id, variant_id, [quantity OR price and currency]}, while given data is[%s]!";
	public static final String ERR_BRAND_NAME_NOT_EXIST = "No brand exists with name [%s]";
	public static final String ERR_INVALID_ENCODING = "Encoding [%s] is not supported!";
	public static final String ERR_USER_CANNOT_CHANGE_OTHER_ORG_SHOP = "User that belongs to organization with id[%d] is not allowed to make changes to a shop from organization with id[%d]";
	public static final String ERR_NO_FILE_UPLOADED = "Uploaded file is invalid or empty!";
	public static final String ERR_CSV_PARSE_FAILURE = "Failed To parse CSV file!";
	public static final String ERR_CONVERT_TO_JSON = "Failed to serialize Object of type[%s] as json string";
	public static final String ERR_NO_IMG_DATA_PROVIDED = "No images to import!";
	public static final String ERR_NO_PRODUCT_EXISTS_WITH_ID = "There is no product exists with id[%d]";
	public static final String ERR_IMPORTING_IMG_FILE = " Importing image file[%s] caused the error[%s]";
	public static final String ERR_NO_IMG_IMPORT_RESPONSE = "No Import reposponse was returned!";
	public static final String ERR_USER_CANNOT_MODIFY_PRODUCT ="User with email [%s] have no rights to modify products from organization of id[%d]!";
	public static final String ERR_TAGS_NOT_FOUND = "tags with names %s were not found for organization[%d]";
	public static final String ERR_NO_VARIANT_FOUND = "No Variant exists with id[%s] , or external id [%s], or barcode [%s]!";
}
